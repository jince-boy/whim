export type ConfigId = string | number

export type ValueType = 'TEXT' | 'RADIO' | 'JSON' | 'IMAGE'

export interface ConfigOption {
  label: string
  value: string
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
  imageFile: File | null
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

export interface ConfigImageProps {
  configId?: ConfigId
  fileId?: string | null
  file?: File | null
  siteLogo?: boolean
  size?: number
}

export interface SiteSettings {
  logoFileId: string | null
  title: string | null
  description: string | null
  copyright: string | null
}
