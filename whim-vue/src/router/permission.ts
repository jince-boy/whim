import router from '@/router'
import { useAuthStore } from '@/stores/modules/auth.ts'
import { useUserStore } from '@/stores/modules/user.ts'
import { usePermissionStore } from '@/stores/modules/permission.ts'
import systemSetting from '@/config/SystemSetting.ts'
import { isMockEnabled } from '@/api'

// 不需要登录的白名单
const whiteList = ['/login']

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  // 切换真实后端时清除旧演示令牌，避免误判为已登录。
  if (!isMockEnabled && authStore.token?.startsWith('mock-')) authStore.logout()
  window['$loadingBar'].start()
  document.title = to.meta.title ? `${systemSetting.title}-${to.meta.title}` : systemSetting.title
  if (useAuthStore().isLogin) {
    if (to.path === '/login') {
      return '/'
    } else {
      if (!isMockEnabled) {
        // 本轮只接登录、退出，真实会话使用静态首页，不调用旧用户信息接口。
        if (!usePermissionStore().menus.length) usePermissionStore().generateMenus()
      } else if (!useUserStore().hasUserInfo) {
        await useUserStore().getUserInfo()
        usePermissionStore().generateRoutes(useUserStore().menus)
        usePermissionStore().getDynamicRoutes.forEach((route) => {
          router.addRoute('Layout', route)
        })
        usePermissionStore().generateMenus()
        return to.fullPath
      }
      return true
    }
  } else {
    if (whiteList.includes(to.path)) {
      return true
    } else {
      return {
        path: '/login',
        // 保存我们所在的位置，以便以后再来
        query: { redirect: encodeURIComponent(to.fullPath || '/') },
      }
    }
  }
})

router.afterEach(() => {
  window['$loadingBar'].finish()
})
