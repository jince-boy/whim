import { usePermissionStore } from '@/stores/modules/permission.ts'
import { useUserStore } from '@/stores/modules/user.ts'
import { useTabStore } from '@/stores/modules/tab.ts'
import { useKeepAliveStore } from '@/stores/modules/keepAlive.ts'
import type { LoginResult } from '@/views/login/hooks/types'

export const useAuthStore = defineStore('auth', {
  state: () => {
    const storage = localStorage.getItem('token') ? localStorage : sessionStorage
    return {
      token: storage.getItem('token'),
      tokenType: storage.getItem('tokenType') || 'Bearer',
      username: storage.getItem('username') || '',
    }
  },
  getters: {
    getToken: (state) => state.token,
    isLogin: (state) => {
      return !!state.token
    },
  },
  actions: {
    /** 保存后端令牌和账号；保持登录时持久化，否则仅保存于当前会话。 */
    login(result: LoginResult, rememberMe: boolean, username: string) {
      this.token = result.accessToken
      this.tokenType = result.tokenType
      this.username = username
      const storage = rememberMe ? localStorage : sessionStorage
      const otherStorage = rememberMe ? sessionStorage : localStorage
      for (const key of ['token', 'tokenType', 'username']) {
        otherStorage.removeItem(key)
      }
      storage.setItem('token', result.accessToken)
      storage.setItem('tokenType', result.tokenType)
      storage.setItem('username', username)
    },
    /** 清理登录令牌、用户授权、动态路由、标签页和页面缓存名单。 */
    logout() {
      this.token = null
      this.tokenType = 'Bearer'
      this.username = ''
      for (const storage of [localStorage, sessionStorage]) {
        for (const key of ['token', 'tokenType', 'username']) storage.removeItem(key)
      }
      usePermissionStore().resetRouter()
      useUserStore().$reset()
      useTabStore().$reset()
      useKeepAliveStore().clearKeepAlive()
    },
  },
})
