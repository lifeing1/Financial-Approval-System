import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { useMessage } from 'naive-ui'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的状态码为200，说明接口请求成功
    if (res.code === 200) {
      return res
    } else {
      // 其他状态码都当作错误处理
      window.$message?.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    console.error('请求错误：', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          window.$message?.error('请先登录')
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        case 403:
          window.$message?.error('权限不足')
          break
        case 404:
          window.$message?.error('请求的资源不存在')
          break
        case 500:
          window.$message?.error(data.message || '服务器错误')
          break
        default:
          window.$message?.error(data.message || '请求失败')
      }
    } else {
      window.$message?.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default request
