<template>
  <div class="statistics">
    <n-card title="备用金统计">
      <n-space vertical :size="24">
        <n-space>
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            clearable
            @update:value="loadData"
          />
        </n-space>
        
        <n-grid :cols="2" :x-gap="24">
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
import { getCashStatistics } from '@/api/statistics'

const dateRange = ref(null)
const deptChartRef = ref(null)
const monthChartRef = ref(null)

let deptChart = null
let monthChart = null

const initCharts = () => {
  nextTick(() => {
    if (deptChartRef.value) {
      deptChart = echarts.init(deptChartRef.value)
      deptChart.setOption({
        color: ['#18A058', '#36AD6A'],
        tooltip: { trigger: 'axis' },
        legend: { data: ['申请金额', '已批准金额'] },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [
          { name: '申请金额', type: 'bar', data: [] },
          { name: '已批准金额', type: 'bar', data: [] }
        ]
      })
    }
    
    if (monthChartRef.value) {
      monthChart = echarts.init(monthChartRef.value)
      monthChart.setOption({
        color: ['#18A058', '#36AD6A'],
        tooltip: { trigger: 'axis' },
        legend: { data: ['申请金额', '已批准金额'] },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [
          { name: '申请金额', type: 'line', data: [] },
          { name: '已批准金额', type: 'line', data: [] }
        ]
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
    
    const res = await getCashStatistics(params)
    const data = res.data
    
    if (deptChart && data.deptStats) {
      deptChart.setOption({
        xAxis: { data: data.deptStats.map(item => item.deptName) },
        series: [
          { data: data.deptStats.map(item => item.applyAmount) },
          { data: data.deptStats.map(item => item.approvedAmount) }
        ]
      })
    }
    
    if (monthChart && data.monthStats) {
      monthChart.setOption({
        xAxis: { data: data.monthStats.map(item => item.month) },
        series: [
          { data: data.monthStats.map(item => item.applyAmount) },
          { data: data.monthStats.map(item => item.approvedAmount) }
        ]
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
  })
})
</script>

<style scoped>
.statistics {
  max-width: 1400px;
}
</style>
