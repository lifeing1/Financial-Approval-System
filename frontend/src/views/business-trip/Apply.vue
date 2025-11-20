<template>
  <div class="business-trip-apply">
    <n-card title="出差申请">
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="120px"
      >
        <n-grid :cols="2" :x-gap="24">
          <n-gi>
            <n-form-item label="出差事由" path="reason">
              <n-input
                v-model:value="formData.reason"
                placeholder="请输入出差事由"
                maxlength="200"
                show-count
              />
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="交通方式" path="transportModes">
              <n-checkbox-group v-model:value="formData.transportModes">
                <n-space>
                  <n-checkbox value="飞机" label="飞机" />
                  <n-checkbox value="高铁" label="高铁" />
                  <n-checkbox value="火车" label="火车" />
                  <n-checkbox value="汽车" label="汽车" />
                  <n-checkbox value="其他" label="其他" />
                </n-space>
              </n-checkbox-group>
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="出发地" path="startPlace">
              <n-input v-model:value="formData.startPlace" placeholder="请输入出发地" />
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="目的地" path="destination">
              <n-input v-model:value="formData.destination" placeholder="请输入目的地" />
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="开始日期" path="startDate">
              <n-date-picker
                v-model:value="formData.startDate"
                type="date"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                placeholder="请选择开始日期"
                :is-date-disabled="isStartDateDisabled"
                @update:value="handleStartDateChange"
                style="width: 100%"
              />
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="结束日期" path="endDate">
              <n-date-picker
                v-model:value="formData.endDate"
                type="date"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                placeholder="请选择结束日期"
                :is-date-disabled="isEndDateDisabled"
                @update:value="handleEndDateChange"
                style="width: 100%"
              />
            </n-form-item>
          </n-gi>
        </n-grid>
        
        <n-divider>费用明细</n-divider>
        
        <n-dynamic-input
          v-model:value="formData.expenses"
          :on-create="onCreateExpense"
        >
          <template #default="{ value }">
            <n-grid :cols="3" :x-gap="12">
              <n-gi>
                <n-select
                  v-model:value="value.expenseType"
                  :options="expenseTypeOptions"
                  placeholder="费用类型"
                />
              </n-gi>
              <n-gi>
                <n-input-number
                  v-model:value="value.amount"
                  placeholder="金额"
                  :precision="2"
                  :min="0"
                  style="width: 100%"
                />
              </n-gi>
              <n-gi>
                <n-input
                  v-model:value="value.remark"
                  placeholder="备注"
                />
              </n-gi>
            </n-grid>
          </template>
        </n-dynamic-input>
        
        <n-form-item label="合计金额" label-width="120px">
          <n-input-number
            :value="totalAmount"
            readonly
            :precision="2"
            style="width: 200px"
          >
            <template #suffix>元</template>
          </n-input-number>
        </n-form-item>
        
        <n-form-item label="备注">
          <n-input
            v-model:value="formData.remark"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
            maxlength="500"
            show-count
          />
        </n-form-item>
        
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSubmit">
              提交审批
            </n-button>
            <n-button @click="handleSaveDraft">
              保存草稿
            </n-button>
            <n-button @click="handleReset">
              重置
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NCard,
  NForm,
  NFormItem,
  NGrid,
  NGi,
  NInput,
  NCheckboxGroup,
  NCheckbox,
  NSpace,
  NDatePicker,
  NDivider,
  NDynamicInput,
  NSelect,
  NInputNumber,
  NButton,
  useMessage, 
  useDialog 
} from 'naive-ui'
import { saveDraft, submitApply } from '@/api/businessTrip'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const formRef = ref(null)
const draftId = ref(null)

const formData = reactive({
  reason: '',
  startPlace: '',
  destination: '',
  startDate: '',
  endDate: '',
  transportModes: [],
  expenses: [],
  remark: ''
})

const rules = {
  reason: {
    required: true,
    message: '请输入出差事由',
    trigger: 'blur'
  },
  startPlace: {
    required: true,
    message: '请输入出发地',
    trigger: 'blur'
  },
  destination: {
    required: true,
    message: '请输入目的地',
    trigger: 'blur'
  },
  startDate: [
    {
      required: true,
      message: '请选择开始日期',
      trigger: ['blur', 'change'],
      validator: (rule, value) => {
        if (!value || value === '') {
          return new Error('请选择开始日期')
        }
        if (formData.endDate && value > formData.endDate) {
          return new Error('开始日期不能晚于结束日期')
        }
        return true
      }
    }
  ],
  endDate: [
    {
      required: true,
      message: '请选择结束日期',
      trigger: ['blur', 'change'],
      validator: (rule, value) => {
        if (!value || value === '') {
          return new Error('请选择结束日期')
        }
        if (formData.startDate && value < formData.startDate) {
          return new Error('结束日期不能早于开始日期')
        }
        return true
      }
    }
  ]
}

const expenseTypeOptions = [
  { label: '交通费', value: '交通费' },
  { label: '住宿费', value: '住宿费' },
  { label: '补贴费', value: '补贴费' },
  { label: '其他费用', value: '其他费用' }
]

const onCreateExpense = () => ({
  expenseType: '',
  amount: 0,
  remark: ''
})

const totalAmount = computed(() => {
  return formData.expenses.reduce((sum, item) => sum + (item.amount || 0), 0)
})

// 禁用开始日期：不能选择晚于结束日期的日期
const isStartDateDisabled = (ts) => {
  if (!formData.endDate) return false
  const endTime = new Date(formData.endDate).getTime()
  // 设置为当天的结束时间 23:59:59
  const endOfDay = new Date(formData.endDate)
  endOfDay.setHours(23, 59, 59, 999)
  return ts > endOfDay.getTime()
}

// 禁用结束日期：不能选择早于开始日期的日期
const isEndDateDisabled = (ts) => {
  if (!formData.startDate) return false
  const startTime = new Date(formData.startDate).getTime()
  // 设置为当天的开始时间 00:00:00
  const startOfDay = new Date(formData.startDate)
  startOfDay.setHours(0, 0, 0, 0)
  return ts < startOfDay.getTime()
}

// 处理开始日期变化
const handleStartDateChange = (value) => {
  if (value && formData.endDate && value > formData.endDate) {
    message.warning('开始日期不能晚于结束日期')
    formData.startDate = ''
  }
}

// 处理结束日期变化
const handleEndDateChange = (value) => {
  if (value && formData.startDate && value < formData.startDate) {
    message.warning('结束日期不能早于开始日期')
    formData.endDate = ''
  }
}

const handleSaveDraft = async () => {
  try {
    const data = {
      id: draftId.value,
      ...formData,
      transportModes: formData.transportModes.join(','),
      startDate: formData.startDate ? new Date(formData.startDate).toISOString().split('T')[0] : null,
      endDate: formData.endDate ? new Date(formData.endDate).toISOString().split('T')[0] : null
    }
    
    const res = await saveDraft(data)
    draftId.value = res.data
    message.success('保存成功')
  } catch (error) {
    console.error('保存失败：', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    dialog.warning({
      title: '提交确认',
      content: '确定要提交审批吗？提交后不可修改',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          // 先保存
          await handleSaveDraft()
          
          // 再提交
          await submitApply(draftId.value)
          message.success('提交成功')
          router.push('/business-trip/my')
        } catch (error) {
          console.error('提交失败：', error)
        }
      }
    })
  } catch (error) {
    message.error('请完善表单信息')
  }
}

const handleReset = () => {
  formRef.value?.restoreValidation()
  Object.assign(formData, {
    reason: '',
    startPlace: '',
    destination: '',
    startDate: '',
    endDate: '',
    transportModes: [],
    expenses: [],
    remark: ''
  })
  draftId.value = null
}
</script>

<style scoped>
.business-trip-apply {
  max-width: 1200px;
}
</style>
