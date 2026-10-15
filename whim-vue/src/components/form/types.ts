import type { ComputedRef } from 'vue'
import type { FormInst, FormProps, GridProps } from 'naive-ui'

/** 原生表单配置保留类型提示，模型由 hook 独立维护。 */
export type SearchFormOptions<Values extends object> = Omit<FormProps, 'model'> & {
  defaultValues: Values
  grid?: GridProps
  searchText?: string
  resetText?: string
}

/** 注册时共享响应式配置，不复制表单状态。 */
export interface SearchFormBinding {
  formProps: ComputedRef<FormProps>
  gridProps: ComputedRef<GridProps>
  searchText: ComputedRef<string>
  resetText: ComputedRef<string>
  resetFields: () => void
}

/** 组件提供的注册入口与命令式方法。 */
export interface SearchFormRegistration {
  connect: (binding: SearchFormBinding) => void
  validate: FormInst['validate']
  restoreValidation: FormInst['restoreValidation']
  submit: () => Promise<boolean>
  reset: () => void
}

export type SearchFormRegister = (instance: SearchFormRegistration | undefined) => void

export interface SearchFormEmits {
  register: [instance: SearchFormRegistration | undefined]
  submit: []
  reset: []
}
