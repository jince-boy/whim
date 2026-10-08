import { usePermissionStore } from '@/stores/modules/permission.ts'
import { useUserStore } from '@/stores/modules/user.ts'
import { useTabStore } from '@/stores/modules/tab.ts'
import { useKeepAliveStore } from '@/stores/modules/keepAlive.ts'

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || null
    return {
      token,
    }
  },
  getters: {
    getToken: (state) => state.token,
    isLogin: (state) => {
      return !!state.token
    },
  },
  actions: {
    login(token: string, rememberMe: boolean = false) {
      this.token = token
      if (rememberMe) {
        localStorage.setItem('token', token)
        sessionStorage.removeItem('token')
      } else {
        sessionStorage.setItem('token', token)
        localStorage.removeItem('token')
      }
    },
    /** 清理登录令牌、用户授权、动态路由、标签页和页面缓存名单。 */
    logout() {
      this.token = null
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
      usePermissionStore().resetRouter()
      useUserStore().$reset()
      useTabStore().$reset()
      useKeepAliveStore().clearKeepAlive()
    },
  },
})
