import type { FormInst } from 'naive-ui'
import type { LoginForm } from '@/views/login/hooks/types.ts'
import { login, verifyLogin, fetchCaptcha } from './api'
import { useAuthStore } from '@/stores/modules/auth.ts'
import { useRoute, useRouter } from 'vue-router'
import { message } from '@/plugins'
import { useIcon } from '@/components/appIcon/hooks/useIcon.ts'
import { authenticatorCodePattern, recoveryCodePattern } from '@/utils/authenticator'

const CHALLENGE_LIFETIME_MS = 3 * 60 * 1000
const { createIcon } = useIcon()
const userIcon = createIcon('user')
const passwordIcon = createIcon('lock-keyhole')
const captchaIcon = createIcon('shield-check')

/** 管理登录表单、验证码及登录后的跳转。 */
export function useLogin() {
  const authStore = useAuthStore()
  const router = useRouter()
  const route = useRoute()
  const formRef = useTemplateRef<FormInst>('formRef')
  const spinShow = ref(false)
  const form = reactive<LoginForm>({
    username: '',
    password: '',
    uuid: '',
    captchaCode: '',
    rememberMe: false,
  })
  const challengeId = ref('')
  const secondCode = ref('')
  const recovering = ref(false)
  let challengeExpiresAt = 0
  let challengeTimer: ReturnType<typeof setTimeout> | undefined
  const validSecondCode = computed(() => recovering.value
    ? recoveryCodePattern.test(secondCode.value)
    : authenticatorCodePattern.test(secondCode.value))

  /** 重新获取图片验证码，清理已经消费或过期的挑战。 */
  function restartLogin() {
    clearTimeout(challengeTimer)
    challengeExpiresAt = 0
    challengeId.value = ''
    secondCode.value = ''
    recovering.value = false
    void getCaptcha()
  }

  /** 后台标签页可能延迟定时器，提交和重新可见时共用同一到期判断。 */
  function expireChallenge() {
    if (pageController.signal.aborted || !challengeId.value || spinShow.value || Date.now() < challengeExpiresAt) return false
    restartLogin()
    message.info('两步验证已过期，请重新登录')
    return true
  }

  /** 恢复码将解除旧绑定，显式切换模式避免误操作。 */
  function changeVerificationMode() {
    recovering.value = !recovering.value
    secondCode.value = ''
  }

  const captcha = ref('')
  const captchaEnabled = ref(true)
  const captchaLoading = ref(false)
  const pageController = new AbortController()
  let captchaExpiresAt = 0
  let captchaTimer: ReturnType<typeof setTimeout> | undefined

  /** 校验并提交登录，失败后更新已被后端消费的验证码。 */
  const handleLogin = async () => {
    if (spinShow.value || expireChallenge()) return
    if (challengeId.value) {
      if (!validSecondCode.value) return
    } else if (captchaLoading.value || (captchaEnabled.value && !form.uuid) || !formRef.value) {
      return
    }
    spinShow.value = true
    try {
      if (!challengeId.value) await formRef.value?.validate()
    } catch {
      spinShow.value = false
      return
    }
    let loginToken: string | undefined
    try {
      // 后台标签页的定时器可能延迟，提交前按实际时间再次检查有效期。
      if (!challengeId.value && captchaEnabled.value && Date.now() >= captchaExpiresAt) {
        await getCaptcha()
        if (form.uuid) message.warning('验证码已自动刷新，请重新输入')
        return
      }
      const requestedAt = Date.now()
      clearTimeout(captchaTimer)
      const res = challengeId.value
        ? await verifyLogin({ challengeId: challengeId.value, code: secondCode.value }, pageController.signal)
        : await login({
            username: form.username,
            password: form.password,
            ...(captchaEnabled.value ? { uuid: form.uuid, captchaCode: form.captchaCode } : {}),
          }, pageController.signal)
      if (pageController.signal.aborted) return
      if (res.code !== 200) {
        message.error(res.message)
        if (!challengeId.value) await getCaptcha()
        return
      }
      if ('challengeId' in res.data) {
        challengeId.value = res.data.challengeId
        form.password = ''
        form.captchaCode = ''
        challengeExpiresAt = requestedAt + CHALLENGE_LIFETIME_MS
        challengeTimer = setTimeout(expireChallenge, challengeExpiresAt - Date.now())
        return
      }
      if ('recovered' in res.data) {
        message.success(res.message)
        restartLogin()
        return
      }
      clearTimeout(challengeTimer)
      challengeId.value = ''
      loginToken = res.data.accessToken
      authStore.login(res.data, form.rememberMe, form.username)
      let target = '/'
      const redirect = route.query.redirect
      if (typeof redirect === 'string') {
        try {
          const path = decodeURIComponent(redirect)
          if (path.startsWith('/') && !path.startsWith('//') && !path.startsWith('/login')) {
            target = path
          }
        } catch {
          // 无效跳转参数由导航守卫选择当前用户的默认入口。
        }
      }
      await router.replace(target)
      if (router.currentRoute.value.path === '/login') {
        if (authStore.getToken === loginToken) {
          authStore.logout()
          message.error('登录初始化未完成，请重新登录')
        }
        // 用户信息初始化失败时，重新获取已被登录请求消费的验证码。
        await getCaptcha()
        return
      }
      message.success(res.message)
    } catch {
      if (pageController.signal.aborted && !loginToken) return
      // 路由加载失败也必须撤销本次登录，防止停在登录页却保留半完成的会话。
      if (loginToken && authStore.getToken === loginToken) {
        authStore.logout()
        message.error('登录后页面加载失败，请重试')
      }
      // 请求错误由公共拦截器提示；未完成登录时刷新一次性验证码。
      if (challengeId.value) secondCode.value = ''
      else if (!authStore.isLogin) await getCaptcha()
    } finally {
      spinShow.value = false
      expireChallenge()
    }
  }
  /** 预加载验证码后替换图片，刷新期间保留旧图并禁止提交。 */
  const getCaptcha = async () => {
    if (challengeId.value || captchaLoading.value || pageController.signal.aborted) return
    clearTimeout(captchaTimer)
    captchaExpiresAt = 0
    captchaLoading.value = true
    captchaEnabled.value = true
    form.uuid = ''
    form.captchaCode = ''
    const requestedAt = Date.now()
    try {
      const res = await fetchCaptcha(pageController.signal)
      if (pageController.signal.aborted) return
      if (res.code !== 200) {
        captcha.value = ''
        message.error(res.message)
        return
      }
      if (res.data.enabled === false) {
        captchaEnabled.value = false
        captcha.value = ''
        return
      }
      const imageSource = res.data.image.startsWith('data:')
        ? res.data.image
        : `data:image/gif;base64,${res.data.image}`
      const image = new Image()
      image.src = imageSource
      try {
        await image.decode()
      } catch {
        if (pageController.signal.aborted) return
        captcha.value = ''
        message.error('验证码图片加载失败，请点击重试')
        return
      }
      if (pageController.signal.aborted) return
      captchaExpiresAt = requestedAt + res.data.expiresIn * 1000
      if (Date.now() >= captchaExpiresAt) {
        captcha.value = ''
        // 请求或图片解码耗时超过有效期时，稍后自动重取，避免连续发送请求。
        captchaTimer = setTimeout(getCaptcha, 1000)
        return
      }
      captcha.value = imageSource
      form.uuid = res.data.uuid
      captchaTimer = setTimeout(getCaptcha, captchaExpiresAt - Date.now())
    } catch {
      // 公共请求封装已提示错误，显示重试入口并阻止无效提交。
      captcha.value = ''
    } finally {
      captchaLoading.value = false
    }
  }
  /** 返回标签页时补充检查有效期，避免后台定时器延迟导致旧验证码仍可提交。 */
  const handleVisibility = () => {
    if (document.visibilityState !== 'visible' || spinShow.value || expireChallenge()) return
    if (!challengeId.value && form.uuid && Date.now() >= captchaExpiresAt) {
      void getCaptcha()
    }
  }
  onMounted(() => {
    void getCaptcha()
    document.addEventListener('visibilitychange', handleVisibility)
  })
  // 离开页面时释放到期定时器，并取消尚未完成的验证码请求。
  onBeforeUnmount(() => {
    clearTimeout(captchaTimer)
    clearTimeout(challengeTimer)
    pageController.abort()
    document.removeEventListener('visibilitychange', handleVisibility)
  })
  return {
    form,
    challengeId, secondCode, recovering, validSecondCode, restartLogin, changeVerificationMode,
    captcha,
    captchaEnabled,
    userIcon,
    passwordIcon,
    captchaIcon,
    handleLogin,
    getCaptcha,
    spinShow,
    captchaLoading,
  }
}
