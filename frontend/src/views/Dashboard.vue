<template>
  <div class="dashboard">
    <n-spin :show="loading">
      <n-grid :cols="4" :x-gap="24" :y-gap="24">
      <!-- 统计卡片 -->
      <n-gi>
        <n-card title="待办事项" hoverable>
          <n-statistic :value="statistics.todo">
            <template #suffix>件</template>
          </n-statistic>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card title="已办事项" hoverable>
          <n-statistic :value="statistics.done">
            <template #suffix>件</template>
          </n-statistic>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card title="我的申请" hoverable>
          <n-statistic :value="statistics.myApply">
            <template #suffix>件</template>
          </n-statistic>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card title="本月出差" hoverable>
          <n-statistic :value="statistics.monthTrip">
            <template #suffix>次</template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>
    
    <n-grid :cols="2" :x-gap="24" style="margin-top: 24px">
      <!-- 待办列表 -->
      <n-gi>
        <n-card title="我的待办" :segmented="{ content: true }">
          <n-list hoverable clickable>
            <n-empty v-if="todoList.length === 0" description="暂无待办事项" />
            <n-list-item v-for="item in todoList" :key="item.id">
              <n-thing :title="item.title" :description="item.description">
                <template #footer>
                  <n-space>
                    <n-tag :type="item.type">{{ item.status }}</n-tag>
                    <n-text depth="3">{{ item.createTime }}</n-text>
                  </n-space>
                </template>
              </n-thing>
            </n-list-item>
            
            <template #footer>
              <n-button text type="primary" @click="router.push('/business-trip/todo')">
                查看全部 →
              </n-button>
            </template>
          </n-list>
        </n-card>
      </n-gi>
      
      <!-- 最近申请 -->
      <n-gi>
        <n-card title="最近申请" :segmented="{ content: true }">
          <n-list hoverable clickable>
            <n-empty v-if="applyList.length === 0" description="暂无申请记录" />
            <n-list-item v-for="item in applyList" :key="item.id">
              <n-thing :title="item.title" :description="item.description">
                <template #footer>
                  <n-space>
                    <n-tag :type="item.type">{{ item.status }}</n-tag>
                    <n-text depth="3">{{ item.createTime }}</n-text>
                  </n-space>
                </template>
              </n-thing>
            </n-list-item>
            
            <template #footer>
              <n-button text type="primary" @click="router.push('/business-trip/my')">
                查看全部 →
              </n-button>
            </template>
          </n-list>
        </n-card>
      </n-gi>
    </n-grid>
  </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessage } from 'naive-ui'
import {
  NGrid,
  NGi,
  NCard,
  NStatistic,
  NList,
  NListItem,
  NThing,
  NSpace,
  NTag,
  NText,
  NButton,
  NSpin,
  NEmpty
} from 'naive-ui'
import { getMyList as getMyBusinessTrip, getTodoList as getTodoBusinessTrip, getDoneList as getDoneBusinessTrip } from '@/api/businessTrip'
import { getMyList as getMyPettyCash, getTodoList as getTodoPettyCash, getDoneList as getDonePettyCash } from '@/api/pettyCash'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

// 统计数据
const statistics = ref({
  todo: 0,
  done: 0,
  myApply: 0,
  monthTrip: 0
})

// 待办列表
const todoList = ref([])

// 申请列表
const applyList = ref([])

// 加载状态
const loading = ref(false)

// 获取状态标签类型
const getStatusType = (status) => {
  const statusMap = {
    '草稿': 'default',
    '待审批': 'warning',
    '审批中': 'info',
    '已通过': 'success',
    '已驳回': 'error',
    '已完成': 'success'
  }
  return statusMap[status] || 'default'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    
    // 并行获取所有数据
    const [tripTodo, cashTodo, tripDone, cashDone, tripMy, cashMy] = await Promise.all([
      getTodoBusinessTrip({ current: 1, size: 10 }),
      getTodoPettyCash({ current: 1, size: 10 }),
      getDoneBusinessTrip({ current: 1, size: 10 }),
      getDonePettyCash({ current: 1, size: 10 }),
      getMyBusinessTrip({ current: 1, size: 10 }),
      getMyPettyCash({ current: 1, size: 10 })
    ])
    
    // 计算统计数据
    const todoTotal = (tripTodo.data?.total || 0) + (cashTodo.data?.total || 0)
    const doneTotal = (tripDone.data?.total || 0) + (cashDone.data?.total || 0)
    const myApplyTotal = (tripMy.data?.total || 0) + (cashMy.data?.total || 0)
    
    // 计算本月出差次数
    const currentMonth = new Date().getMonth()
    const currentYear = new Date().getFullYear()
    const monthTripCount = (tripMy.data?.records || []).filter(item => {
      const createDate = new Date(item.createTime)
      return createDate.getMonth() === currentMonth && 
             createDate.getFullYear() === currentYear &&
             item.status !== '草稿' && item.status !== '已驳回'
    }).length
    
    statistics.value = {
      todo: todoTotal,
      done: doneTotal,
      myApply: myApplyTotal,
      monthTrip: monthTripCount
    }
    
    // 处理待办列表 - 混合出差和备用金
    const todoTrips = (tripTodo.data?.records || []).slice(0, 3).map(item => ({
      id: `trip-${item.id}`,
      title: `${item.applicantName || ''}的出差申请`,
      description: `${item.destination || ''} - ${item.startDate || ''} 至 ${item.endDate || ''}`,
      status: item.status || '待审批',
      type: getStatusType(item.status),
      createTime: formatDate(item.createTime)
    }))
    
    const todoCash = (cashTodo.data?.records || []).slice(0, 3).map(item => ({
      id: `cash-${item.id}`,
      title: `${item.applicantName || ''}的备用金申请`,
      description: `申请金额：${item.amount || 0}元`,
      status: item.status || '待审批',
      type: getStatusType(item.status),
      createTime: formatDate(item.createTime)
    }))
    
    todoList.value = [...todoTrips, ...todoCash]
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      .slice(0, 5)
    
    // 处理我的申请列表 - 混合出差和备用金
    const myTrips = (tripMy.data?.records || []).slice(0, 3).map(item => ({
      id: `trip-${item.id}`,
      title: '我的出差申请',
      description: `${item.destination || ''} - ${item.startDate || ''} 至 ${item.endDate || ''}`,
      status: item.status || '草稿',
      type: getStatusType(item.status),
      createTime: formatDate(item.createTime)
    }))
    
    const myCash = (cashMy.data?.records || []).slice(0, 3).map(item => ({
      id: `cash-${item.id}`,
      title: '我的备用金申请',
      description: `申请金额：${item.amount || 0}元`,
      status: item.status || '草稿',
      type: getStatusType(item.status),
      createTime: formatDate(item.createTime)
    }))
    
    applyList.value = [...myTrips, ...myCash]
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      .slice(0, 5)
      
  } catch (error) {
    console.error('加载数据失败:', error)
    message.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
</style>
