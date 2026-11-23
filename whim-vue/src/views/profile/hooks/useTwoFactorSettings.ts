import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { message } from '@/plugins'
import { authenticatorCodePattern, recoveryCodePattern } from '@/utils/authenticator'
import { disableTwoFactor, enableTwoFactor, getTwoFactorStatus, startTwoFactorSetup } from './api'
import type { TwoFactorSettingsDraft } from './types'

/** 管理本人的验证器状态和单次绑定或解绑草稿。 */
export function useTwoFactorSettings(username: () => string) {
  const auth = useAuthStore()
  const router = useRouter()
  const enabled = ref<boolean | null>(null)
  const loading = ref(false)
  const dialog = ref<TwoFactorSettingsDraft>()
  const recoveryDisplay = useTemplateRef<HTMLElement>('recoveryDisplay')
  let statusRequest: AbortController | undefined
  let active = true

  const statusDescription = computed(() => {
    if (enabled.value === null) return loading.value ? '正在查询验证状态…' : '未能获取验证状态'
    return enabled.value ? '已开启，登录时需要输入验证器动态码' : '未开启，可使用手机验证器保护账号'
  })
  const uri = computed(() => dialog.value?.setup
    ? `otpauth://totp/Whim:${encodeURIComponent(username())}?secret=${dialog.value.setup.secret}&issuer=Whim&algorithm=SHA1&digits=6&period=30`
    : '')
  const canSubmit = computed(() => {
    const draft = dialog.value
    if (!draft) return false
    if (draft.setup) return draft.savedRecovery && authenticatorCodePattern.test(draft.code)
    return !!draft.password && (!draft.disabling || authenticatorCodePattern.test(draft.code) || recoveryCodePattern.test(draft.code))
  })

  function open() {
    if (enabled.value === null || loading.value) return
    dialog.value = { disabling: enabled.value, password: '', code: '', savedRecovery: false, submitting: false }
  }

  function close() {
    dialog.value = undefined
  }

  /** 浏览器限制剪贴板时选中本次弹窗中的恢复码。 */
  async function copyRecoveryCode() {
    const draft = dialog.value
    if (!draft?.setup) return
    try {
      await navigator.clipboard.writeText(draft.setup.recoveryCode)
      if (dialog.value === draft) message.success('恢复码已复制')
    } catch {
      if (dialog.value !== draft || !recoveryDisplay.value) return
      window.getSelection()?.selectAllChildren(recoveryDisplay.value)
      message.info('请手动复制恢复码')
    }
  }

  async function refresh() {
    if (!active || loading.value || !auth.getToken) return
    const request = new AbortController()
    statusRequest = request
    loading.value = true
    try {
      const result = await getTwoFactorStatus(request.signal)
      if (request.signal.aborted) return
      enabled.value = result.code === 200 ? result.data : null
      if (result.code !== 200) message.error(result.message)
    } catch {
      if (!request.signal.aborted) enabled.value = null
    } finally {
      if (statusRequest === request) {
        statusRequest = undefined
        loading.value = false
      }
    }
  }

  async function submit() {
    const draft = dialog.value
    const token = auth.getToken
    if (!draft || !token || !canSubmit.value || draft.submitting) return
    draft.submitting = true
    try {
      if (!draft.disabling && !draft.setup) {
        const result = await startTwoFactorSetup({ password: draft.password })
        if (dialog.value !== draft || auth.getToken !== token) return
        if (result.code !== 200) return message.error(result.message)
        draft.password = ''
        draft.setup = result.data
        return
      }
      const result = draft.setup
        ? await enableTwoFactor({ setupId: draft.setup.setupId, code: draft.code })
        : await disableTwoFactor({ password: draft.password, code: draft.code })
      // 即使提交期间离开个人信息页，成功变更也已使原会话失效。
      if (auth.getToken !== token) return
      if (result.code !== 200) return message.error(result.message)
      close()
      message.success(result.message)
      auth.logout()
      await router.replace('/login')
    } catch {
      // 请求层已提示错误，仍打开的弹窗可更正密码或动态码。
    } finally {
      draft.submitting = false
    }
  }

  function resetView() {
    statusRequest?.abort()
    statusRequest = undefined
    enabled.value = null
    loading.value = false
    close()
  }

  watch(() => auth.getToken, () => {
    resetView()
    void refresh()
  }, { immediate: true })
  onActivated(() => {
    active = true
    void refresh()
  })
  onDeactivated(() => {
    active = false
    resetView()
  })
  onBeforeUnmount(resetView)
  return { enabled, loading, statusDescription, dialog, uri, canSubmit, open, close, refresh, submit, copyRecoveryCode }
}
