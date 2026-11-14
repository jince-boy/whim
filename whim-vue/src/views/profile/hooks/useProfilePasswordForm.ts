import { onScopeDispose, ref, useTemplateRef } from 'vue'
import type { FormInst } from 'naive-ui'
import { createProfilePasswordRules } from './rule'
import type { ProfilePasswordFormModel } from './types'

/** 密码只保存在当前弹窗草稿中，关闭后清理，不写入个人资料或浏览器缓存。 */
export function useProfilePasswordForm() {
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<ProfilePasswordFormModel>({
    oldPassword: '',
    password: '',
    confirmPassword: '',
  })
  const rules = createProfilePasswordRules(() => formModel.value.password)
  onScopeDispose(() => {
    formModel.value = { oldPassword: '', password: '', confirmPassword: '' }
  })
  return { formRef, formModel, rules }
}
