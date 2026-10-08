import { type FormInst } from 'naive-ui'
/**
 * Index 通用表单 Props 类型
 * @template T 表单数据类型
 */
export interface BaseFormProps<T extends Record<string, unknown>> {
  model: T
  rules?: Record<string, unknown>
  showFeedback?: boolean
  labelPlacement?: 'left' | 'top'
}

/**
 * Index 事件类型
 * @template T 表单数据类型
 */
export interface BaseFormEmits<T extends Record<string, unknown>> {
  submit: [values: T]
  reset: [values: T]
}

export function useBaseForm<T extends Record<string, unknown>>(
  props: BaseFormProps<T>,
  emit: <E extends keyof BaseFormEmits<T>>(event: E, ...args: BaseFormEmits<T>[E]) => void,
) {
  /** Naive UI 表单实例 */
  const formRef = useTemplateRef<FormInst>('formRef')

  /** 与父组件 model 保持引用同步 */
  const formModel = toRef(() => props.model)

  /** 初始表单数据快照 */
  const defaultFormModel = shallowRef({ ...props.model })

  /** 提交表单 */
  const submit = async () => {
    await formRef.value?.validate()
    emit('submit', toRaw(formModel.value) as T)
  }

  /** 重置表单 */
  const resetFields = () => {
    formRef.value?.restoreValidation()
    emit('reset', { ...defaultFormModel.value })
  }

  return {
    formRef,
    formModel,
    submit,
    resetFields,
  }
}
