<template>
  <div class="done-list">
    <n-card title="已办审批">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :pagination="pagination"
        :loading="loading"
        @update:page="handlePageChange"
      />
    </n-card>
    
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
          <n-descriptions-item label="使用期限">
            {{ detailData.usePeriod || '-' }}
          </n-descriptions-item>
          <n-descriptions-item v-if="detailData.remark" label="备注" :span="2">
            {{ detailData.remark }}
          </n-descriptions-item>
          <n-descriptions-item label="申请时间" :span="2">
            {{ detailData.createTime }}
          </n-descriptions-item>
          <n-descriptions-item label="审批结果">
            <n-tag :type="getStatusTag(detailData.status).type">
              {{ getStatusTag(detailData.status).label }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="审批时间">
            {{ detailData.approveTime || '-' }}
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
  NButton, 
  NTag,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NSpin,
  NSpace,
  useMessage
} from 'naive-ui'
import { getDoneList, getDetailWithUserInfo } from '@/api/pettyCash'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])
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

const getStatusTag = (status) => {
  const map = {
    2: { type: 'success', label: '已通过' },
    3: { type: 'error', label: '已驳回' }
  }
  return map[status] || { type: 'default', label: '未知' }
}

const columns = [
  { title: '申请编号', key: 'applyNo', width: 180 },
  { title: '申请人', key: 'userName', width: 100 },
  { title: '部门', key: 'deptName', width: 120 },
  { title: '申请事由', key: 'reason', ellipsis: { tooltip: true } },
  { title: '申请金额', key: 'totalAmount', width: 120, render: (row) => `¥${row.totalAmount || 0}` },
  {
    title: '审批结果',
    key: 'status',
    width: 100,
    render: (row) => {
      const tag = getStatusTag(row.status)
      return h(NTag, { type: tag.type }, { default: () => tag.label })
    }
  },
  { title: '审批时间', key: 'approveTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => {
      return h(NButton, { size: 'small', onClick: () => handleView(row.id) }, { default: () => '查看' })
    }
  }
]

const loadData = async () => {
  try {
    loading.value = true
    const res = await getDoneList({ current: pagination.value.page, size: pagination.value.pageSize })
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.done-list {
  max-width: 1400px;
}
</style>
