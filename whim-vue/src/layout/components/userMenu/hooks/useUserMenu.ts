import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from '@/plugins'
import { useAuthStore } from '@/stores/modules/auth'
import { useUserStore } from '@/stores/modules/user'
import { logout } from '@/views/login/hooks/api'

/** 管理用户资料入口和服务端退出登录。 */
export function useUserMenu() {
  const authStore = useAuthStore()
  const userStore = useUserStore()
  const router = useRouter()
  const signingOut = ref(false)
  const username = computed(() => userStore.user?.username || authStore.username || '用户')
  const displayName = computed(() => userStore.user?.name || username.value)
  const avatar = computed(() => userStore.user?.avatar || undefined)
  const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
  const options = computed(() => [
    { label: '个人信息设置', key: 'profile', disabled: signingOut.value },
    {
      label: signingOut.value ? '正在退出…' : '退出登录',
      key: 'logout',
      disabled: signingOut.value,
    },
  ])

  /** 点击头像或姓名，直接进入当前用户的个人信息页。 */
  async function openProfile(): Promise<void> {
    if (signingOut.value) return
    await router.push('/profile')
  }

  /** 服务端退出成功后清理本地登录状态和页面缓存。 */
  async function handleSelect(key: string | number): Promise<void> {
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

  return { displayName, avatar, avatarText, signingOut, options, openProfile, handleSelect }
}
