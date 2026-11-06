import { ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { createPasswordRules } from './rule'
import type { PasswordFormModel, PasswordFormProps } from './types'

/** 密码始终从空草稿填写，不回显任何已有凭据。 */
export function usePasswordForm(props: PasswordFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<PasswordFormModel>({ id: props.user.id, password: '', confirmPassword: '' })
  const rules = createPasswordRules(() => formModel.value.password)
  watch(() => props.user.id, (id) => {
    formModel.value = { id, password: '', confirmPassword: '' }
    formRef.value?.restoreValidation()
  })
  return { formRef, formModel, rules }
}

