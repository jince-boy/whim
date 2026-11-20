import { computed, ref, useTemplateRef } from 'vue'
import type { FormInst } from 'naive-ui'
import { getConfigRules } from './rule'
import { getEffectiveConfigValue, getSiteSettingField } from './useSiteSettings'
import type { ConfigFormModel, ConfigFormProps, ConfigOption, ValueType } from './types'

/** 每次打开弹窗创建独立草稿，取消时保留列表中的原值。 */
export function useConfigForm(props: ConfigFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const item = props.initialModel
  const formModel = ref<ConfigFormModel>({
    configName: item?.configName ?? '',
    configKey: item?.configKey ?? '',
    valueType: item?.valueType ?? 'TEXT',
    valueOptions: item?.valueOptions?.map(option => ({ ...option })) ?? [],
    configValue: item ? getEffectiveConfigValue(item) : '',
    remark: item?.remark ?? '',
  })
  const isSiteSetting = !!getSiteSettingField(item)
  const selectableOptions = computed(() => formModel.value.valueOptions.filter(option => option.label.trim() && option.value.trim()))
  const rules = computed(() => getConfigRules(formModel.value.valueType, selectableOptions))

  function newOption(): ConfigOption {
    return { label: '', value: '' }
  }

  function changeType(type: ValueType) {
    formModel.value.valueType = type
    formModel.value.valueOptions = type === 'RADIO' ? [newOption(), newOption()] : []
    if (type === 'RADIO') formModel.value.configValue = ''
    formRef.value?.restoreValidation()
  }

  return { formRef, formModel, selectableOptions, isSiteSetting, rules, changeType, newOption }
}
