<template>
  <n-layout has-sider style="height: 100vh">
    <!-- 侧边栏 -->
    <n-layout-sider
      bordered
      show-trigger
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <h2 v-if="!collapsed">AO 系统</h2>
        <h2 v-else>AO</h2>
      </div>
      
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>
    
    <!-- 主内容区 -->
    <n-layout>
      <!-- 顶部栏 -->
      <n-layout-header bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;">
        <n-breadcrumb>
          <n-breadcrumb-item>{{ currentRoute }}</n-breadcrumb-item>
        </n-breadcrumb>
        
        <n-space>
          <n-dropdown :options="userOptions" @select="handleUserAction">
            <n-button text>
              <template #icon>
                <n-icon :component="PersonCircleOutline" />
              </template>
              {{ userStore.userInfo?.realName }}
            </n-button>
          </n-dropdown>
        </n-space>
      </n-layout-header>
      
      <!-- 内容区 -->
      <n-layout-content
        content-style="padding: 24px; min-height: calc(100vh - 64px);"
        :native-scrollbar="false"
      >
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NBreadcrumb,
  NBreadcrumbItem,
  NSpace,
  NDropdown,
  NButton,
  NIcon,
  useDialog, 
  useMessage 
} from 'naive-ui'
import { 
  PersonCircleOutline, 
  LogOutOutline,
  HomeOutline,
  AirplaneOutline,
  CashOutline,
  GitNetworkOutline,
  BarChartOutline,
  PersonOutline
} from '@vicons/ionicons5'
import { logout, getUserMenus } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const dialog = useDialog()
const message = useMessage()

const collapsed = ref(false)
const activeKey = ref(route.path)

const currentRoute = computed(() => route.meta.title || '工作台')

// 图标映射
const iconMap = {
  'home': HomeOutline,
  'airplane': AirplaneOutline,
  'cash': CashOutline,
  'git-network': GitNetworkOutline,
  'bar-chart': BarChartOutline,
  'person': PersonOutline
}

// 菜单选项
const menuOptions = ref([])

// 从后端菜单数据构建前端菜单选项
const buildMenuOptions = (menus) => {
  if (!menus || menus.length === 0) return []
  
  return menus.map(menu => {
    // 确保菜单项有有效的key
    let menuKey = menu.path || menu.menuCode || `menu_${menu.id}`
    // 处理可能的无效路径
    if (!menuKey || menuKey === 'null') {
      menuKey = `menu_${menu.id}`
    }
    
    const option = {
      label: menu.menuName,
      key: menuKey,
      icon: menu.icon ? () => h(iconMap[menu.icon] || HomeOutline) : undefined
    }
    
    // 如果有子菜单，递归构建
    if (menu.children && menu.children.length > 0) {
      option.children = buildMenuOptions(menu.children)
    }
    
    return option
  })
}

// 加载用户菜单
const loadMenus = async () => {
  try {
    // 如果 store 中已有菜单，直接使用
    if (userStore.menus && userStore.menus.length > 0) {
      menuOptions.value = buildMenuOptions(userStore.menus)
      return
    }
    
    // 否则从后端获取
    const res = await getUserMenus()
    if (res.data) {
      userStore.setMenus(res.data)
      menuOptions.value = buildMenuOptions(res.data)
    }
  } catch (error) {
    console.error('加载菜单失败：', error)
    message.error('加载菜单失败')
  }
}

// 组件挂载时加载菜单
onMounted(() => {
  loadMenus()
})

// 用户下拉菜单
const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: () => h(PersonOutline)
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(LogOutOutline)
  }
]

const handleMenuSelect = (key) => {
  activeKey.value = key
  
  // 检查key是否有效
  if (!key || key === 'null') {
    console.error('Invalid menu key:', key)
    return
  }
  
  // 处理特殊菜单项
  if (key === 'logout') {
    dialog.warning({
      title: '退出登录',
      content: '确定要退出登录吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await logout()
          userStore.logout()
          message.success('已退出登录')
          router.push('/login')
        } catch (error) {
          console.error('退出登录失败：', error)
        }
      }
    })
    return
  }
  
  if (key === 'profile') {
    router.push('/profile')
    return
  }
  
  // 对于其他菜单项，验证路由后导航
  try {
    // 检查路由是否存在
    const resolvedRoute = router.resolve(key)
    if (resolvedRoute.matched.length > 0) {
      router.push(key).catch(err => {
        console.error('Navigation error:', err)
        message.error('页面跳转失败')
      })
    } else {
      console.error('No matching route found for key:', key)
      message.error('找不到对应的页面')
    }
  } catch (err) {
    console.error('Route resolution error:', err)
    message.error('页面跳转失败')
  }
}

const handleUserAction = async (key) => {
  handleMenuSelect(key)
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
  color: #18A058;
}

.logo h2 {
  margin: 0;
  font-weight: bold;
}
</style>