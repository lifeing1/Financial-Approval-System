import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 或 sessionStorage 中获取 token
  const getStoredToken = () => {
    return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
  }
  
  const token = ref(getStoredToken())
  const userInfo = ref(null)
  const menus = ref([])

  const setToken = (newToken, remember = false) => {
    token.value = newToken
    // 根据是否记住我选择存储位置
    if (remember) {
      // 记住我：使用 localStorage（持久化）
      localStorage.setItem('token', newToken)
      sessionStorage.removeItem('token')
    } else {
      // 不记住：使用 sessionStorage（关闭浏览器即清除）
      sessionStorage.setItem('token', newToken)
      localStorage.removeItem('token')
    }
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const setMenus = (menuList) => {
    menus.value = menuList
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    menus.value = []
    // 清除所有存储的 token
    localStorage.removeItem('token')
    sessionStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    menus,
    setToken,
    setUserInfo,
    setMenus,
    logout
  }
})
