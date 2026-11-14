import { defineStore } from 'pinia'
import { computed, shallowRef } from 'vue'
import type { UserInfoResult } from './user/types'
import { getUserInfo as fetchUserInfo } from './user/api'
import { useAuthStore } from './auth'

/** 当前用户资料和授权共用一次请求，清理会话时使未完成的请求失效。 */
export const useUserStore = defineStore('user', () => {
  const user = shallowRef<UserInfoResult | null>(null)
  const hasUserInfo = computed(() => user.value !== null)
  const permissionCode = computed(() => user.value?.permissionCodeSet ?? [])
  const roleCode = computed(() => user.value?.roleCodeSet ?? [])
  const superAdministrator = computed(() => user.value?.superAdministrator === true)
  let revision = 0
  let pending: Promise<void> | undefined

  /** 超级管理员身份默认拥有全部功能，普通账号精确匹配实际角色权限。 */
  function hasPermission(permission: string): boolean {
    return superAdministrator.value || permissionCode.value.includes(permission)
  }

  /** 获取用户资料；并发调用复用请求，旧会话的响应不能覆盖新账号。 */
  async function getUserInfo(): Promise<void> {
    if (pending) return pending
    const version = revision
    const token = useAuthStore().getToken
    const request = (async () => {
      const response = await fetchUserInfo()
      if (version !== revision || token !== useAuthStore().getToken) return
      if (response.code !== 200) throw new Error(response.message || '用户信息获取失败，请重试')
      user.value = response.data
    })()
    pending = request
    try {
      await request
    } finally {
      if (pending === request) pending = undefined
    }
  }

  /** 清空当前用户和授权，并丢弃未完成请求的结果。 */
  function clearUserInfo(): void {
    revision++
    pending = undefined
    user.value = null
  }

  /** 应用本人资料保存结果，并使保存前尚未结束的旧查询失效。 */
  function updateCurrentUserInfo(info: UserInfoResult): void {
    if (!user.value || String(info.userId) !== String(user.value.userId)) return
    revision++
    pending = undefined
    user.value = info
  }

  return {
    user,
    hasUserInfo,
    superAdministrator,
    permissionCode,
    roleCode,
    hasPermission,
    getUserInfo,
    clearUserInfo,
    updateCurrentUserInfo,
  }
})
