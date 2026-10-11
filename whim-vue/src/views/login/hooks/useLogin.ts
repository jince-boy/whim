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
      message.success(res.message)
      let target = '/index'
      if (isMockEnabled) {
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
      }
      await router.replace(target)
    } catch {
      // 请求错误由公共拦截器提示；仅未完成登录时刷新验证码。
      if (!authStore.isLogin) await getCaptcha()
    } finally {
      spinShow.value = false
    }
  }
  /** 加载验证码图片并清空旧答案，合并连续点击刷新。 */
  const getCaptcha = async () => {
    if (captchaLoading.value) return
    captchaLoading.value = true
    form.uuid = ''
    form.captchaCode = ''
    captcha.value = ''
    try {
      const res = await fetchCaptcha()
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      form.uuid = res.data.uuid
      captcha.value = res.data.image.startsWith('data:')
        ? res.data.image
        : `data:image/gif;base64,${res.data.image}`
    } catch {
      // 公共请求封装已提示错误，保持空验证码以阻止无效提交。
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
