<template>
  <div class="my-list">
    <n-card title="我的申请">
      <template #header-extra>
        <n-button type="primary" @click="router.push('/business-trip/apply')">
          新建申请
        </n-button>
      </template>
      
      <n-space vertical :size="16">
        <n-space>
          <n-select
            v-model:value="statusFilter"
            :options="statusOptions"
            placeholder="状态筛选"
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
          <n-descriptions-item label="申请状态">
            <n-button 
              v-if="detailData.status !== 0" 
              text 
              type="primary" 
              @click="showApprovalHistory(detailData.id)"
            >
              <n-tag :type="getStatusTag(detailData.status).type">
                {{ getStatusTag(detailData.status).label }}
              </n-tag>
            </n-button>
            <n-tag v-else :type="getStatusTag(detailData.status).type">
              {{ getStatusTag(detailData.status).label }}
            </n-tag>
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
          <n-descriptions-item label="总费用" :span="2">
            <span style="color: #18a058; font-weight: 600;">¥{{ detailData.totalAmount || 0 }}</span>
          </n-descriptions-item>
          <n-descriptions-item v-if="detailData.remark" label="备注" :span="2">
            {{ detailData.remark }}
          </n-descriptions-item>
          <n-descriptions-item label="申请时间" :span="2">
            {{ formatDateTime(detailData.createTime) }}
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
    
    <!-- 审批流程弹窗 -->
    <n-modal
      v-model:show="showApprovalHistoryModal"
      preset="card"
      title="审批流程"
      style="width: 800px"
      :bordered="false"
    >
      <n-spin :show="approvalHistoryLoading">
        <!-- 新的审批流程展示 -->
        <div v-if="processNodes && processNodes.length > 0" class="process-flow-container">
          <n-steps :current="currentStep" :status="stepStatus">
            <n-step 
              v-for="(node, index) in processNodes" 
              :key="index"
              :title="node.nodeName"
              :description="getStepDescription(node, index)"
            >
              <template #icon>
                <n-icon v-if="isNodeCompleted(index)">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16">
                    <circle cx="12" cy="12" r="10" fill="#18a058"/>
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" fill="#ffffff"/>
                  </svg>
                </n-icon>
                <n-icon v-else-if="isNodeCurrent(index)">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16">
                    <circle cx="12" cy="12" r="10" fill="#2080f0"/>
                    <polygon points="10,8 16,12 10,16" fill="#ffffff"/>
                  </svg>
                </n-icon>
                <n-icon v-else>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16">
                    <circle cx="12" cy="12" r="10" fill="#ccc"/>
                  </svg>
                </n-icon>
              </template>
            </n-step>
          </n-steps>
          
          <!-- 审批详情 -->
          <div class="approval-details" v-if="approvalHistory && approvalHistory.length > 0">
            <n-divider title-placement="left">审批详情</n-divider>
            <n-timeline>
              <n-timeline-item 
                v-for="(item, index) in approvalHistory" 
                :key="index"
                :type="getTimelineType(item.action)"
                :title="getActionLabel(item.action)"
                :time="formatDateTime(item.createTime)"
              >
                <template #default>
                  <n-text depth="3" style="font-size: 12px;">审批意见：{{ item.opinion || '无' }}</n-text>
                </template>
                <template #footer>
                  <n-text depth="3" style="font-size: 12px;">审批人：{{ item.approverName }}</n-text>
                </template>
              </n-timeline-item>
            </n-timeline>
          </div>
        </div>
        
        <!-- 原有的时间线展示（作为备选） -->
        <n-timeline v-else-if="approvalHistory.length > 0">
          <n-timeline-item 
            v-for="(item, index) in approvalHistory" 
            :key="index"
            :type="getTimelineType(item.action)"
            :title="getActionLabel(item.action)"
            :time="formatDateTime(item.createTime)"
          >
            <template #default>
              <n-text depth="3" style="font-size: 12px;">审批意见：{{ item.opinion || '无' }}</n-text>
            </template>
            <template #footer>
              <n-text depth="3" style="font-size: 12px;">审批人：{{ item.approverName }} {{formatDateTime(item.createTime) }}</n-text>
            </template>
          </n-timeline-item>
        </n-timeline>
        <n-empty v-else description="暂无审批记录" />
      </n-spin>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showApprovalHistoryModal = false">关闭</n-button>
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
import { useRouter } from 'vue-router'
import { 
  NCard,
  NButton,
  NSpace,
  NSelect,
  NDataTable,
  NTag,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NDivider,
  NSpin,
  NIcon,
  NImage,
  NEmpty,
  NTimeline,
  NTimelineItem,
  NText,
  NSteps,
  NStep,
  useMessage
} from 'naive-ui'
import { AttachOutline as AttachIcon } from '@vicons/ionicons5'
import { getMyList, getDetail, getApprovalHistory } from '@/api/businessTrip'
import { getProcessNodes } from '@/api/workflow'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref(null)
const showDetailModal = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const showPreviewModal = ref(false)
const previewFile = ref({ fileName: '', filePath: '' })

// 审批历史相关
const showApprovalHistoryModal = ref(false)
const approvalHistoryLoading = ref(false)
const approvalHistory = ref([])
const processNodes = ref([]) // 流程节点信息
const currentStep = ref(0) // 当前步骤
const stepStatus = ref('process') // 步骤状态

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return date
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  // 如果没有值，返回默认值
  if (!dateTime) return '-'
  
  try {
    // 如果已经是格式化的字符串
    if (typeof dateTime === 'string') {
      // 检查是否是标准的日期时间格式
      if (dateTime.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)) {
        return dateTime
      }
      // 检查是否是ISO格式
      if (dateTime.includes('T')) {
        const date = new Date(dateTime)
        if (!isNaN(date.getTime())) {
          const year = date.getFullYear()
          const month = String(date.getMonth() + 1).padStart(2, '0')
          const day = String(date.getDate()).padStart(2, '0')
          const hours = String(date.getHours()).padStart(2, '0')
          const minutes = String(date.getMinutes()).padStart(2, '0')
          const seconds = String(date.getSeconds()).padStart(2, '0')
          return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
        }
      }
      // 其他字符串格式直接返回
      return dateTime
    }
    
    // 如果是数字（时间戳）
    if (typeof dateTime === 'number') {
      const date = new Date(dateTime)
      if (!isNaN(date.getTime())) {
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
    }
    
    // 如果是Date对象
    if (dateTime instanceof Date) {
      if (!isNaN(dateTime.getTime())) {
        const year = dateTime.getFullYear()
        const month = String(dateTime.getMonth() + 1).padStart(2, '0')
        const day = String(dateTime.getDate()).padStart(2, '0')
        const hours = String(dateTime.getHours()).padStart(2, '0')
        const minutes = String(dateTime.getMinutes()).padStart(2, '0')
        const seconds = String(dateTime.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
    }
    
    // 如果是对象（可能是LocalDateTime的序列化形式）
    if (typeof dateTime === 'object') {
      // 尝试从对象中提取日期信息
      if (dateTime.year && dateTime.month && dateTime.dayOfMonth) {
        // LocalDateTime序列化后的对象格式
        const year = dateTime.year
        const month = String(dateTime.monthValue || dateTime.month).padStart(2, '0')
        const day = String(dateTime.dayOfMonth).padStart(2, '0')
        const hours = String(dateTime.hour || 0).padStart(2, '0')
        const minutes = String(dateTime.minute || 0).padStart(2, '0')
        const seconds = String(dateTime.second || 0).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
    }
    
    // 默认情况下返回原值
    return dateTime
  } catch (e) {
    // 如果有任何错误，返回原始值
    console.error('Error formatting datetime:', e)
    return dateTime
  }
}

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '审批中', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已驳回', value: 3 }
]

const getStatusTag = (status) => {
  const map = {
    0: { type: 'default', label: '草稿' },
    1: { type: 'info', label: '审批中' },
    2: { type: 'success', label: '已通过' },
    3: { type: 'error', label: '已驳回' }
  }
  return map[status] || { type: 'default', label: '未知' }
}

// 获取时间线类型
const getTimelineType = (action) => {
  switch (action) {
    case 'APPROVE':
      return 'success'
    case 'REJECT':
      return 'error'
    default:
      return 'info'
  }
}

// 获取操作标签
const getActionLabel = (action) => {
  switch (action) {
    case 'APPROVE':
      return '审批通过'
    case 'REJECT':
      return '审批驳回'
    default:
      return '提交申请'
  }
}

// 获取步骤描述
const getStepDescription = (node, index) => {
  // 查找该节点的审批记录
  const approvalRecord = approvalHistory.value.find(record => {
    // 这里需要根据实际情况匹配节点和审批记录
    // 简化处理：假设节点顺序和审批记录顺序一致
    return approvalHistory.value.indexOf(record) === index
  })
  
  if (approvalRecord) {
    return `${approvalRecord.approverName} - ${formatDateTime(approvalRecord.createTime)}`
  }
  
  if (isNodeCompleted(index)) {
    return '已完成'
  } else if (isNodeCurrent(index)) {
    return '进行中'
  } else {
    return '待处理'
  }
}

// 判断节点是否已完成
const isNodeCompleted = (index) => {
  // 简化处理：假设前几个节点已完成
  return index < approvalHistory.value.length
}

// 判断节点是否为当前节点
const isNodeCurrent = (index) => {
  // 简化处理：假设当前节点是下一个未完成的节点
  return index === approvalHistory.value.length
}

const columns = [
  {
    title: '申请编号',
    key: 'applyNo',
    width: 180
  },
  {
    title: '出差事由',
    key: 'reason',
    width: 120,
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
    title: '出差时间',
    key: 'tripDate',
    width: 220,
    render: (row) => `${formatDate(row.startDate)} ~ ${formatDate(row.endDate)}`
  },
  {
    title: '总费用',
    key: 'totalAmount',
    width: 120,
    render: (row) => `¥${row.totalAmount || 0}`
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      // 对于非草稿状态，显示为可点击的按钮
      if (row.status !== 0) {
        return h(NButton, {
          text: true,
          type: 'primary',
          onClick: () => showApprovalHistory(row.id)
        }, {
          default: () => h(NTag, { type: getStatusTag(row.status).type }, { 
            default: () => getStatusTag(row.status).label 
          })
        })
      } else {
        // 草稿状态显示为普通标签
        const tag = getStatusTag(row.status)
        return h(NTag, { type: tag.type }, { default: () => tag.label })
      }
    }
  },
  {
    title: '申请时间',
    key: 'applyDate',
    width: 180,
    render: (row) => formatDateTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, {
            size: 'small',
            onClick: () => handleView(row.id)
          }, { default: () => '查看' }),
          row.status === 0 && h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleEdit(row.id)
          }, { default: () => '编辑' })
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
      size: pagination.value.pageSize,
      status: statusFilter.value
    }
    
    const res = await getMyList(params)
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

// 显示审批历史
const showApprovalHistory = async (id) => {
  try {
    showApprovalHistoryModal.value = true
    approvalHistoryLoading.value = true
    
    // 获取审批历史记录
    const historyRes = await getApprovalHistory(id)
    approvalHistory.value = historyRes.data
    
    // 获取流程节点信息
    const nodesRes = await getProcessNodes('businessTripProcess')
    processNodes.value = nodesRes.data
    
    // 计算当前步骤
    currentStep.value = approvalHistory.value.length
    stepStatus.value = detailData.value && detailData.value.status === 3 ? 'error' : 'process'
  } catch (error) {
    console.error('获取审批历史失败：', error)
    message.error('获取审批历史失败')
  } finally {
    approvalHistoryLoading.value = false
  }
}

const handleEdit = (id) => {
  // 跳转到申请页面，带上ID进行编辑
  router.push({ path: '/business-trip/apply', query: { id } })
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-list {
  max-width: 1400px;
}

.process-flow-container {
  padding: 20px 0;
}

.approval-details {
  margin-top: 30px;
}
</style>