import type { FormInst } from 'naive-ui'
import type { DictType, DictTypeFormProps } from './types'

/** 创建独立的字典类型编辑草稿，避免修改父组件的初始数据。 */
export function useDictTypeForm(props: DictTypeFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const defaultFormModel: DictType = { id: '', name: '', type: '', remark: '' }
  const formModel = ref<DictType>({ ...defaultFormModel, ...props.initialModel })

  // 当前模型字段均为标量；复制对象即可隔离编辑草稿。
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = { ...defaultFormModel, ...model }
      formRef.value?.restoreValidation()
    },
  )

  return { formRef, formModel }
}
