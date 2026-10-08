import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { logout as requestLogout } from '@/views/login/hooks/api'
import { useAuthStore } from '@/stores/modules/auth'

/** 管理退出请求和本地会话清理，避免重复点击或请求失败阻止退出。 */
export function useLogout() {
  const loggingOut = ref(false)
  const router = useRouter()
  const message = useMessage()
  const authStore = useAuthStore()

  /** 注销会话并清理当前设备的登录状态，完成后替换路由至登录页。 */
  const handleLogout = async (): Promise<void> => {
    if (loggingOut.value) return
    loggingOut.value = true
    try {
      let sessionRevoked = false
      try {
        const response = await requestLogout()
        sessionRevoked = response.code === 200
      } catch (error) {
        // 请求拦截器已提示错误；仍继续本地退出，避免用户被困在登录状态。
        console.warn('会话注销请求失败，将继续清理本地登录状态', error)
      }
      authStore.logout()
      if (router.currentRoute.value.path !== '/login') {
        await router.replace('/login')
      }
      if (sessionRevoked) {
        message.success('已退出登录')
      } else {
        message.warning('已退出当前设备，未能确认服务器会话已注销')
      }
    } finally {
      loggingOut.value = false
    }
  }

  return { loggingOut, handleLogout }
}
