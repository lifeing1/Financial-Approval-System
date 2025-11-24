<template>
  <div class="login-container">
    <n-card class="login-card" title="公司 AO 系统">
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        size="large"
      >
        <n-form-item path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>
        
        <n-form-item path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>
        
        <n-form-item>
          <n-checkbox v-model:checked="formData.rememberMe">
            记住我（七天内免登录）
          </n-checkbox>
        </n-form-item>
        
        <n-form-item>
          <n-button
            type="primary"
            block
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </n-button>
        </n-form-item>
      </n-form>
      
      <div class="login-tip">
        <p>测试账号：</p>
        <p>超级管理员：admin / 123456</p>
        <p>部门负责人：zhangsan / 123456</p>
        <p>普通员工：wangwu / 123456</p>
      </div>
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
  NInput, 
  NButton, 
  NCheckbox, 
  NIcon,
  useMessage 
} from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { login, getUserInfo, getUserMenus } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  }
}

const handleLogin = async () => {
  if (loading.value) return
  
  try {
    // 表单验证
    if (formRef.value) {
      await formRef.value.validate((errors) => {
        if (errors) {
          return
        }
      })
    }
    
    loading.value = true
    
    // 登录
    const loginRes = await login(formData)
    if (!loginRes || !loginRes.data || !loginRes.data.token) {
      message.error('登录失败，请重试')
      return
    }
    
    userStore.setToken(loginRes.data.token, formData.rememberMe)
    
    // 获取用户信息
    const userInfoRes = await getUserInfo()
    userStore.setUserInfo(userInfoRes.data)
    
    // 获取菜单
    const menusRes = await getUserMenus()
    userStore.setMenus(menusRes.data)
    
    message.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败：', error)
    if (error.response) {
      message.error(error.response.data?.message || '登录失败')
    } else if (error.message) {
      message.error(error.message)
    } else {
      message.error('登录失败，请检查用户名密码')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-tip {
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  line-height: 1.8;
}

.login-tip p {
  margin: 0;
}
</style>
