import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: '/business-trip/apply',
        name: 'BusinessTripApply',
        component: () => import('@/views/business-trip/Apply.vue'),
        meta: { title: '出差申请' }
      },
      {
        path: '/business-trip/todo',
        name: 'BusinessTripTodo',
        component: () => import('@/views/business-trip/TodoList.vue'),
        meta: { title: '待办审批' }
      },
      {
        path: '/business-trip/done',
        name: 'BusinessTripDone',
        component: () => import('@/views/business-trip/DoneList.vue'),
        meta: { title: '已办审批' }
      },
      {
        path: '/business-trip/my',
        name: 'BusinessTripMy',
        component: () => import('@/views/business-trip/MyList.vue'),
        meta: { title: '我的申请' }
      },
      {
        path: '/petty-cash/apply',
        name: 'PettyCashApply',
        component: () => import('@/views/petty-cash/Apply.vue'),
        meta: { title: '备用金申请' }
      },
      {
        path: '/petty-cash/todo',
        name: 'PettyCashTodo',
        component: () => import('@/views/petty-cash/TodoList.vue'),
        meta: { title: '待办审批' }
      },
      {
        path: '/petty-cash/done',
        name: 'PettyCashDone',
        component: () => import('@/views/petty-cash/DoneList.vue'),
        meta: { title: '已办审批' }
      },
      {
        path: '/petty-cash/my',
        name: 'PettyCashMy',
        component: () => import('@/views/petty-cash/MyList.vue'),
        meta: { title: '我的申请' }
      },
      {
        path: '/workflow/list',
        name: 'WorkflowList',
        component: () => import('@/views/workflow/List.vue'),
        meta: { title: '流程管理', permission: 'admin' }
      },
      {
        path: '/statistics/business-trip',
        name: 'BusinessTripStats',
        component: () => import('@/views/statistics/BusinessTrip.vue'),
        meta: { title: '出差统计' }
      },
      {
        path: '/statistics/petty-cash',
        name: 'PettyCashStats',
        component: () => import('@/views/statistics/PettyCash.vue'),
        meta: { title: '备用金统计' }
      },
      {
        path: '/statistics/efficiency',
        name: 'EfficiencyStats',
        component: () => import('@/views/statistics/Efficiency.vue'),
        meta: { title: '审批效率' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 如果访问需要登录的页面
  if (to.meta.requiresAuth !== false) {
    if (!userStore.token) {
      // 没有token，跳转登录页
      next('/login')
      return
    }
    
    // 有token但没有用户信息，需要获取用户信息（刷新页面的情况）
    if (!userStore.userInfo) {
      try {
        const res = await getUserInfo()
        userStore.setUserInfo(res.data)
        next()
      } catch (error) {
        // 获取用户信息失败，可能token已过期
        console.error('获取用户信息失败：', error)
        userStore.logout()
        next('/login')
      }
    } else {
      next()
    }
  } else if (to.path === '/login' && userStore.token) {
    // 已登录用户访问登录页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router
