<template>
  <div class="statistics">
    <n-card title="出差统计">
      <n-space vertical :size="24">
        <n-space>
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            clearable
            @update:value="loadData"
          />
        </n-space>
        
        <n-grid :cols="3" :x-gap="24">
          <n-gi>
            <n-card title="按部门统计" size="small">
              <div ref="deptChartRef" style="height: 300px"></div>
            </n-card>
          </n-gi>
          
          <n-gi>
            <n-card title="按月份统计" size="small">
              <div ref="monthChartRef" style="height: 300px"></div>
            </n-card>
          </n-gi>
          
          <n-gi>
            <n-card title="按申请人统计" size="small">
              <div ref="userChartRef" style="height: 300px"></div>
            </n-card>
          </n-gi>
        </n-grid>
      </n-space>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { 
  NCard,
  NSpace,
  NDatePicker,
  NGrid,
  NGi
} from 'naive-ui'
import * as echarts from 'echarts'
import { getTripStatistics } from '@/api/statistics'

const dateRange = ref(null)
const deptChartRef = ref(null)
const monthChartRef = ref(null)
const userChartRef = ref(null)

let deptChart = null
let monthChart = null
let userChart = null

const initCharts = () => {
  nextTick(() => {
    if (deptChartRef.value) {
      deptChart = echarts.init(deptChartRef.value)
      deptChart.setOption({
        color: ['#18A058'],
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [{ name: '出差次数', type: 'bar', data: [] }]
      })
    }
    
    if (monthChartRef.value) {
      monthChart = echarts.init(monthChartRef.value)
      monthChart.setOption({
        color: ['#18A058'],
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [{ name: '出差费用', type: 'line', data: [] }]
      })
    }
    
    if (userChartRef.value) {
      userChart = echarts.init(userChartRef.value)
      userChart.setOption({
        color: ['#18A058'],
        tooltip: { trigger: 'item' },
        series: [{
          name: '出差次数',
          type: 'pie',
          radius: '50%',
          data: []
        }]
      })
    }
  })
}

const loadData = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = new Date(dateRange.value[0]).toISOString().split('T')[0]
      params.endDate = new Date(dateRange.value[1]).toISOString().split('T')[0]
    }
    
    const res = await getTripStatistics(params)
    const data = res.data
    
    // 更新图表数据
    if (deptChart && data.deptStats) {
      deptChart.setOption({
        xAxis: { data: data.deptStats.map(item => item.deptName) },
        series: [{ data: data.deptStats.map(item => item.count) }]
      })
    }
    
    if (monthChart && data.monthStats) {
      monthChart.setOption({
        xAxis: { data: data.monthStats.map(item => item.month) },
        series: [{ data: data.monthStats.map(item => item.totalAmount) }]
      })
    }
    
    if (userChart && data.userStats) {
      userChart.setOption({
        series: [{
          data: data.userStats.map(item => ({
            name: item.userName,
            value: item.count
          }))
        }]
      })
    }
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

onMounted(() => {
  initCharts()
  loadData()
  
  window.addEventListener('resize', () => {
    deptChart?.resize()
    monthChart?.resize()
    userChart?.resize()
  })
})
</script>

<style scoped>
.statistics {
  max-width: 1400px;
}
</style>
