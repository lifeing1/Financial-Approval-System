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
import { ref, computed, h } from 'vue'
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
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const dialog = useDialog()
const message = useMessage()

const collapsed = ref(false)
const activeKey = ref(route.path)

const currentRoute = computed(() => route.meta.title || '工作台')

// 菜单选项
const menuOptions = ref([
  {
    label: '工作台',
    key: '/dashboard',
    icon: () => h(HomeOutline)
  },
  {
    label: '出差管理',
    key: 'business-trip',
    icon: () => h(AirplaneOutline),
    children: [
      { label: '出差申请', key: '/business-trip/apply' },
      { label: '待办审批', key: '/business-trip/todo' },
      { label: '已办审批', key: '/business-trip/done' },
      { label: '我的申请', key: '/business-trip/my' }
    ]
  },
  {
    label: '备用金管理',
    key: 'petty-cash',
    icon: () => h(CashOutline),
    children: [
      { label: '备用金申请', key: '/petty-cash/apply' },
      { label: '待办审批', key: '/petty-cash/todo' },
      { label: '已办审批', key: '/petty-cash/done' },
      { label: '我的申请', key: '/petty-cash/my' }
    ]
  },
  {
    label: '流程管理',
    key: '/workflow/list',
    icon: () => h(GitNetworkOutline)
  },
  {
    label: '数据统计',
    key: 'statistics',
    icon: () => h(BarChartOutline),
    children: [
      { label: '出差统计', key: '/statistics/business-trip' },
      { label: '备用金统计', key: '/statistics/petty-cash' }
      // { label: '审批效率', key: '/statistics/efficiency' }
    ]
  },
  {
    label: '个人中心',
    key: '/profile',
    icon: () => h(PersonOutline)
  }
])

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
  router.push(key)
}

const handleUserAction = async (key) => {
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
  } else if (key === 'profile') {
    router.push('/profile')
  }
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
