import { usePermissionStore } from '@/stores/modules/permission.ts'
import { useUserStore } from '@/stores/modules/user.ts'
import { useTabStore } from '@/stores/modules/tab.ts'
import { useKeepAliveStore } from '@/stores/modules/keepAlive.ts'
import type { LoginResult } from '@/views/login/hooks/types'

export const useAuthStore = defineStore('auth', {
  /** 恢复登录令牌，并将旧版分散缓存转换为同一个令牌对象。 */
  state: () => {
    const storage = localStorage.getItem('token') ? localStorage : sessionStorage
    let token: string | null = null
    let tokenType = 'Bearer'
    const savedToken = storage.getItem('token')
    if (savedToken) {
      try {
        const legacyTokenType = storage.getItem('tokenType')
        const saved: unknown = legacyTokenType
          ? { token: savedToken, tokenType: legacyTokenType }
          : JSON.parse(savedToken)
        if (
          typeof saved === 'object' &&
          saved !== null &&
          'token' in saved &&
          typeof saved.token === 'string' &&
          saved.token &&
          'tokenType' in saved &&
          typeof saved.tokenType === 'string' &&
          saved.tokenType
        ) {
          token = saved.token
          tokenType = saved.tokenType
          if (legacyTokenType) storage.setItem('token', JSON.stringify({ token, tokenType }))
        } else {
          storage.removeItem('token')
        }
      } catch {
        // 无效缓存按未登录处理，避免解析失败阻止页面加载。
        storage.removeItem('token')
      }
    }
    storage.removeItem('tokenType')
    return {
      token,
      tokenType,
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
      // 新登录会话不能复用上一账号的授权、导航和页面实例。
      usePermissionStore().resetRouter()
      useUserStore().$reset()
      useTabStore().$reset()
      useKeepAliveStore().clearKeepAlive()
      this.token = result.accessToken
      this.tokenType = result.tokenType
      this.username = username
      const storage = rememberMe ? localStorage : sessionStorage
      const otherStorage = rememberMe ? sessionStorage : localStorage
      for (const key of ['token', 'tokenType', 'username']) {
        otherStorage.removeItem(key)
      }
      storage.setItem('token', JSON.stringify({ token: this.token, tokenType: this.tokenType }))
      storage.removeItem('tokenType')
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
