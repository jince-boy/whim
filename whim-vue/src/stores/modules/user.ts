import type { UserState } from './user/types'
import { getUserInfo } from './user/api'
import { useAuthStore } from './auth'

export const useUserStore = defineStore('user', {
  /** 初始化当前用户状态，授权随用户资料统一更新。 */
  state: (): UserState => ({
    user: null,
  }),
  getters: {
    /** 判断当前会话是否已完成用户信息初始化。 */
    hasUserInfo(): boolean {
      return !!this.user
    },
    /** 提供按钮权限指令使用的权限编码。 */
    permissionCode: (state): string[] => state.user?.permissionCodeSet ?? [],
    /** 提供角色指令使用的角色编码。 */
    roleCode: (state): string[] => state.user?.roleCodeSet ?? [],
  },
  actions: {
    /** 获取真实用户资料；失败时阻止进入依赖授权信息的页面。 */
    async getUserInfo() {
      const authStore = useAuthStore()
      const token = authStore.getToken
      const res = await getUserInfo()
      // 退出或切换账号后，丢弃旧会话尚未完成的响应。
      if (authStore.getToken !== token) throw new Error('登录会话已变更，请重新获取用户信息')
      if (res.code !== 200) {
        const message = res.message || '用户信息获取失败，请重试'
        window.$message?.error(message)
        throw new Error(message)
      }
      this.user = res.data
    },
  },
})
