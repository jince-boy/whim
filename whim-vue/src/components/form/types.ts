import type { ComputedRef } from 'vue'
import type { FormInst, FormProps, GridProps } from 'naive-ui'

/** 查询字段的摘要说明；格式化只影响标签，不改变请求值。 */
export interface SearchFormField<Values extends object> {
  key: Extract<keyof Values, string>
  label: string
  format?: (values: Readonly<Values>) => string
}

/** 已提交条件的可读摘要。 */
export interface SearchFormFilter {
  key: string
  label: string
  value: string
}

/** 原生表单配置保留类型提示，模型由 hook 独立维护。 */
export type SearchFormOptions<Values extends object> = Omit<FormProps, 'model'> & {
  /** 筛选区的无障碍名称，不显示独立标题栏。 */
  title?: string
  /** 是否默认折叠更多字段，默认 true；grid.collapsed 可覆盖此项。 */
  collapsed?: boolean
  defaultValues: Values
  fields?: SearchFormField<Values>[]
  grid?: GridProps
  searchText?: string
  resetText?: string
}

/** 注册时共享响应式配置，不复制表单状态。 */
export interface SearchFormBinding {
  title: ComputedRef<string>
  collapsed: ComputedRef<boolean>
  filters: ComputedRef<SearchFormFilter[]>
  formProps: ComputedRef<FormProps>
  gridProps: ComputedRef<GridProps>
  searchText: ComputedRef<string>
  resetText: ComputedRef<string>
  resetFields: () => void
  commitValues: () => void
  removeFilter: (key: string) => boolean
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
  filterChange: []
}
