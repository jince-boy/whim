export type ConfigId = string | number

export type ValueType = 'TEXT' | 'RADIO' | 'JSON'

export interface ConfigOption {
  label: string
  value: string
}

export const valueTypeLabels: Record<ValueType, string> = {
  TEXT: '文本',
  RADIO: '单选',
  JSON: 'JSON',
}

export interface ConfigQuery {
  configName: string
  configKey: string
  remark: string
}

export interface ConfigItem {
  id: ConfigId
  configName: string
  configKey: string
  configValue: string | null
  valueType: ValueType
  valueOptions: ConfigOption[] | null
  remark: string | null
  builtIn: boolean
}

export interface ConfigFormModel {
  configName: string
  configKey: string
  configValue: string
  valueType: ValueType
  valueOptions: ConfigOption[]
  remark: string
}

export interface ConfigFormProps {
  initialModel?: ConfigItem
}

export interface SiteSettings {
  title: string | null
  description: string | null
  copyright: string | null
}
