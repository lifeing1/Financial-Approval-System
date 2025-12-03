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
      title="备用金申请详情"
      style="width: 700px"
      :bordered="false"
    >
      <n-spin :show="detailLoading">
        <n-descriptions v-if="detailData" :column="2" bordered>
          <n-descriptions-item label="申请编号">
            {{ detailData.applyNo }}
          </n-descriptions-item>
          <n-descriptions-item label="申请人">
            {{ detailData.userName || '-' }} ({{ detailData.deptName || '-' }})
          </n-descriptions-item>
          <n-descriptions-item label="申请事由" :span="2">
            {{ detailData.reason }}
          </n-descriptions-item>
          <n-descriptions-item label="申请金额">
            <span style="color: #18a058; font-weight: 600;">¥{{ detailData.amount || 0 }}</span>
          </n-descriptions-item>
<!--          <n-descriptions-item label="使用期限">-->
<!--            {{ detailData.usePeriod || '-' }}-->
<!--          </n-descriptions-item>-->
          <n-descriptions-item v-if="detailData.remark" label="备注" :span="2">
            {{ detailData.remark }}
          </n-descriptions-item>
          <n-descriptions-item label="申请时间" :span="2">
            {{ detailData.createTime }}
          </n-descriptions-item>
        </n-descriptions>
      </n-spin>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetailModal = false">关闭</n-button>
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
  NSpace,
  NDescriptions,
  NDescriptionsItem,
  NSpin,
  useMessage 
} from 'naive-ui'
import { getTodoList, approve, reject, getDetailWithUserInfo } from '@/api/pettyCash'

const message = useMessage()

const loading = ref(false)
const tableData = ref([])
const showApprovalModal = ref(false)
const approvalOpinion = ref('')
const currentRow = ref(null)
const showDetailModal = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const columns = [
  { title: '申请编号', key: 'applyNo', width: 180 },
  { title: '申请人', key: 'userName', width: 100 },
  { title: '部门', key: 'deptName', width: 120 },
  { title: '申请事由', key: 'reason', ellipsis: { tooltip: true } },
  { title: '申请金额', key: 'totalAmount', width: 120, render: (row) => `¥${row.totalAmount || 0}` },
  { title: '申请时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleView(row.id) }, { default: () => '查看' }),
          h(NButton, { size: 'small', type: 'primary', onClick: () => handleShowApproval(row) }, { default: () => '审批' })
        ]
      })
    }
  }
]

const loadData = async () => {
  try {
    loading.value = true
    const res = await getTodoList({ current: pagination.value.page, size: pagination.value.pageSize })
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
    const res = await getDetailWithUserInfo(id)
    detailData.value = res.data
  } catch (error) {
    console.error('获取详情失败：', error)
    message.error('获取详情失败')
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
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
