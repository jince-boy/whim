import type { FormRules } from 'naive-ui'
import type { Ref } from 'vue'
import type { ConfigOption, ValueType } from './types'

const configRules: FormRules = {
  configName: [
    { required: true, whitespace: true, message: '请输入配置名称', trigger: 'blur' },
    { max: 128, message: '配置名称不能超过128个字符', trigger: 'blur' },
  ],
  configKey: [
    { required: true, whitespace: true, message: '请输入配置键', trigger: 'blur' },
    { max: 128, message: '配置键不能超过128个字符', trigger: 'blur' },
  ],
  remark: { max: 255, message: '备注不能超过255个字符', trigger: 'blur' },
}

export function isValidConfigJson(value: string): boolean {
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

export function getConfigRules(valueType: ValueType, selectableOptions: Readonly<Ref<ConfigOption[]>>): FormRules {
  if (valueType === 'JSON') {
    return {
      ...configRules,
      configValue: { trigger: 'blur', validator: (_, value: string) => isValidConfigJson(value) || new Error('请输入有效的 JSON') },
    }
  }
  if (valueType !== 'RADIO') return configRules
  return {
    ...configRules,
    valueOptions: [
      {
        validator: (_, options: ConfigOption[]) => {
          if (!options.length) return new Error('请至少添加一个选项')
          return options.length === selectableOptions.value.length || new Error('请补全每个选项的名称和值')
        },
      },
      {
        trigger: ['input', 'blur'],
        validator: (_, options: ConfigOption[]) => {
          const labels = options.map(option => option.label.trim()).filter(Boolean)
          const values = options.map(option => option.value).filter(Boolean)
          if (new Set(labels).size !== labels.length) return new Error('单选选项名称不能重复')
          return new Set(values).size === values.length || new Error('单选选项值不能重复')
        },
      },
    ],
    configValue: {
      trigger: 'change',
      validator: (_, value: string) => selectableOptions.value.some(option => option.value === value) || new Error('请选择一个配置值'),
    },
  }
}
