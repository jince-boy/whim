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

const authStore = useAuthStore()

/** 管理登录表单、验证码及登录后的跳转。 */
export function useLogin() {
  const message = useMessage()
  const formRef = useTemplateRef<FormInst>('formRef')
  const spinShow = ref(false)
  const form = reactive<LoginForm>({
    username: '',
    password: '',
    uuid: '',
    captcha: null,
    rememberMe: false,
  })
  const captcha = ref('')

  const handleLogin = () => {
    formRef.value?.validate((errors) => {
      if (!errors) {
        spinShow.value = true
        login(form)
          .then(async (res) => {
            if (res.code == 200) {
              message.success(res.message)
              authStore.login(res.data.token, form.rememberMe)
              await router.push(
                decodeURIComponent((router.currentRoute.value.query.redirect as string) || '/'),
              )
            } else {
              getCaptcha()
              message.error(res.message)
            }
          })
          .catch(() => {
            getCaptcha()
          })
          .finally(() => {
            spinShow.value = false
          })
      }
    })
  }
  const getCaptcha = () => {
    fetchCaptcha().then((res) => {
      form.uuid = res.data.uuid
      captcha.value = res.data.image
    })
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
  }
}
