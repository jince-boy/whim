import { computed, ref, useTemplateRef } from 'vue'
import type { FormInst, UploadFileInfo } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import { getConfigRules } from './rule'
import { getEffectiveConfigValue, getSiteSettingField } from './useSiteSettings'
import type { ConfigFormModel, ConfigFormProps, ConfigOption, ValueType } from './types'

/** 每次打开弹窗创建独立草稿，取消时保留列表中的原值。 */
export function useConfigForm(props: ConfigFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const item = props.initialModel
  const dictionary = useDictStore()
  const valueTypeOptions = computed(() => dictionary.getDictOptions('sys_config_value_type'))
  const formModel = ref<ConfigFormModel>({
    imageFile: null,
    configName: item?.configName ?? '',
    configKey: item?.configKey ?? '',
    valueType: item?.valueType ?? 'TEXT',
    valueOptions: item?.valueOptions?.map(option => ({ ...option })) ?? [],
    configValue: item ? getEffectiveConfigValue(item) : '',
    remark: item?.remark ?? '',
  })
  const siteField = getSiteSettingField(item)
  const isSiteSetting = !!siteField
  const isSiteLogo = siteField === 'logoFileId'
  const logoCropFile = ref<File | null>(null)
  const selectableOptions = computed(() => formModel.value.valueOptions.filter(option => option.label.trim() && option.value.trim()))
  const rules = computed(() => getConfigRules(formModel.value.valueType, selectableOptions))

  function newOption(): ConfigOption {
    return { label: '', value: '' }
  }

  function changeType(type: ValueType) {
    if (type === 'IMAGE' || formModel.value.valueType === 'IMAGE' || type === 'RADIO') {
      formModel.value.configValue = ''
      formModel.value.imageFile = null
    }
    formModel.value.valueType = type
    formModel.value.valueOptions = type === 'RADIO' ? [newOption(), newOption()] : []
    formRef.value?.restoreValidation()
  }

  function selectImage({ file }: { file: UploadFileInfo }) {
    const image = file.file
    if (!image) return
    if (!['image/png', 'image/jpeg', 'image/gif'].includes(image.type) || !image.size || image.size > 20 * 1024 * 1024) {
      message.warning('请选择不超过20 MiB的 PNG、JPG 或 GIF 图片')
      return
    }
    if (isSiteLogo) {
      // 先交给裁剪器，取消或导出失败时保留当前 Logo 草稿。
      logoCropFile.value = image
    } else {
      formModel.value.imageFile = image
    }
  }

  function clearImage() {
    formModel.value.imageFile = null
    formModel.value.configValue = ''
  }

  return { formRef, formModel, selectableOptions, isSiteSetting, isSiteLogo, logoCropFile, valueTypeOptions, rules, changeType, newOption, selectImage, clearImage }
}
