import router from '@/router'
import { useAuthStore } from '@/stores/modules/auth'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'
import { useTabStore } from '@/stores/modules/tab'
import systemSetting from '@/config/SystemSetting'
import { isMockEnabled } from '@/api'

/** 用户与导航完成初始化后重新匹配深链接，错误不能留下半套动态路由。 */
router.beforeEach(async (to) => {
  window.$loadingBar?.start()
  const auth = useAuthStore()
  const user = useUserStore()
  const navigation = usePermissionStore()
  if (!isMockEnabled && auth.token?.startsWith('mock-')) auth.logout()
  if (!auth.isLogin)
    return to.path === '/login'
      ? true
      : { path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } }
  const token = auth.getToken
  try {
    if (!user.hasUserInfo) await user.getUserInfo()
    if (!auth.isLogin) return '/login'
    if (!navigation.initialized) {
      await navigation.loadMenus()
      if (!auth.isLogin) return '/login'
      if (!navigation.initialized) return false
      return to.fullPath
    }
  } catch (error) {
    // 已取消的旧会话请求不能清理新账号已经加载的导航。
    if (auth.getToken !== token) return false
    if (error instanceof Error) window.$message?.error(error.message)
    auth.logout()
    return { path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } }
  }
  return to.path === '/login' || to.path === '/' ? navigation.homePath : true
})

/** 标签激活、浏览器标题和进度条在导航真正完成后统一更新。 */
router.afterEach((to, _from, failure) => {
  window.$loadingBar?.finish()
  if (failure) return
  document.title = to.meta.title ? `${systemSetting.title}-${to.meta.title}` : systemSetting.title
  if (to.meta.isTab) useTabStore().visitRoute(to)
})
/** 懒加载或路由错误也必须结束进度提示。 */
router.onError(() => window.$loadingBar?.error())
