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
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { 
  NCard,
  NDataTable,
  NButton, 
  NTag 
} from 'naive-ui'
import { getDoneList } from '@/api/pettyCash'

const loading = ref(false)
const tableData = ref([])

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
  { title: '申请金额', key: 'amount', width: 120, render: (row) => `¥${row.amount || 0}` },
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

const handleView = (id) => {
  console.log('查看', id)
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
