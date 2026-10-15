import { computed, onBeforeUnmount, onMounted, ref, shallowRef, useTemplateRef } from 'vue'
import type { FormInst } from 'naive-ui'
import type { SearchFormBinding, SearchFormEmits, SearchFormRegistration } from '../types'

/** 连接查询表单 hook，并将用户提交、重置作为事件通知调用方。 */
export function useSearchFormView(
  emit: <Event extends keyof SearchFormEmits>(
    event: Event,
    ...args: SearchFormEmits[Event]
  ) => void,
) {
  const binding = shallowRef<SearchFormBinding>()
  const formRef = useTemplateRef<FormInst>('formRef')
  const submitting = ref(false)
  const formProps = computed(() => binding.value?.formProps.value)
  const gridProps = computed(() => binding.value?.gridProps.value)

  /** 使用原生表单校验，未挂载时明确失败。 */
  const validate: FormInst['validate'] = (...args) => {
    if (!formRef.value) throw new Error('查询表单尚未挂载')
    return formRef.value.validate(...args)
  }

  /** 清理字段校验。 */
  function restoreValidation() {
    formRef.value?.restoreValidation()
  }

  /** 阻止重复校验，无效字段留在表单中展示。 */
  async function submit() {
    if (!binding.value || submitting.value) return false
    submitting.value = true
    try {
      await validate()
      emit('submit')
      return true
    } catch {
      return false
    } finally {
      submitting.value = false
    }
  }

  /** 通过 hook 恢复模型，再通知调用方使用新条件。 */
  function reset() {
    if (!binding.value || submitting.value) return
    binding.value.resetFields()
    emit('reset')
  }

  const registration: SearchFormRegistration = {
    /** 使用 hook 持有的同一份响应式状态。 */
    connect: (value) => {
      binding.value = value
    },
    validate,
    restoreValidation,
    submit,
    reset,
  }
  onMounted(() => emit('register', registration))
  onBeforeUnmount(() => emit('register', undefined))
  return { binding, formProps, gridProps, submitting, submit, reset, validate, restoreValidation }
}
