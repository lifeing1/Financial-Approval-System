<template>
  <div class="petty-cash-apply">
    <n-card title="备用金申请">
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="120px"
      >
        <n-grid :cols="2" :x-gap="24">
          <n-gi :span="2">
            <n-form-item label="申请事由" path="reason">
              <n-input
                v-model:value="formData.reason"
                placeholder="请输入申请事由"
                maxlength="200"
                show-count
              />
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="申请金额" path="amount">
              <n-input-number
                v-model:value="formData.amount"
                placeholder="请输入申请金额"
                :precision="2"
                :min="0"
                style="width: 100%"
              >
                <template #suffix>元</template>
              </n-input-number>
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="使用期限" path="usePeriod">
              <n-input v-model:value="formData.usePeriod" placeholder="例如：3个月" />
            </n-form-item>
          </n-gi>
          
<!--          <n-gi :span="2">-->
<!--            <n-form-item label="还款计划" path="repaymentPlan">-->
<!--              <n-input-->
<!--                v-model:value="formData.repaymentPlan"-->
<!--                type="textarea"-->
<!--                placeholder="请输入还款计划"-->
<!--                :rows="3"-->
<!--                maxlength="500"-->
<!--                show-count-->
<!--              />-->
<!--            </n-form-item>-->
<!--          </n-gi>-->
          
          <n-gi :span="2">
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
          </n-gi>
        </n-grid>
        
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NCard,
  NForm,
  NFormItem,
  NGrid,
  NGi,
  NInput,
  NInputNumber,
  NSpace,
  NButton,
  useMessage, 
  useDialog 
} from 'naive-ui'
import { saveDraft, submitApply } from '@/api/pettyCash'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const formRef = ref(null)
const draftId = ref(null)

const formData = reactive({
  reason: '',
  amount: null,
  usePeriod: '',
  repaymentPlan: '',
  remark: ''
})

const rules = {
  reason: {
    required: true,
    message: '请输入申请事由',
    trigger: 'blur'
  },
  amount: {
    required: true,
    type: 'number',
    message: '请输入申请金额',
    trigger: 'blur'
  }
}

const handleSaveDraft = async () => {
  try {
    const data = {
      id: draftId.value,
      ...formData
    }
    
    const res = await saveDraft(data)
    draftId.value = res.data
    message.success('保存成功')
  } catch (error) {
    console.error('保存失败：', error)
    message.error('保存失败，请稍后重试')
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
          if (draftId.value) {
            await submitApply(draftId.value)
            message.success('提交成功')
            router.push('/petty-cash/my')
          } else {
            message.error('保存失败，请重试')
          }
        } catch (error) {
          console.error('提交失败：', error)
          message.error('提交失败，请稍后重试')
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
    amount: null,
    usePeriod: '',
    repaymentPlan: '',
    remark: ''
  })
  draftId.value = null
}
</script>

<style scoped>
.petty-cash-apply {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
