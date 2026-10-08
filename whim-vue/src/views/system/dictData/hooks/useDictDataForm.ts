import type { FormInst, SelectOption } from 'naive-ui'
import type { DictData, DictDataFormProps } from './types'

/** 创建字典数据编辑草稿及回显样式选项。 */
export function useDictDataForm(props: DictDataFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const defaultFormModel: DictData = {
    id: '',
    dictType: '',
    label: '',
    value: '',
    listClass: 'default',
    sort: 0,
    remark: '',
  }
  const formModel = ref<DictData>({ ...defaultFormModel, ...props.initialModel })
  const options: SelectOption[] = [
    { label: '默认', value: 'default' },
    { label: '主要', value: 'primary' },
    { label: '信息', value: 'info' },
    { label: '成功', value: 'success' },
    { label: '警告', value: 'warning' },
    { label: '危险', value: 'error' },
  ]

  // 当前模型字段均为标量；复制对象即可隔离编辑草稿。
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = { ...defaultFormModel, ...model }
      formRef.value?.restoreValidation()
    },
  )

  return { formRef, formModel, options }
}
