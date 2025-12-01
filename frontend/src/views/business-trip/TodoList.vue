<template>
  <div class="todo-list">
    <n-card title="待办审批">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :pagination="pagination"
        :loading="loading"
        @update:page="handlePageChange"
      />
    </n-card>
    
    <!-- 审批弹窗 -->
    <n-modal
      v-model:show="showApprovalModal"
      preset="card"
      title="审批"
      style="width: 600px"
    >
      <n-form>
        <n-form-item label="审批意见">
          <n-input
            v-model:value="approvalOpinion"
            type="textarea"
            placeholder="请输入审批意见"
            :rows="4"
          />
        </n-form-item>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showApprovalModal = false">取消</n-button>
          <n-button type="error" @click="handleReject">驳回</n-button>
          <n-button type="primary" @click="handleApprove">通过</n-button>
        </n-space>
      </template>
    </n-modal>
    
    <!-- 详情弹窗 -->
    <n-modal
      v-model:show="showDetailModal"
      preset="card"
      title="出差申请详情"
      style="width: 800px"
      :bordered="false"
    >
      <n-spin :show="detailLoading">
        <n-descriptions v-if="detailData" :column="2" bordered>
          <n-descriptions-item label="申请编号">
            {{ detailData.applyNo }}
          </n-descriptions-item>
          <n-descriptions-item label="申请人">
            {{ detailData.userName }} ({{ detailData.deptName }})
          </n-descriptions-item>
          <n-descriptions-item label="出差事由" :span="2">
            {{ detailData.reason }}
          </n-descriptions-item>
          <n-descriptions-item label="出发地">
            {{ detailData.startPlace }}
          </n-descriptions-item>
          <n-descriptions-item label="目的地">
            {{ detailData.destination }}
          </n-descriptions-item>
          <n-descriptions-item label="开始日期">
            {{ detailData.startDate }}
          </n-descriptions-item>
          <n-descriptions-item label="结束日期">
            {{ detailData.endDate }}
          </n-descriptions-item>
          <n-descriptions-item label="交通方式" :span="2">
            {{ detailData.transportModes }}
          </n-descriptions-item>
          <n-descriptions-item label="预计费用" :span="2">
            <span style="color: #18a058; font-weight: 600;">¥{{ detailData.totalAmount || 0 }}</span>
          </n-descriptions-item>
          <n-descriptions-item v-if="detailData.remark" label="备注" :span="2">
            {{ detailData.remark }}
          </n-descriptions-item>
          <n-descriptions-item label="申请时间" :span="2">
            {{ detailData.createTime }}
          </n-descriptions-item>
        </n-descriptions>
        
        <!-- 费用明细 -->
        <n-divider v-if="detailData?.expenses?.length > 0">费用明细</n-divider>
        <div v-if="detailData?.expenses?.length > 0" style="margin-top: 16px;">
          <n-card
            v-for="(expense, index) in detailData.expenses"
            :key="index"
            size="small"
            style="margin-bottom: 12px;"
          >
            <n-descriptions :column="3" size="small">
              <n-descriptions-item label="费用类型">
                {{ expense.expenseType }}
              </n-descriptions-item>
              <n-descriptions-item label="金额">
                <span style="color: #18a058; font-weight: 600;">¥{{ expense.amount }}</span>
              </n-descriptions-item>
              <n-descriptions-item label="备注">
                {{ expense.remark || '-' }}
              </n-descriptions-item>
            </n-descriptions>
            
            <!-- 附件列表 -->
            <div v-if="expense.attachments?.length > 0" style="margin-top: 12px;">
              <div style="font-size: 12px; color: #666; margin-bottom: 8px;">附件：</div>
              <n-space>
                <n-button
                  v-for="(att, attIdx) in expense.attachments"
                  :key="attIdx"
                  text
                  type="primary"
                  size="small"
                  @click="previewAttachment(att)"
                >
                  <template #icon>
                    <n-icon :component="AttachIcon" />
                  </template>
                  {{ att.fileName }}
                </n-button>
              </n-space>
            </div>
          </n-card>
        </div>
      </n-spin>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetailModal = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
    
    <!-- 附件预览弹窗 -->
    <n-modal
      v-model:show="showPreviewModal"
      preset="card"
      :title="previewFile.fileName"
      style="width: 90%; max-width: 1200px;"
      :bordered="false"
    >
      <div style="max-height: 70vh; overflow: auto;">
        <!-- 图片预览 -->
        <n-image
          v-if="isImage(previewFile.fileName)"
          :src="previewFile.filePath"
          style="width: 100%;"
        />
        
        <!-- PDF预览 -->
        <iframe
          v-else-if="isPdf(previewFile.fileName)"
          :src="previewFile.filePath"
          style="width: 100%; height: 70vh; border: none;"
        />
        
        <!-- 其他文件类型提示下载 -->
        <n-empty v-else description="此文件类型不支持预览">
          <template #extra>
            <n-button size="small" @click="downloadFile(previewFile.filePath)">
              点击下载
            </n-button>
          </template>
        </n-empty>
      </div>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="downloadFile(previewFile.filePath)">下载</n-button>
          <n-button @click="showPreviewModal = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { 
  NCard,
  NDataTable,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NButton, 
  NTag, 
  NSpace,
  NDescriptions,
  NDescriptionsItem,
  NDivider,
  NSpin,
  NIcon,
  NImage,
  NEmpty,
  useMessage 
} from 'naive-ui'
import { AttachOutline as AttachIcon } from '@vicons/ionicons5'
import { getTodoList, approve, reject, getDetail } from '@/api/businessTrip'

const message = useMessage()

const loading = ref(false)
const tableData = ref([])
const showApprovalModal = ref(false)
const approvalOpinion = ref('')
const currentRow = ref(null)
const showDetailModal = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const showPreviewModal = ref(false)
const previewFile = ref({ fileName: '', filePath: '' })

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  // 后端已经返回格式化的字符串，直接使用
  return dateTime
}

const columns = [
  {
    title: '申请编号',
    key: 'applyNo',
    width: 180
  },
  {
    title: '申请人',
    key: 'userName',
    width: 100
  },
  {
    title: '部门',
    key: 'deptName',
    width: 120
  },
  {
    title: '出差事由',
    key: 'reason',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '目的地',
    key: 'destination',
    width: 120
  },
  {
    title: '预计费用',
    key: 'totalAmount',
    width: 120,
    render: (row) => `¥${row.totalAmount || 0}`
  },
  {
    title: '申请时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatDateTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, {
            size: 'small',
            onClick: () => handleView(row.id)
          }, { default: () => '查看' }),
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleShowApproval(row)
          }, { default: () => '审批' })
        ]
      })
    }
  }
]

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.value.page,
      size: pagination.value.pageSize
    }
    
    const res = await getTodoList(params)
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

const handleView = async (id) => {
  try {
    showDetailModal.value = true
    detailLoading.value = true
    const res = await getDetail(id)
    detailData.value = res.data
  } catch (error) {
    console.error('获取详情失败：', error)
    message.error('获取详情失败')
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
}

// 判断是否为图片
const isImage = (fileName) => {
  if (!fileName) return false
  const ext = fileName.toLowerCase().split('.').pop()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)
}

// 判断是否为PDF
const isPdf = (fileName) => {
  if (!fileName) return false
  return fileName.toLowerCase().endsWith('.pdf')
}

// 预览附件
const previewAttachment = (attachment) => {
  previewFile.value = {
    fileName: attachment.fileName,
    filePath: attachment.filePath
  }
  showPreviewModal.value = true
}

// 下载文件
const downloadFile = (url) => {
  const link = document.createElement('a')
  link.href = url
  link.download = ''
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleShowApproval = (row) => {
  currentRow.value = row
  approvalOpinion.value = ''
  showApprovalModal.value = true
}

const handleApprove = async () => {
  try {
    await approve(currentRow.value.taskId, approvalOpinion.value)
    message.success('审批成功')
    showApprovalModal.value = false
    loadData()
  } catch (error) {
    console.error('审批失败：', error)
    message.error('审批失败')
  }
}

const handleReject = async () => {
  if (!approvalOpinion.value) {
    message.error('请输入驳回原因')
    return
  }
  
  try {
    await reject(currentRow.value.taskId, approvalOpinion.value)
    message.success('已驳回')
    showApprovalModal.value = false
    loadData()
  } catch (error) {
    console.error('驳回失败：', error)
    message.error('驳回失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.todo-list {
  max-width: 1400px;
}
</style>
