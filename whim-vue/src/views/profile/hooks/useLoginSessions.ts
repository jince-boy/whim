import { onActivated, onDeactivated, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useFormDialog } from '@/components/formDialog'
import { useAuthStore } from '@/stores/modules/auth'
import { logout } from '@/views/login/hooks/api'
import { getLoginSessions, logoutAllSessions, revokeLoginSession } from './api'
import type { LoginSession } from './types'

/** 管理个人会话列表、单终端撤销及全端退出。 */
export function useLoginSessions() {
  const auth = useAuthStore()
  const router = useRouter()
  const { openConfirmDialog } = useFormDialog()
  const sessions = ref<LoginSession[]>([])
  const loading = ref(false)
  const loadFailed = ref(false)
  const active = ref(true)
  const revision = ref(0)

  onActivated(() => { active.value = true })
  onDeactivated(() => { active.value = false })
  watch([() => auth.getToken, active, revision], async ([token, visible], previous, onCleanup) => {
    // 刷新时保留原列表的位置，离开页面或切换账号时立即清除旧会话。
    if (!visible || token !== previous?.[0]) sessions.value = []
    loadFailed.value = false
    loading.value = Boolean(token && visible)
    if (!token || !visible) return
    const controller = new AbortController()
    onCleanup(() => controller.abort())
    try {
      const result = await getLoginSessions(controller.signal)
      if (controller.signal.aborted || token !== auth.getToken) return
      if (result.code === 200) sessions.value = result.data
      else loadFailed.value = true
    } catch {
      if (!controller.signal.aborted) loadFailed.value = true
    } finally {
      if (!controller.signal.aborted) loading.value = false
    }
  }, { immediate: true })

  function refresh() {
    revision.value++
  }

  /** 按登录时识别的设备类别展示终端，旧会话缺少类别时使用通用图标。 */
  function deviceIcon(type: string | null) {
    switch (type?.toLowerCase()) {
      case 'tablet': return 'tablet'
      case 'phone':
      case 'mobile': return 'smartphone'
      case 'desktop': return 'monitor'
      default: return 'monitor-smartphone'
    }
  }

  /** 确认后只对打开弹窗时的账号操作，退出当前终端时同步清理本地状态。 */
  function confirmLogout(session?: LoginSession) {
    const token = auth.getToken
    if (!token) return
    const terminal = session && ([session.browser, session.os].filter(Boolean).join(' / ') || session.deviceType || '未知设备')
    const title = !session ? '退出全部会话' : session.current ? '退出当前会话' : '撤销会话'
    openConfirmDialog({
      title,
      content: !session ? '所有设备（包括当前设备）都需要重新登录。'
        : session.current ? '当前设备将返回登录页，其他会话可以继续使用。'
        : `撤销 ${terminal}（IP ${session.ipAddress || '未知'}）的会话后，该设备需要重新登录。`,
      modalProps: { type: 'error', positiveText: title },
      async onSubmit() {
        if (auth.getToken !== token) return false
        const result = !session ? await logoutAllSessions()
          : session.current ? await logout() : await revokeLoginSession(session.sessionId)
        if (auth.getToken !== token) return false
        if (result.code === 200 && (!session || session.current)) {
          // 即使等待响应期间离开页面，已被服务端注销的本地令牌也应清理。
          auth.logout()
          await router.replace('/login')
        }
        return result
      },
      onSuccess: refresh,
    })
  }

  return { sessions, loading, loadFailed, refresh, confirmLogout, deviceIcon }
}
