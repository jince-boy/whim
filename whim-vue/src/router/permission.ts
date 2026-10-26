import { isAxiosError } from 'axios'
import router from '@/router'
import { useAuthStore } from '@/stores/modules/auth'
import { useUserStore } from '@/stores/modules/user'
import { useTabStore } from '@/stores/modules/tab'
import { usePermissionStore } from '@/stores/modules/permission'
import { loadingBar, message } from '@/plugins'
import systemSetting from '@/config/SystemSetting'

/** 登录和页面刷新共用初始化流程，路由就绪后才进入业务页面。 */
router.beforeEach(async (to) => {
  loadingBar.start()
  const auth = useAuthStore()
  const user = useUserStore()
  const permission = usePermissionStore()
  if (!auth.isLogin)
    return to.path === '/login'
      ? true
      : { path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } }
  const token = auth.getToken
  let routesAdded = false
  try {
    if (!user.hasUserInfo) await user.getUserInfo()
    if (auth.getToken !== token || !user.hasUserInfo) return false
    if (!permission.initialized) {
      await permission.loadMenus()
      routesAdded = true
    }
    if (auth.getToken !== token || !permission.initialized) return false
  } catch (error) {
    // 旧会话的请求失败不能清理新账号的登录状态。
    if (auth.getToken !== token) return false
    // HTTP 错误由拦截器提示，菜单配置和业务初始化错误在此提示。
    if (!isAxiosError(error))
      message.error(error instanceof Error ? error.message : '登录初始化失败，请重试')
    auth.logout()
    return {
      path: '/login',
      query: {
        redirect: to.path === '/login' ? to.query.redirect : encodeURIComponent(to.fullPath),
      },
    }
  }
  if (to.path === '/login' || to.path === '/') {
    const home = router.resolve(permission.homePath)
    return { path: home.path, query: home.query, hash: home.hash, replace: true }
  }
  // 初次刷新时可能已匹配到 404；按地址重新匹配，不携带旧路由名称。
  if (routesAdded) return { path: to.path, query: to.query, hash: to.hash, replace: true }
  return true
})

/** 标签激活、浏览器标题和进度条在导航真正完成后统一更新。 */
router.afterEach((to, _from, failure) => {
  loadingBar.finish()
  if (failure) return
  document.title = to.meta.title ? `${systemSetting.title}-${to.meta.title}` : systemSetting.title
  if (to.meta.isTab) useTabStore().visitRoute(to)
})
/** 懒加载或路由错误也必须结束进度提示。 */
router.onError(() => loadingBar.error())
