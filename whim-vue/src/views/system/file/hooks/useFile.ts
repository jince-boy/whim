import { computed, h, ref } from 'vue'
import dayjs from 'dayjs'
import {
  NButton,
  NSpace,
  NTag,
  useThemeVars,
  type DataTableColumns,
  type GlobalThemeOverrides,
} from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import StorageForm from '../form.vue'
import FileThumbnail from '../components/FileThumbnail.vue'
import * as api from './api'
import { useFileUpload } from './useFileUpload'
import { formatFileSize as formatSize } from '../utils'
import type { FileItem, FileQuery, StorageItem } from './types'

/** 文件及存储配置共用一个页面，各操作遵循独立功能权限。 */
export function useFile() {
  const user = useUserStore()
  const dictionary = useDictStore()
  const themeVars = useThemeVars()
  const tabThemeOverrides = computed<GlobalThemeOverrides['Tabs']>(() => ({
    tabBorderRadius: '6px',
    tabTextColorActiveSegment: themeVars.value.primaryColor,
    tabTextColorHoverSegment: themeVars.value.primaryColor,
  }))
  const { openFormDialog, openDeleteDialog, openConfirmDialog } = useFormDialog()
  const can = (code: string) => user.hasPermission(`system:file:${code}`)
  const canView = computed(() => can('page'))
  const canStorage = computed(() => can('storageList'))
  const canUpload = computed(() => can('upload'))
  const canDelete = computed(() => can('delete'))
  const activeTab = ref(canView.value ? 'files' : 'storage')
  const downloadingId = ref<string>()
  const deleting = ref(false)
  const platformLabel = (value: string) =>
    dictionary.getDictData('sys_file_storage_platform', value)
  const platformOptions = computed(() => dictionary.getDictOptions('sys_file_storage_platform'))
  const uploadSourceOptions = computed(() => dictionary.getDictOptions('sys_file_upload_source'))

  const fileColumns = computed<DataTableColumns<FileItem>>(() => [
    ...(canDelete.value
      ? [
          {
            type: 'selection' as const,
            width: 40,
            disabled: () => deleting.value || !!downloadingId.value,
          },
        ]
      : []),
    {
      title: '缩略图',
      key: 'thumbnail',
      width: 80,
      align: 'center',
      render: (row) => h(FileThumbnail, { file: row, canPreview: can('download') }),
    },
    { title: '文件名称', key: 'fileName', minWidth: 220, ellipsis: { tooltip: true } },
    { title: '原文件名称', key: 'originalName', minWidth: 220, ellipsis: { tooltip: true } },
    { title: '文件后缀', key: 'fileSuffix', width: 90, render: (row) => row.fileSuffix || '—' },
    { title: '大小', key: 'fileSize', width: 100, render: (row) => formatSize(row.fileSize) },
    { title: '文件类型', key: 'contentType', width: 170, ellipsis: { tooltip: true } },
    {
      title: '上传途径',
      key: 'uploadSource',
      width: 140,
      ellipsis: { tooltip: true },
      render: (row) => {
        if (!row.uploadSource) return '—'
        const item = dictionary.getDictData('sys_file_upload_source', row.uploadSource)
        return h(NTag, { size: 'small', bordered: false, type: item.listClass }, () => item.label)
      },
    },
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
    {
      title: '上传人',
      key: 'createByName',
      width: 140,
      ellipsis: { tooltip: true },
      render: (row) => row.createByName || row.createBy || '—',
    },
    { title: '上传时间', key: 'createTime', width: 180 },
    {
      title: '操作',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (row) =>
        h(NSpace, { size: 12, align: 'center', wrap: false, wrapItem: false }, () => [
          can('download')
            ? h(
                NButton,
                {
                  text: true,
                  size: 'small',
                  type: 'info',
                  'aria-label': '下载',
                  'aria-busy': downloadingId.value === row.id,
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
    scrollX: 1900,
    request: ({ dateRange, ...query }) =>
      api.getFilePage({
        ...query,
        fileName: query.fileName.trim(),
        originalName: query.originalName.trim(),
        fileSuffix: query.fileSuffix.trim(),
        startTime: dateRange ? dayjs(dateRange[0]).format('YYYY-MM-DD HH:mm:ss') : undefined,
        endTime: dateRange ? dayjs(dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : undefined,
      }),
    immediate: canView.value,
    search: {
      defaultValues: {
        fileName: '',
        originalName: '',
        fileSuffix: '',
        storageId: null,
        platform: null,
        uploadSource: null,
        dateRange: null,
      },
      fields: [
        { key: 'fileName', label: '文件名称' },
        { key: 'originalName', label: '原文件名称' },
        ...(canStorage.value
          ? [
              {
                key: 'storageId' as const,
                label: '存储配置',
                format: (values: Readonly<FileQuery>) =>
                  storageOptions.value.find((item) => item.value === values.storageId)?.label ??
                  String(values.storageId),
              },
            ]
          : []),
        { key: 'fileSuffix', label: '文件后缀' },
        {
          key: 'platform',
          label: '存储类型',
          format: (values) => platformLabel(values.platform ?? '').label,
        },
        {
          key: 'uploadSource',
          label: '上传途径',
          format: (values) =>
            dictionary.getDictData('sys_file_upload_source', values.uploadSource ?? '').label,
        },
        {
          key: 'dateRange',
          label: '上传时间',
          format: (values) =>
            values.dateRange
              ?.map((time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'))
              .join(' 至 ') ?? '',
        },
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
      content: `确定删除存储配置“${row.name}”？仍有文件或未完成上传任务的配置无法删除。`,
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
      onSubmit: async () => {
        deleting.value = true
        try {
          return await api.deleteFile(row.id)
        } finally {
          deleting.value = false
        }
      },
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
          try {
            if (deletedCount) await files.reload()
          } finally {
            deleting.value = false
          }
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

  const uploads = useFileUpload(canUpload, () => files.reload())
  return {
    themeVars,
    tabThemeOverrides,
    canView,
    canStorage,
    canUpload,
    canDelete,
    activeTab,
    registerFiles,
    registerStorage,
    form: files.searchModel,
    storageOptions,
    platformOptions,
    uploadSourceOptions,
    ...uploads,
    deleting,
    downloadingId,
    selectedFiles,
    formatSize,
    addStorage,
    removeSelectedFiles,
  }
}
