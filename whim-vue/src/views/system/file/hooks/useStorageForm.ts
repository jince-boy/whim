import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import type { StorageFormProps, StorageSave } from './types'
import { createStorageRules } from './rule'

/** 存储配置使用独立草稿，密钥永不从接口回填。 */
export function useStorageForm(props: StorageFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const dictionary = useDictStore()
  const defaults: StorageSave = {
    name: '',
    platform: 'local',
    basePath: './data/files',
    endpoint: '',
    bucket: '',
    region: '',
    accessKey: '',
    secretKey: '',
  }
  const formModel = ref<StorageSave>({ ...defaults, ...props.initialModel, secretKey: '' })
  const platformOptions = computed(() => dictionary.getDictOptions('sys_file_storage_platform'))
  const isLocal = computed(() => formModel.value.platform === 'local')
  const rules = createStorageRules(
    () => formModel.value,
    () => props.initialModel,
  )
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = { ...defaults, ...model, secretKey: '' }
      formRef.value?.restoreValidation()
    },
  )
  watch(
    () => formModel.value.platform,
    (platform) => {
      formModel.value.basePath = platform === 'local' ? './data/files' : ''
      formModel.value.secretKey = ''
      formRef.value?.restoreValidation()
    },
  )
  return { formRef, formModel, platformOptions, isLocal, rules }
}
