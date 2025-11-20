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
import { getDoneList } from '@/api/businessTrip'

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
    title: '审批结果',
    key: 'status',
    width: 100,
    render: (row) => {
      const tag = getStatusTag(row.status)
      return h(NTag, { type: tag.type }, { default: () => tag.label })
    }
  },
  {
    title: '审批时间',
    key: 'approveTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => {
      return h(NButton, {
        size: 'small',
        onClick: () => handleView(row.id)
      }, { default: () => '查看' })
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
    
    const res = await getDoneList(params)
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
  // TODO: 查看详情
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
