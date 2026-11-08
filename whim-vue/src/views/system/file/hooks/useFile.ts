import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NSpace,
  NTag,
  useThemeVars,
  type DataTableColumns,
  type GlobalThemeOverrides,
  type UploadCustomRequestOptions,
} from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import StorageForm from '../form.vue'
import * as api from './api'
import type { FileItem, FileQuery, StorageItem } from './types'

/** 文件及存储配置共用一个页面，各操作遵循独立功能权限。 */
export function useFile() {
  const user = useUserStore()
  const dictionary = useDictStore()
  const themeVars = useThemeVars()
  const tabThemeOverrides = computed<GlobalThemeOverrides['Tabs']>(() => ({
    tabBorderRadius: '6px',
    tabPaddingMediumSegment: '8px 16px',
    tabTextColorActiveSegment: themeVars.value.primaryColor,
    tabTextColorHoverSegment: themeVars.value.primaryColor,
  }))
  const { openFormDialog, openDeleteDialog, openConfirmDialog } = useFormDialog()
  const can = (code: string) =>
    user.permissionCode.includes('*') || user.permissionCode.includes(`system:file:${code}`)
  const canView = computed(() => can('page'))
  const canStorage = computed(() => can('storageList'))
  const canUpload = computed(() => can('upload'))
  const canDelete = computed(() => can('delete'))
  const canDownload = computed(() => can('download'))
  const activeTab = ref(canView.value ? 'files' : 'storage')
  const uploading = ref(false)
  const uploadProgress = ref(0)
  const uploadLimit = ref<number>()
  const downloadingId = ref<string>()
  const deleting = ref(false)
  const platformLabel = (value: string) =>
    dictionary.getDictData('sys_file_storage_platform', value)

  /** 使用字节单位展示真实大小，兼容后端字符串形式的 Long。 */
  function formatSize(size: string | number) {
    const bytes = Number(size)
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  }

  const fileColumns = computed<DataTableColumns<FileItem>>(() => [
    ...(canDelete.value || canDownload.value
      ? [
          {
            type: 'selection' as const,
            width: 40,
            disabled: () => deleting.value || !!downloadingId.value,
          },
        ]
      : []),
    { title: '文件名称', key: 'originalName', minWidth: 200, ellipsis: { tooltip: true } },
    { title: '大小', key: 'fileSize', width: 100, render: (row) => formatSize(row.fileSize) },
    { title: '内容类型', key: 'contentType', width: 170, ellipsis: { tooltip: true } },
    { title: '存储配置', key: 'storageName', width: 150, ellipsis: { tooltip: true } },
    {
      title: '存储类型',
      key: 'platform',
      width: 120,
      render: (row) => {
        const item = platformLabel(row.platform)
        return h(NTag, { size: 'small', bordered: false, type: item.listClass }, () => item.label)
      },
    },
    { title: '上传时间', key: 'createTime', width: 180 },
    {
      title: '操作',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (row) =>
        h(NSpace, { size: 12, wrap: false }, () => [
          can('download')
            ? h(
                NButton,
                {
                  text: true,
                  size: 'small',
                  type: 'info',
                  loading: downloadingId.value === row.id,
                  disabled: !!downloadingId.value || deleting.value,
                  onClick: () => download(row),
                },
                () => '下载',
              )
            : null,
          can('delete')
            ? h(
                NButton,
                {
                  text: true,
                  size: 'small',
                  type: 'error',
                  disabled: deleting.value || !!downloadingId.value,
                  onClick: () => removeFile(row),
                },
                () => '删除',
              )
            : null,
        ]),
    },
  ])
  const [registerFiles, files] = useWhimTable<FileItem, FileQuery>(() => ({
    title: '文件列表',
    columns: fileColumns,
    rowKey: (row) => row.id,
    scrollX: 1080,
    request: api.getFilePage,
    immediate: canView.value,
    search: {
      defaultValues: { originalName: '', storageId: null },
      fields: [
        { key: 'originalName', label: '文件名称' },
        ...(canStorage.value ? [{ key: 'storageId' as const, label: '存储配置' }] : []),
      ],
    },
  }))
  const selectedFiles = computed(() => {
    const selectedIds = new Set(files.getCheckedRowKeys())
    return files.data.value.filter((row) => selectedIds.has(row.id))
  })
  const storageColumns = computed<DataTableColumns<StorageItem>>(() => [
    {
      title: '配置名称',
      key: 'name',
      minWidth: 160,
      render: (row) =>
        h(NSpace, { align: 'center', size: 8 }, () => [
          row.name,
          row.defaultFlag === 1
            ? h(NTag, { size: 'small', type: 'success', bordered: false }, () => '默认配置')
            : null,
        ]),
    },
    {
      title: '存储类型',
      key: 'platform',
      width: 120,
      render: (row) => platformLabel(row.platform).label,
    },
    {
      title: '目录 / 对象前缀',
      key: 'basePath',
      minWidth: 180,
      ellipsis: { tooltip: true },
      render: (row) => row.basePath || '—',
    },
    {
      title: '服务地址',
      key: 'endpoint',
      minWidth: 190,
      ellipsis: { tooltip: true },
      render: (row) => row.endpoint || '—',
    },
    {
      title: '存储桶',
      key: 'bucket',
      width: 140,
      ellipsis: { tooltip: true },
      render: (row) => row.bucket || '—',
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 230,
      render: (row) =>
        h(NSpace, { size: 12, wrap: false }, () => [
          can('updateStorage')
            ? h(
                NButton,
                { text: true, size: 'small', type: 'warning', onClick: () => editStorage(row) },
                () => '修改',
              )
            : null,
          can('defaultStorage') && row.defaultFlag !== 1
            ? h(
                NButton,
                { text: true, size: 'small', type: 'info', onClick: () => setDefault(row) },
                () => '设为默认配置',
              )
            : null,
          can('deleteStorage')
            ? h(
                NButton,
                {
                  text: true,
                  size: 'small',
                  type: 'error',
                  disabled: row.defaultFlag === 1,
                  onClick: () => removeStorage(row),
                },
                () => '删除',
              )
            : null,
        ]),
    },
  ])
  const [registerStorage, storages] = useWhimTable<StorageItem, Record<string, never>>(() => ({
    title: '存储配置',
    columns: storageColumns,
    rowKey: (row) => row.id,
    request: api.getStorageList,
    immediate: canStorage.value,
    scrollX: 1100,
    toolbar: { buttons: ['add'], permissions: { add: ['system:file:addStorage'] } },
  }))
  const storageOptions = computed(() =>
    storages.data.value.map((row) => ({ label: row.name, value: row.id })),
  )
  /** 新增配置后立即刷新列表和历史文件筛选项。 */
  function addStorage() {
    openFormDialog({
      title: '新增存储配置',
      width: 620,
      formComponent: StorageForm,
      onSubmit: api.addStorage,
      onSuccess: () => storages.reload(),
    })
  }

  /** 编辑草稿不携带密钥，空密钥由服务端保留原值。 */
  function editStorage(row: StorageItem) {
    openFormDialog({
      title: '修改存储配置',
      width: 620,
      formComponent: StorageForm,
      formProps: { initialModel: row },
      onSubmit: api.updateStorage,
      onSuccess: () => storages.reload(),
    })
  }

  /** 切换后所有新上传统一使用该配置，历史文件保持原位置。 */
  function setDefault(row: StorageItem) {
    openConfirmDialog({
      title: '设置默认配置',
      content: `后续所有文件统一上传到“${row.name}”，历史文件位置保持不变。`,
      onSubmit: () => api.updateDefaultStorage(row.id),
      onSuccess: () => storages.reload(),
    })
  }

  /** 删除前由后端检查默认项及文件引用。 */
  function removeStorage(row: StorageItem) {
    openDeleteDialog({
      content: `确定删除存储配置“${row.name}”？仍有文件的配置无法删除。`,
      onSubmit: () => api.deleteStorage(row.id),
      onSuccess: async () => {
        if (files.searchModel.value.storageId === row.id) files.searchModel.value.storageId = null
        await storages.reload()
      },
    })
  }

  /** 删除实际对象需要明确确认，成功后刷新文件列表。 */
  function removeFile(row: FileItem) {
    if (deleting.value || downloadingId.value) return
    openDeleteDialog({
      content: `确定永久删除文件“${row.originalName}”？存储中的文件会同时删除，无法恢复。`,
      onSubmit: () => api.deleteFile(row.id),
      onSuccess: () => files.reload(),
    })
  }

  /** 确认后逐项删除，遇到失败停止并保留尚未删除的选择，避免重复处理成功项。 */
  function removeSelectedFiles() {
    if (deleting.value || downloadingId.value || !selectedFiles.value.length) return
    const selected = [...selectedFiles.value]
    openDeleteDialog({
      title: '删除文件',
      content: `确定永久删除选中的 ${selected.length} 个文件？存储中的文件会同时删除，无法恢复。`,
      successMessage: false,
      onSubmit: async () => {
        deleting.value = true
        let deletedCount = 0
        try {
          // 复用已鉴权的删除接口，存储删除失败时停止后续操作，保留记录供重试。
          for (const row of selected) {
            const response = await api.deleteFile(row.id)
            if (response.code !== 200) {
              message.error(response.message)
              break
            }
            deletedCount++
          }
        } catch {
          // 请求错误由公共拦截器提示；下方同时报告本次实际完成的数量。
        } finally {
          files.setCheckedRowKeys(selected.slice(deletedCount).map((row) => row.id))
          if (deletedCount) await files.reload()
          deleting.value = false
        }
        if (deletedCount === selected.length) message.success(`已删除 ${deletedCount} 个文件`)
        else
          message.warning(
            `已删除 ${deletedCount} 个文件，剩余 ${selected.length - deletedCount} 个未删除，可重新勾选后重试`,
          )
        return true
      },
    })
  }

  /** 工具栏下载当前唯一选中的文件，与行内下载共用认证和加载状态。 */
  async function downloadSelectedFile() {
    if (selectedFiles.value.length !== 1) return
    const file = selectedFiles.value[0]
    if (file) await download(file)
  }

  /** 下载请求通过公共客户端携带登录凭据。 */
  async function download(row: FileItem) {
    if (downloadingId.value || deleting.value) return
    downloadingId.value = row.id
    try {
      await api.downloadManagedFile(row)
    } catch {
      /* 公共请求层提示错误。 */
    } finally {
      downloadingId.value = undefined
    }
  }

  /** 校验真实文件大小，上传完成后才刷新服务器列表。 */
  async function upload(options: UploadCustomRequestOptions) {
    const file = options.file.file
    if (!file || uploading.value) {
      options.onError()
      return
    }
    if (!uploadLimit.value || !file.size || file.size > uploadLimit.value) {
      message.error(`请选择非空且不超过 ${formatSize(uploadLimit.value ?? 0)} 的文件`)
      options.onError()
      return
    }
    uploading.value = true
    uploadProgress.value = 0
    try {
      const result = await api.uploadFile(file, (percent) => {
        uploadProgress.value = percent
        options.onProgress({ percent })
      })
      if (result.code !== 200) {
        message.error(result.message)
        options.onError()
        return
      }
      options.onFinish()
      message.success('文件上传成功')
      await files.reload()
    } catch {
      options.onError()
    } finally {
      uploading.value = false
    }
  }

  /** 从真实后端获取限制，加载失败时保持上传禁用。 */
  async function loadUploadLimit() {
    if (!canUpload.value) return
    try {
      const response = await api.getUploadLimit()
      if (response.code === 200) uploadLimit.value = Number(response.data)
      else message.error(response.message)
    } catch {
      /* 公共请求层提示错误。 */
    }
  }
  onMounted(loadUploadLimit)
  return {
    themeVars,
    tabThemeOverrides,
    can,
    canView,
    canStorage,
    canUpload,
    canDelete,
    canDownload,
    activeTab,
    registerFiles,
    registerStorage,
    form: files.searchModel,
    storageOptions,
    uploading,
    uploadProgress,
    deleting,
    downloadingId,
    selectedFiles,
    uploadLimit,
    formatSize,
    addStorage,
    upload,
    loadUploadLimit,
    removeSelectedFiles,
    downloadSelectedFile,
  }
}
