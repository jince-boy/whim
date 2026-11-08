import type { FormRules } from 'naive-ui'
import type { StorageItem, StorageSave } from './types'

/** 按存储类型校验字段；相同平台修改时允许留空密钥。 */
export function createStorageRules(
  model: () => StorageSave,
  initial: () => StorageItem | undefined,
): FormRules {
  const requiredCloud = (value: string, label: string) =>
    model().platform === 'local' || !!value?.trim() || new Error(`请输入${label}`)
  return {
    name: [
      { required: true, whitespace: true, message: '请输入配置名称', trigger: 'blur' },
      { max: 64, message: '最多64个字符', trigger: 'blur' },
    ],
    platform: { required: true, message: '请选择存储类型', trigger: 'change' },
    basePath: {
      validator: (_, value: string) =>
        model().platform !== 'local' || !!value?.trim() || new Error('请输入服务器上的存储目录'),
      trigger: 'blur',
    },
    endpoint: {
      validator: (_, value: string) => requiredCloud(value, '服务地址'),
      trigger: 'blur',
    },
    bucket: { validator: (_, value: string) => requiredCloud(value, '存储桶'), trigger: 'blur' },
    accessKey: {
      validator: (_, value: string) => requiredCloud(value, 'Access Key'),
      trigger: 'blur',
    },
    region: {
      validator: (_, value: string) =>
        model().platform !== 'aliyunOss' || !!value?.trim() || new Error('请输入地域'),
      trigger: 'blur',
    },
    secretKey: {
      validator: (_, value: string) =>
        model().platform === 'local' ||
        !!value?.trim() ||
        initial()?.platform === model().platform ||
        new Error('请输入 Secret Key'),
      trigger: 'blur',
    },
  }
}
