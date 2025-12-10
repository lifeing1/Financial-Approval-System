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
                value-format="yyyy-MM-dd"
                placeholder="请选择开始日期"
                style="width: 100%"
                clearable
                @update:value="handleStartDateChange"
              />
            </n-form-item>
          </n-gi>
          
          <n-gi>
            <n-form-item label="结束日期" path="endDate">
              <n-date-picker
                v-model:value="formData.endDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择结束日期"
                style="width: 100%"
                clearable
                @update:value="handleEndDateChange"
              />
            </n-form-item>
          </n-gi>
        </n-grid>
        
        <n-divider>费用明细</n-divider>
        
        <n-dynamic-input
          v-model:value="formData.expenses"
          :on-create="onCreateExpense"
        >
          <template #default="{ value, index }">
            <div class="expense-item">
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
                  >
                    <template #suffix>
                      <span style="color: #999;">元</span>
                    </template>
                  </n-input-number>
                </n-gi>
                <n-gi>
                  <n-input
                    v-model:value="value.remark"
                    placeholder="备注说明"
                  />
                </n-gi>
              </n-grid>
              
              <!-- 文件上传区域 -->
              <div class="upload-section">
                <div class="upload-label">
                  <n-icon :component="AttachIcon" :size="16" style="margin-right: 4px;" />
                  附件上传
                </div>
                <n-upload
                  :custom-request="(options) => handleCustomUpload(options, index)"
                  :max="3"
                  :show-file-list="false"
                  accept="image/*,.pdf"
                  @before-upload="handleBeforeUpload"
                >
                  <n-upload-dragger class="upload-dragger">
                    <div class="upload-content">
                      <n-icon :component="CloudUploadIcon" :size="32" class="upload-icon" />
                      <div class="upload-text">
                        <p class="upload-hint">点击或拖拽文件到此区域上传</p>
                        <p class="upload-desc">支持 JPG、PNG、PDF 格式，单个文件不超过 10MB，最多 3 个文件</p>
                      </div>
                    </div>
                  </n-upload-dragger>
                </n-upload>
                
                <!-- 已上传文件列表 -->
                <div v-if="value.attachments && value.attachments.length > 0" class="file-list">
                  <div
                    v-for="(fileObj, idx) in value.attachments"
                    :key="idx"
                    class="file-item"
                  >
                    <!-- 文件预览（图片） -->
                    <div v-if="isImage(fileObj.name)" class="file-preview">
                      <img :src="getFilePreviewUrl(fileObj.file)" :alt="fileObj.name" />
                    </div>
                    <!-- PDF图标 -->
                    <div v-else class="file-preview pdf-icon">
                      <n-icon :component="DocumentIcon" :size="32" />
                    </div>
                    
                    <div class="file-info">
                      <span class="file-name">{{ fileObj.name }}</span>
                      <span class="file-size">{{ formatFileSize(fileObj.size) }}</span>
                    </div>
                    
                    <div class="file-actions">
                      <n-button
                        text
                        type="primary"
                        size="small"
                        @click="previewFile(fileObj)"
                        class="file-preview-btn"
                      >
                        <template #icon>
                          <n-icon :component="EyeIcon" :size="16" />
                        </template>
                        预览
                      </n-button>
                      <n-button
                        text
                        type="error"
                        size="small"
                        @click="confirmDelete(index, idx)"
                        class="file-delete"
                      >
                        <template #icon>
                          <n-icon :component="DeleteIcon" :size="16" />
                        </template>
                        删除
                      </n-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
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
    
    <!-- 图片预览弹窗 -->
    <n-modal
      v-model:show="showPreview"
      preset="card"
      :title="previewFileInfo?.name || '文件预览'"
      style="width: 80%; max-width: 1000px;"
    >
      <div v-if="previewType === 'image' && previewFileInfo" style="text-align: center;">
        <img
          :src="getFilePreviewUrl(previewFileInfo.file)"
          :alt="previewFileInfo.name"
          style="max-width: 100%; max-height: 70vh; object-fit: contain;"
        />
      </div>
      <template #footer>
        <div style="text-align: right;">
          <n-button @click="closePreview">关闭</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
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
  NUpload,
  NUploadDragger,
  NIcon,
  NModal,
  NImage,
  useMessage, 
  useDialog 
} from 'naive-ui'
import { 
  CloudUploadOutline as CloudUploadIcon,
  DocumentAttachOutline as AttachIcon,
  DocumentOutline as FileIcon,
  DocumentTextOutline as DocumentIcon,
  EyeOutline as EyeIcon,
  TrashOutline as DeleteIcon
} from '@vicons/ionicons5'
import { saveDraft, submitApply } from '@/api/businessTrip'
import { uploadToOss } from '@/api/file'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const formRef = ref(null)
const draftId = ref(null)

// 文件预览相关
const showPreview = ref(false)
const previewFileInfo = ref(null)
const previewType = ref('') // 'image' or 'pdf'

const formData = reactive({
  reason: '',
  startPlace: '',
  destination: '',
  startDate: null,
  endDate: null,
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
  remark: '',
  attachments: []  // 存储文件对象 { name, size, file }
})

const totalAmount = computed(() => {
  return formData.expenses.reduce((sum, item) => sum + (item.amount || 0), 0)
})

// 自定义文件上传（本地暂存）
const handleCustomUpload = ({ file, onFinish, onError }, index) => {
  try {
    // 检查文件数量限制
    if (formData.expenses[index].attachments.length >= 3) {
      message.error('最多只能上传3个文件')
      onError()
      return
    }
    
    // 本地暂存文件对象
    const fileObj = {
      name: file.file.name,
      size: file.file.size,
      file: file.file  // 保存原始File对象
    }
    
    if (!formData.expenses[index].attachments) {
      formData.expenses[index].attachments = []
    }
    
    formData.expenses[index].attachments.push(fileObj)
    message.success(`文件 ${fileObj.name} 已添加，将在提交时上传`)
    onFinish()
  } catch (error) {
    console.error('文件添加失败:', error)
    message.error('文件添加失败')
    onError()
  }
}

// 文件上传前验证
const handleBeforeUpload = (data) => {
  const file = data.file.file
  const fileSize = file.size / 1024 / 1024 // MB
  
  if (fileSize > 10) {
    message.error('文件大小不能超过10MB')
    return false
  }
  
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf']
  if (!allowedTypes.includes(file.type)) {
    message.error('只允许上传图片（jpg/jpeg/png）或PDF文件')
    return false
  }
  
  return true
}

// 移除附件（本地删除）
const removeAttachment = (expenseIndex, attachmentIndex) => {
  formData.expenses[expenseIndex].attachments.splice(attachmentIndex, 1)
  message.success('文件已移除')
}

// 确认删除附件
const confirmDelete = (expenseIndex, attachmentIndex) => {
  const fileObj = formData.expenses[expenseIndex].attachments[attachmentIndex]
  dialog.warning({
    title: '删除确认',
    content: `确定要删除文件 "${fileObj.name}" 吗？`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: () => {
      removeAttachment(expenseIndex, attachmentIndex)
    }
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 判断是否为图片文件
const isImage = (filename) => {
  const ext = filename.toLowerCase().split('.').pop()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
}

// 获取文件预览URL
const getFilePreviewUrl = (file) => {
  return URL.createObjectURL(file)
}

// 预览文件
const previewFile = (fileObj) => {
  previewFileInfo.value = fileObj
  
  if (isImage(fileObj.name)) {
    previewType.value = 'image'
    showPreview.value = true
  } else if (fileObj.name.toLowerCase().endsWith('.pdf')) {
    // PDF在新窗口打开
    const url = getFilePreviewUrl(fileObj.file)
    window.open(url, '_blank')
  }
}

// 关闭预览
const closePreview = () => {
  showPreview.value = false
  previewFileInfo.value = null
  previewType.value = ''
}

// 上传所有文件到OSS（已废弃，不再使用）
/*
const uploadAllFilesToOss = async () => {
  const uploadPromises = []
  
  for (let i = 0; i < formData.expenses.length; i++) {
    const expense = formData.expenses[i]
    if (expense.attachments && expense.attachments.length > 0) {
      // 为每个费用明细上传文件
      const fileUploadPromises = expense.attachments.map(async (fileObj) => {
        try {
          const res = await uploadToOss(fileObj.file, 'expense/')
          // 返回文件名和URL的映射
          return {
            fileName: fileObj.name,
            url: res.data
          }
        } catch (error) {
          console.error(`文件 ${fileObj.name} 上传失败:`, error)
          throw error
        }
      })
      
      uploadPromises.push(
        Promise.all(fileUploadPromises).then(fileInfos => ({ index: i, fileInfos }))
      )
    }
  }
  
  // 等待所有文件上传完成
  const results = await Promise.all(uploadPromises)
  
  // 将上传后的文件信息保存到对应的费用明细
  results.forEach(({ index, fileInfos }) => {
    formData.expenses[index].uploadedUrls = fileInfos
  })
}
*/

// 处理开始日期变化
const handleStartDateChange = (value) => {
  if (value && formData.endDate) {
    const startDate = new Date(value)
    const endDate = new Date(formData.endDate)
    if (startDate > endDate) {
      message.warning('开始日期不能晚于结束日期')
      formData.startDate = null
    }
  }
}

// 处理结束日期变化
const handleEndDateChange = (value) => {
  if (value && formData.startDate) {
    const startDate = new Date(formData.startDate)
    const endDate = new Date(value)
    if (endDate < startDate) {
      message.warning('结束日期不能早于开始日期，请重新选择')
      formData.endDate = null
    }
  }
}

// 格式化日期为 yyyy-MM-dd
const formatDate = (timestamp) => {
  if (!timestamp) return null
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const handleSaveDraft = async () => {
  try {
    const data = {
      id: draftId.value,
      ...formData,
      transportModes: formData.transportModes.join(','),
      startDate: formatDate(formData.startDate),
      endDate: formatDate(formData.endDate),
      expenses: formData.expenses.map(expense => ({
        expenseType: expense.expenseType,
        amount: expense.amount,
        remark: expense.remark,
        // 不上传附件到OSS，attachments保持为空数组
        attachments: []
      }))
    }
    
    const res = await saveDraft(data)
    draftId.value = res.data
    message.success('保存成功')
  } catch (error) {
    console.error('保存失败：', error)
    message.error('保存失败：' + (error.message || '请稍后重试'))
    throw error; // 抛出错误以便上层捕获
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
          // 显示加载中
          const hasFiles = formData.expenses.some(exp => exp.attachments && exp.attachments.length > 0)
          const loadingMsg = message.loading(hasFiles ? '正在上传文件并提交...' : '正在提交...', { duration: 0 })
          
          try {
            // 准备提交数据
            const submitData = {
              id: draftId.value,
              reason: formData.reason,
              startPlace: formData.startPlace,
              destination: formData.destination,
              startDate: formatDate(formData.startDate),
              endDate: formatDate(formData.endDate),
              transportModes: formData.transportModes.join(','),
              remark: formData.remark,
              expenses: []
            }
            
            // 处理费用明细和附件
            for (const expense of formData.expenses) {
              const expenseData = {
                expenseType: expense.expenseType,
                amount: expense.amount,
                remark: expense.remark,
                attachments: []
              }
              
              // 上传附件
              if (expense.attachments && expense.attachments.length > 0) {
                for (const attachment of expense.attachments) {
                  try {
                    const ossResult = await uploadToOss(attachment.file)
                    expenseData.attachments.push({
                      fileName: attachment.name,
                      url: ossResult.data
                    })
                  } catch (uploadError) {
                    loadingMsg.destroy()
                    message.error(`文件 ${attachment.name} 上传失败`)
                    throw uploadError
                  }
                }
              }
              
              submitData.expenses.push(expenseData)
            }
            
            // 直接提交申请，传入完整的表单数据
            await submitApply(submitData)
            loadingMsg.destroy()
            message.success('提交成功')
            router.push('/business-trip/my')
          } catch (error) {
            loadingMsg.destroy()
            throw error
          }
        } catch (error) {
          console.error('提交失败：', error)
          message.error('提交失败：' + (error.message || '请稍后重试'))
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
    startDate: null,
    endDate: null,
    transportModes: [],
    expenses: [],
    remark: ''
  })
  draftId.value = null
}
</script>

<style scoped>
.business-trip-apply {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.expense-item {
  width: 100%;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.upload-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e0e0e6;
}

.upload-label {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.upload-dragger {
  width: 100%;
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
  background: #fff;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-dragger:hover {
  border-color: #18a058;
  background: #f0f9ff;
}

.upload-content {
  padding: 24px 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.upload-icon {
  color: #18a058;
  flex-shrink: 0;
}

.upload-text {
  text-align: left;
  flex: 1;
}

.upload-hint {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.upload-desc {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

.file-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fff;
  border: 1px solid #e0e0e6;
  border-radius: 6px;
  transition: all 0.3s;
  gap: 12px;
}

.file-item:hover {
  border-color: #18a058;
  box-shadow: 0 2px 8px rgba(24, 160, 88, 0.1);
}

.file-preview {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-preview.pdf-icon {
  color: #d03050;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.file-icon {
  color: #18a058;
  margin-right: 8px;
  flex-shrink: 0;
}

.file-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-actions {
  display: flex;
  gap: 20px;
  margin-left: auto;
  flex-shrink: 0;
}

.file-preview-btn {
  color: #18a058;
}

.file-preview-btn:hover {
  background-color: rgba(24, 160, 88, 0.1);
}

.file-delete {
  flex-shrink: 0;
}
</style>
