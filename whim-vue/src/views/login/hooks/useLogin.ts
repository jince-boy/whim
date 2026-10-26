import type { FormInst } from 'naive-ui'
import type { LoginForm } from '@/views/login/hooks/types.ts'
import { login, fetchCaptcha } from './api'
import { useAuthStore } from '@/stores/modules/auth.ts'
import { useRoute, useRouter } from 'vue-router'
import { message } from '@/plugins'
import { useIcon } from '@/components/icon/useIcon.ts'

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
  const captcha = ref('')
  const captchaLoading = ref(false)
  const captchaController = new AbortController()
  let captchaExpiresAt = 0
  let captchaTimer: ReturnType<typeof setTimeout> | undefined

  /** 校验并提交登录，失败后更新已被后端消费的验证码。 */
  const handleLogin = async () => {
    if (spinShow.value || captchaLoading.value || !form.uuid || !formRef.value) return
    spinShow.value = true
    try {
      await formRef.value.validate()
    } catch {
      spinShow.value = false
      return
    }
    let loginToken: string | undefined
    try {
      // 后台标签页的定时器可能延迟，提交前按实际时间再次检查有效期。
      if (Date.now() >= captchaExpiresAt) {
        await getCaptcha()
        if (form.uuid) message.warning('验证码已自动刷新，请重新输入')
        return
      }
      const res = await login({
        username: form.username,
        password: form.password,
        uuid: form.uuid,
        captchaCode: form.captchaCode,
      })
      if (res.code !== 200) {
        message.error(res.message)
        await getCaptcha()
        return
      }
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
      // 路由加载失败也必须撤销本次登录，防止停在登录页却保留半完成的会话。
      if (loginToken && authStore.getToken === loginToken) {
        authStore.logout()
        message.error('登录后页面加载失败，请重试')
      }
      // 请求错误由公共拦截器提示；未完成登录时刷新一次性验证码。
      if (!authStore.isLogin) await getCaptcha()
    } finally {
      spinShow.value = false
    }
  }
  /** 预加载验证码后替换图片，刷新期间保留旧图并禁止提交。 */
  const getCaptcha = async () => {
    if (captchaLoading.value || captchaController.signal.aborted) return
    clearTimeout(captchaTimer)
    captchaExpiresAt = 0
    captchaLoading.value = true
    form.uuid = ''
    form.captchaCode = ''
    const requestedAt = Date.now()
    try {
      const res = await fetchCaptcha(captchaController.signal)
      if (captchaController.signal.aborted) return
      if (res.code !== 200) {
        captcha.value = ''
        message.error(res.message)
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
        if (captchaController.signal.aborted) return
        captcha.value = ''
        message.error('验证码图片加载失败，请点击重试')
        return
      }
      if (captchaController.signal.aborted) return
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
  const handleCaptchaVisibility = () => {
    if (document.visibilityState === 'visible' && form.uuid && Date.now() >= captchaExpiresAt) {
      void getCaptcha()
    }
  }
  onMounted(() => {
    void getCaptcha()
    document.addEventListener('visibilitychange', handleCaptchaVisibility)
  })
  // 离开页面时释放到期定时器，并取消尚未完成的验证码请求。
  onBeforeUnmount(() => {
    clearTimeout(captchaTimer)
    captchaController.abort()
    document.removeEventListener('visibilitychange', handleCaptchaVisibility)
  })
  return {
    formRef,
    form,
    captcha,
    userIcon,
    passwordIcon,
    captchaIcon,
    handleLogin,
    getCaptcha,
    spinShow,
    captchaLoading,
  }
}
