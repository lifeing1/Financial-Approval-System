<template>
  <div class="workflow-list">
    <n-card title="流程管理">
      <template #header-extra>
        <n-space>
          <n-button type="primary" @click="handleShowUploadModal">
            上传流程
          </n-button>
          <n-button type="primary" @click="showDesignerModal = true">
            设计流程
          </n-button>
        </n-space>
      </template>
      
      <n-space vertical :size="16">
        <n-space>
          <n-select
            v-model:value="category"
            :options="categoryOptions"
            placeholder="流程分类"
            style="width: 150px"
            clearable
            @update:value="loadData"
          />
        </n-space>
        
        <n-data-table
          :columns="columns"
          :data="tableData"
          :pagination="pagination"
          :loading="loading"
          @update:page="handlePageChange"
        />
      </n-space>
    </n-card>
    
    <!-- 上传流程对话框 -->
    <n-modal
      v-model:show="showUploadModal"
      preset="card"
      title="部署流程"
      style="width: 600px"
    >
      <n-form>
        <n-form-item label="部署方式">
          <n-radio-group v-model:value="uploadForm.deployType">
            <n-space>
              <n-radio value="preset">选择项目文件</n-radio>
              <n-radio value="upload">上传本地文件</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        
        <n-form-item v-if="uploadForm.deployType === 'preset'" label="选择BPMN文件">
          <n-select
            v-model:value="uploadForm.selectedPresetFile"
            :options="presetFiles.map(f => ({ label: f, value: f }))"
            placeholder="请选择BPMN文件"
          />
        </n-form-item>
        
        <n-form-item v-if="uploadForm.deployType === 'upload'" label="BPMN文件">
          <n-upload
            :max="1"
            accept=".bpmn,.xml"
            :on-change="handleFileChange"
          >
            <n-button>{{ uploadForm.file ? uploadForm.file.name : '选择文件' }}</n-button>
          </n-upload>
        </n-form-item>
        
        <n-form-item label="流程名称">
          <n-input v-model:value="uploadForm.processName" placeholder="请输入流程名称" />
        </n-form-item>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showUploadModal = false">取消</n-button>
          <n-button type="primary" @click="handleUploadDeploy">确定</n-button>
        </n-space>
      </template>
    </n-modal>
    
    <!-- 流程设计器对话框 -->
    <n-modal
      v-model:show="showDesignerModal"
      preset="card"
      title="流程设计器"
      style="width: 90%; max-width: 1200px;"
    >
      <n-alert type="info" style="margin-bottom: 16px">
        流程设计器功能开发中，当前可以通过上传BPMN文件进行部署。
        <br/>
        后续将集成 bpmn-js 实现可视化流程设计功能。
      </n-alert>
    </n-modal>
    
    <!-- 节点配置对话框 -->
    <n-modal
      v-model:show="showNodeConfigModal"
      preset="card"
      title="流程节点配置"
      style="width: 800px"
    >
      <n-form v-if="currentProcess">
        <n-alert type="info" style="margin-bottom: 16px">
          流程：{{ currentProcess.processName }} ({{ currentProcess.processKey }})
        </n-alert>
        
        <n-divider>节点列表</n-divider>
        
        <n-empty v-if="!currentProcess.nodes || currentProcess.nodes.length === 0" description="暂无节点配置" />
        
        <n-list v-else bordered>
          <n-list-item v-for="node in currentProcess.nodes" :key="node.id">
            <n-thing>
              <template #header>{{ node.nodeName }} ({{ node.nodeKey }})</template>
              <template #description>
                <n-space>
                  <n-tag v-if="node.assigneeType === 'user'" type="info">指定用户</n-tag>
                  <n-tag v-else-if="node.assigneeType === 'role'" type="success">指定角色</n-tag>
                  <n-tag v-else-if="node.assigneeType === 'dept_leader'" type="warning">部门负责人</n-tag>
                  <n-tag v-if="node.allowReject">允许驳回</n-tag>
                  <n-tag v-if="node.requireAttachment">需要附件</n-tag>
                  <n-text v-if="node.timeoutHours > 0" depth="3">超时：{{ node.timeoutHours }}小时</n-text>
                </n-space>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
      </n-form>
    </n-modal>
    
    <!-- 历史版本对话框 -->
    <n-modal
      v-model:show="showVersionsModal"
      preset="card"
      title="历史版本"
      style="width: 800px"
    >
      <n-data-table
        :columns="versionColumns"
        :data="versionList"
        :pagination="false"
      />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import {
  NCard,
  NButton,
  NSpace,
  NSelect,
  NDataTable,
  NTag,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NUpload,
  NRadioGroup,
  NRadio,
  NAlert,
  NDivider,
  NEmpty,
  NList,
  NListItem,
  NThing,
  NText,
  useMessage,
  useDialog
} from 'naive-ui'
import {
  getProcessDefinitionList,
  getProcessDefinitionDetail,
  deployProcessByFile,
  getPresetBpmnFiles,
  deployPresetBpmn,
  deleteDeployment,
  activateProcess,
  suspendProcess,
  exportProcessBpmn,
  getProcessVersions
} from '@/api/workflow'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const category = ref(null)
const showUploadModal = ref(false)
const showDesignerModal = ref(false)
const showNodeConfigModal = ref(false)
const showVersionsModal = ref(false)
const currentProcess = ref(null)
const versionList = ref([])

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const uploadForm = ref({
  processName: '',
  file: null,
  deployType: 'preset', // 'preset' 或 'upload'
  selectedPresetFile: null
})

const presetFiles = ref([])

const categoryOptions = [
  { label: '出差审批', value: 'business_trip' },
  { label: '备用金审批', value: 'petty_cash' }
]

const getStatusTag = (status) => {
  return status === 1
    ? { type: 'success', label: '激活' }
    : { type: 'default', label: '暂停' }
}

const getCategoryLabel = (cat) => {
  const option = categoryOptions.find(o => o.value === cat)
  return option ? option.label : cat
}

const columns = [
  { title: '流程名称', key: 'processName', width: 200 },
  { title: '流程KEY', key: 'processKey', width: 180 },
  {
    title: '分类',
    key: 'category',
    width: 120,
    render: (row) => getCategoryLabel(row.category)
  },
  { title: '版本', key: 'version', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const tag = getStatusTag(row.status)
      return h(NTag, { type: tag.type }, { default: () => tag.label })
    }
  },
  { title: '部署时间', key: 'deployTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 350,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, {
            size: 'small',
            onClick: () => handleViewNodes(row.processKey)
          }, { default: () => '节点配置' }),
          h(NButton, {
            size: 'small',
            onClick: () => handleViewVersions(row.processKey)
          }, { default: () => '历史版本' }),
          row.status === 1
            ? h(NButton, {
              size: 'small',
              type: 'warning',
              onClick: () => handleSuspend(row.processKey)
            }, { default: () => '暂停' })
            : h(NButton, {
              size: 'small',
              type: 'success',
              onClick: () => handleActivate(row.processKey)
            }, { default: () => '激活' }),
          h(NButton, {
            size: 'small',
            onClick: () => handleExport(row.processDefinitionId)
          }, { default: () => '导出' }),
          h(NButton, {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.deploymentId)
          }, { default: () => '删除' })
        ]
      })
    }
  }
]

const versionColumns = [
  { title: '版本号', key: 'version', width: 100 },
  { title: '流程定义ID', key: 'processDefinitionId' },
  { title: '部署ID', key: 'deploymentId' },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const tag = getStatusTag(row.status)
      return h(NTag, { type: tag.type }, { default: () => tag.label })
    }
  },
  { title: '部署时间', key: 'deployTime', width: 180 }
]

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.value.page,
      size: pagination.value.pageSize,
      category: category.value
    }
    
    const res = await getProcessDefinitionList(params)
    tableData.value = res.data.records
    pagination.value.itemCount = res.data.total
  } catch (error) {
    console.error('加载数据失败：', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadData()
}

const handleFileChange = ({ file }) => {
  uploadForm.value.file = file.file
}

const handleUploadDeploy = async () => {
  if (!uploadForm.value.processName) {
    message.error('请输入流程名称')
    return
  }
  
  try {
    if (uploadForm.value.deployType === 'preset') {
      // 部署预置文件
      if (!uploadForm.value.selectedPresetFile) {
        message.error('请选择BPMN文件')
        return
      }
      
      await deployPresetBpmn({
        fileName: uploadForm.value.selectedPresetFile,
        processName: uploadForm.value.processName
      })
    } else {
      // 上传本地文件
      if (!uploadForm.value.file) {
        message.error('请选择BPMN文件')
        return
      }
      
      const formData = new FormData()
      formData.append('file', uploadForm.value.file)
      formData.append('processName', uploadForm.value.processName)
      
      await deployProcessByFile(formData)
    }
    
    message.success('部署成功')
    showUploadModal.value = false
    uploadForm.value = { 
      processName: '', 
      file: null,
      deployType: 'preset',
      selectedPresetFile: null
    }
    loadData()
  } catch (error) {
    console.error('部署失败：', error)
  }
}

const handleViewNodes = async (processKey) => {
  try {
    const res = await getProcessDefinitionDetail(processKey)
    currentProcess.value = res.data
    showNodeConfigModal.value = true
  } catch (error) {
    console.error('获取节点配置失败：', error)
  }
}

const handleViewVersions = async (processKey) => {
  try {
    const res = await getProcessVersions(processKey)
    versionList.value = res.data
    showVersionsModal.value = true
  } catch (error) {
    console.error('获取历史版本失败：', error)
  }
}

const handleActivate = async (processKey) => {
  try {
    await activateProcess(processKey)
    message.success('流程已激活')
    loadData()
  } catch (error) {
    console.error('激活失败：', error)
  }
}

const handleSuspend = async (processKey) => {
  try {
    await suspendProcess(processKey)
    message.success('流程已暂停')
    loadData()
  } catch (error) {
    console.error('暂停失败：', error)
  }
}

const handleExport = async (processDefinitionId) => {
  try {
    const blob = await exportProcessBpmn(processDefinitionId)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${processDefinitionId}.bpmn20.xml`
    link.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败：', error)
  }
}

const handleDelete = (deploymentId) => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除此流程部署吗？此操作不可恢复！',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteDeployment(deploymentId, true)
        message.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败：', error)
      }
    }
  })
}

const loadPresetFiles = async () => {
  try {
    const res = await getPresetBpmnFiles()
    presetFiles.value = res.data || []
  } catch (error) {
    console.error('加载预置文件列表失败：', error)
  }
}

const handleShowUploadModal = () => {
  loadPresetFiles()
  showUploadModal.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workflow-list {
  max-width: 1400px;
}
</style>
