import { computed, ref } from 'vue'
import { isAxiosError } from 'axios'
import { message } from '@/plugins'
import { useUserStore } from '@/stores/modules/user'

/** 展示当前用户的资料与角色，支持重新读取服务端最新信息。 */
export function useProfile() {
  const userStore = useUserStore()
  const loading = ref(false)
  const user = computed(() => userStore.user)
  const displayName = computed(() => user.value?.name || user.value?.username || '用户')
  const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
  const roles = computed(() =>
    user.value?.roleInfoList?.length
      ? user.value.roleInfoList.map((role) => ({ code: role.roleCode, name: role.roleName }))
      : (user.value?.roleCodeSet ?? []).map((code) => ({ code, name: code })),
  )
  const accountType = computed(() =>
    user.value?.loginType === 'system' ? '系统账号' : user.value?.loginType || '—',
  )

  /** 刷新用户资料，请求失败时保留原资料并提示错误。 */
  async function refreshProfile(): Promise<void> {
    if (loading.value) return
    loading.value = true
    try {
      await userStore.getUserInfo()
    } catch (error) {
      if (!isAxiosError(error))
        message.error(error instanceof Error ? error.message : '用户资料刷新失败，请重试')
    } finally {
      loading.value = false
    }
  }

  return { user, displayName, avatarText, roles, accountType, loading, refreshProfile }
}
