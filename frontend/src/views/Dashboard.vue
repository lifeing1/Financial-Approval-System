<template>
  <div class="dashboard">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
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
  NButton
} from 'naive-ui'

const router = useRouter()
const userStore = useUserStore()

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

// 加载数据
const loadData = async () => {
  // TODO: 调用接口获取数据
  statistics.value = {
    todo: 5,
    done: 23,
    myApply: 12,
    monthTrip: 3
  }
  
  todoList.value = [
    {
      id: 1,
      title: '张三的出差申请',
      description: '北京 → 上海，2024-01-15',
      status: '待审批',
      type: 'warning',
      createTime: '2024-01-10 10:30'
    },
    {
      id: 2,
      title: '李四的备用金申请',
      description: '申请金额：5000元',
      status: '待审批',
      type: 'warning',
      createTime: '2024-01-10 09:15'
    }
  ]
  
  applyList.value = [
    {
      id: 1,
      title: '我的出差申请',
      description: '深圳 → 广州，2024-01-20',
      status: '审批中',
      type: 'info',
      createTime: '2024-01-09 14:20'
    },
    {
      id: 2,
      title: '我的备用金申请',
      description: '申请金额：3000元',
      status: '已通过',
      type: 'success',
      createTime: '2024-01-08 11:45'
    }
  ]
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
