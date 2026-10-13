import type { FormInst } from 'naive-ui'
import type { LoginForm } from '@/views/login/hooks/types.ts'
import { login, fetchCaptcha } from './api'
import { useAuthStore } from '@/stores/modules/auth.ts'
import router from '@/router'
import { useIcon } from '@/components/icon/useIcon.ts'
import { isMockEnabled } from '@/api'

const { createIcon } = useIcon()
const userIcon = createIcon('yonghu')
const passwordIcon = createIcon('mima')
const captchaIcon = createIcon('yanzhengma')

/** 管理登录表单、验证码及登录后的跳转。 */
export function useLogin() {
  const authStore = useAuthStore()
  const message = useMessage()
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
    try {
      const res = await login({ ...form })
      if (res.code !== 200) {
        message.error(res.message)
        await getCaptcha()
        return
      }
      authStore.login(res.data, form.rememberMe, form.username)
      let target = '/index'
      const redirect = router.currentRoute.value.query.redirect
      if (typeof redirect === 'string') {
        try {
          const path = decodeURIComponent(redirect)
          if (path.startsWith('/') && !path.startsWith('//') && !path.startsWith('/login')) {
            target = path
          }
        } catch {
          // 无效跳转参数使用首页。
        }
      }
      await router.replace(target)
      if (router.currentRoute.value.path === '/login') {
        // 用户信息初始化失败时，重新获取已被登录请求消费的验证码。
        await getCaptcha()
        return
      }
      message.success(res.message)
    } catch {
      // 请求错误由公共拦截器提示；仅未完成登录时刷新验证码。
      if (!authStore.isLogin) await getCaptcha()
    } finally {
      spinShow.value = false
    }
  }
  /** 预加载验证码后替换图片，刷新期间保留旧图并禁止提交。 */
  const getCaptcha = async () => {
    if (captchaLoading.value) return
    captchaLoading.value = true
    form.uuid = ''
    form.captchaCode = ''
    try {
      const res = await fetchCaptcha()
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
        captcha.value = ''
        message.error('验证码图片加载失败，请点击重试')
        return
      }
      captcha.value = imageSource
      form.uuid = res.data.uuid
    } catch {
      // 公共请求封装已提示错误，显示重试入口并阻止无效提交。
      captcha.value = ''
    } finally {
      captchaLoading.value = false
    }
  }
  onMounted(() => {
    getCaptcha()
  })
  return {
    isMockEnabled,
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
