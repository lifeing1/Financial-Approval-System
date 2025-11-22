<template>
  <div class="my-list">
    <n-card title="我的申请">
      <template #header-extra>
        <n-button type="primary" @click="router.push('/petty-cash/apply')">
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
  NTag
} from 'naive-ui'
import { getMyList } from '@/api/pettyCash'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

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

const columns = [
  {
    title: '申请编号',
    key: 'applyNo',
    width: 180
  },
  {
    title: '申请事由',
    key: 'reason',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '申请金额',
    key: 'amount',
    width: 120,
    render: (row) => `¥${row.amount || 0}`
  },
  {
    title: '使用期限',
    key: 'usePeriod',
    width: 120
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const tag = getStatusTag(row.status)
      return h(NTag, { type: tag.type }, { default: () => tag.label })
    }
  },
  {
    title: '申请时间',
    key: 'applyDate',
    width: 150
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

const handleView = (id) => {
  console.log('查看', id)
}

const handleEdit = (id) => {
  console.log('编辑', id)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-list {
  max-width: 1400px;
}
</style>
