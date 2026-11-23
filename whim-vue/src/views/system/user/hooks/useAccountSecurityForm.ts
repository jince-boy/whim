import { computed, ref, useId, useTemplateRef } from 'vue'
import type { FormInst } from 'naive-ui'
import { createPasswordRules } from './rule'
import type { AccountSecurityFormProps, PasswordFormModel } from './types'

/** 两个操作使用各自的密码草稿，公共弹窗只校验、提交当前页签。 */
export function useAccountSecurityForm(props: AccountSecurityFormProps) {
  const activeTab = ref<'password' | 'twoFactor'>('password')
  const formId = useId()
  const passwordRef = useTemplateRef<FormInst>('passwordRef')
  const resetRef = useTemplateRef<FormInst>('resetRef')
  const passwordModel = ref<PasswordFormModel>({ id: props.user.id, password: '', confirmPassword: '' })
  const resetModel = ref({ currentPassword: '' })
  const rules = createPasswordRules(() => passwordModel.value.password)
  const formRef = computed(() => activeTab.value === 'password' ? passwordRef.value : resetRef.value)
  const formModel = computed(() => activeTab.value === 'password' ? passwordModel.value : resetModel.value)
  return { activeTab, formId, formRef, formModel, passwordModel, resetModel, rules }
}
