import router from '@/router'
import { useAuthStore } from '@/stores/modules/auth'
import { useUserStore } from '@/stores/modules/user'
import { useTabStore } from '@/stores/modules/tab'
import systemSetting from '@/config/SystemSetting'

/** 登录后只初始化当前用户资料，个人信息页作为默认入口。 */
router.beforeEach(async (to) => {
  window.$loadingBar?.start()
  const auth = useAuthStore()
  const user = useUserStore()
  if (!auth.isLogin)
    return to.path === '/login'
      ? true
      : { path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } }
  const token = auth.getToken
  try {
    if (!user.hasUserInfo) await user.getUserInfo()
    if (!auth.isLogin) return '/login'
  } catch {
    // 旧会话的请求失败不能清理新账号的登录状态。
    if (auth.getToken !== token) return false
    // 用户资料和 HTTP 错误已在数据层提示。
    auth.logout()
    return {
      path: '/login',
      query: {
        redirect: to.path === '/login' ? to.query.redirect : encodeURIComponent(to.fullPath),
      },
    }
  }
  return to.path === '/login' || to.path === '/' ? '/profile' : true
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
