import type { UserInfoResult } from './user/types'
import { getUserInfo } from './user/api'

export const useUserStore = defineStore('user', {
  state: (): UserInfoResult => ({
    user: null,
    permissionCode: [],
    roleCode: [],
    menus: [],
  }),
  getters: {
    hasUserInfo(): boolean {
      return !!this.user
    },
  },
  actions: {
    async getUserInfo() {
      await getUserInfo().then((res) => {
        this.user = res.data.user
        this.permissionCode = res.data.permissionCode
        this.roleCode = res.data.roleCode
        this.menus = res.data.menus
      })
    },
  },
})
