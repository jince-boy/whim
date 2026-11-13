import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { DropdownOption } from 'naive-ui'
import { createIcon } from '@/components/icon/useIcon'
import { message } from '@/plugins'
import { useAuthStore } from '@/stores/modules/auth'
import { useUserStore } from '@/stores/modules/user'
import { useAvatarImages } from '@/hooks/useAvatarImages'
import { logout } from '@/views/login/hooks/api'

/** 管理用户资料入口和服务端退出登录。 */
export function useUserMenu() {
  const authStore = useAuthStore()
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const signingOut = ref(false)
  const menuShown = ref(false)
  const username = computed(() => userStore.user?.username || authStore.username || '用户')
  const displayName = computed(() => userStore.user?.name || username.value)
  const { getAvatarUrl } = useAvatarImages(() => [userStore.user?.avatar])
  const avatar = computed(() => getAvatarUrl(userStore.user?.avatar))
  const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
  const options = computed<DropdownOption[]>(() => [
    { label: '个人信息', key: 'profile', icon: createIcon('user-round', 16), disabled: signingOut.value },
    { type: 'divider', key: 'divider' },
    {
      label: signingOut.value ? '正在退出…' : '退出登录', key: 'logout',
      icon: createIcon('log-out', 16), disabled: signingOut.value,
    },
  ])

  watch(() => route.fullPath, () => { menuShown.value = false })

  /** 从个人菜单进入当前账号的资料页。 */
  async function openProfile(): Promise<void> {
    if (signingOut.value) return
    await router.push('/profile')
  }

  /** 服务端退出成功后清理本地登录状态和页面缓存。 */
  async function handleSelect(key: string | number): Promise<void> {
    menuShown.value = false
    if (signingOut.value) return
    if (key === 'profile') {
      await openProfile()
      return
    }
    if (key !== 'logout') return
    signingOut.value = true
    try {
      const res = await logout()
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      authStore.logout()
      await router.replace('/login')
      message.success(res.message || '退出成功')
    } catch {
      // 请求错误由公共拦截器提示；失败时保留会话以便重试。
    } finally {
      signingOut.value = false
    }
  }

  return { username, displayName, avatar, avatarText, signingOut, menuShown, options, handleSelect }
}
