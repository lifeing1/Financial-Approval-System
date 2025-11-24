<template>
  <div class="profile">
    <n-card title="个人信息">
      <n-descriptions label-placement="left" :column="2">
        <n-descriptions-item label="用户名">
          {{ userStore.userInfo?.username }}
        </n-descriptions-item>
        <n-descriptions-item label="姓名">
          {{ userStore.userInfo?.realName }}
        </n-descriptions-item>
        <n-descriptions-item label="部门">
          {{ userStore.userInfo?.deptName || '未分配' }}
        </n-descriptions-item>
        <n-descriptions-item label="角色">
          <n-space v-if="userStore.userInfo?.roles && userStore.userInfo.roles.length > 0">
            <n-tag 
              v-for="(role, index) in userStore.userInfo.roles" 
              :key="index" 
              type="info"
            >
              {{ role }}
            </n-tag>
          </n-space>
          <span v-else style="color: #999">未分配</span>
        </n-descriptions-item>
        <n-descriptions-item label="手机号">
          {{ userStore.userInfo?.phone }}
        </n-descriptions-item>
        <n-descriptions-item label="邮箱">
          {{ userStore.userInfo?.email }}
        </n-descriptions-item>
      </n-descriptions>
    </n-card>
    
    <n-card title="修改密码" style="margin-top: 24px">
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="100px"
        style="max-width: 500px"
      >
        <n-form-item label="原密码" path="oldPassword">
          <n-input
            v-model:value="formData.oldPassword"
            type="password"
            placeholder="请输入原密码"
          />
        </n-form-item>
        
        <n-form-item label="新密码" path="newPassword">
          <n-input
            v-model:value="formData.newPassword"
            type="password"
            placeholder="请输入新密码"
          />
        </n-form-item>
        
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input
            v-model:value="formData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
          />
        </n-form-item>
        
        <n-form-item>
          <n-button type="primary" @click="handleSubmit">
            确认修改
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NSpace,
  NTag,
  useMessage 
} from 'naive-ui'
import { changePassword } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref(null)

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: {
    required: true,
    message: '请输入原密码',
    trigger: 'blur'
  },
  newPassword: {
    required: true,
    message: '请输入新密码',
    trigger: 'blur'
  },
  confirmPassword: [
    {
      required: true,
      message: '请再次输入新密码',
      trigger: 'blur'
    },
    {
      validator: (rule, value) => {
        return value === formData.newPassword
      },
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    await changePassword({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword
    })
    message.success('密码修改成功，请重新登录', {
      duration: 2000,
      onAfterLeave: () => {
        // 清除本地登录状态
        userStore.logout()
        // 跳转到登录页
        router.push('/login')
      }
    })
  } catch (error) {
    console.error('修改密码失败：', error)
  }
}
</script>

<style scoped>
.profile {
  max-width: 1200px;
}
</style>
