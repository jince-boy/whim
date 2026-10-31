import { computed, h, onDeactivated, onScopeDispose, ref } from 'vue'
import { NButton, NSpace, NTag, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { message } from '@/plugins'
import {
  deleteDictData,
  exportDictData,
  fetchDictDataDetail,
  fetchDictDataPage,
  insertDictData,
  updateDictData,
} from './api'
import type { DictDataProps, DictDataQuery, DictDataResult } from './types'
import DictDataForm from '../form.vue'

/** 管理当前类型的数据；类型切换由父工作区重建面板，隔离请求与勾选状态。 */
export function useDictData(props: DictDataProps) {
  const userStore = useUserStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const exporting = ref(false)
  const editingId = ref<string>()
  let editVersion = 0

  /** 行操作与工具栏使用一致的权限。 */
  const can = (permission: string) =>
    userStore.permissionCode.includes('*') || userStore.permissionCode.includes(permission)
  const canEdit = computed(() => can('system:dictData:update') && can('system:dictData:detail'))
  const canAdd = computed(() => can('system:dictData:add'))
  const canDelete = computed(() => can('system:dictData:delete'))
  const canExport = computed(() => can('system:dictData:export'))

  const columns = computed(() => {
    const columns: DataTableColumns<DictDataResult> = [
      ...(canDelete.value ? [{ type: 'selection' as const, width: 40 }] : []),
      {
        title: '字典标签',
        key: 'label',
        width: 130,
        ellipsis: { tooltip: true },
      },
      { title: '字典值', key: 'value', width: 110, ellipsis: { tooltip: true } },
      { title: '排序', key: 'sort', width: 60, align: 'center' },
      {
        title: '展示效果',
        key: 'listClass',
        width: 130,
        render: (row) =>
          h(
            NTag,
            {
              bordered: false,
              type: row.listClass,
              style: 'max-width: 100%; overflow: hidden',
            },
            () => row.label,
          ),
      },
      {
        title: '备注',
        key: 'remark',
        minWidth: 120,
        ellipsis: { tooltip: true },
        render: (row) => row.remark || '—',
      },
      { title: '创建时间', key: 'createTime', width: 200 },
    ]
    if (canEdit.value || canDelete.value) {
      columns.push({
        title: '操作',
        key: 'action',
        width: 112,
        fixed: 'right',
        render: (row) =>
          h(NSpace, { size: 12 }, () => [
            canEdit.value
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'warning',
                    loading: editingId.value === row.id,
                    disabled: editingId.value !== undefined && editingId.value !== row.id,
                    onClick: () => openEditDialog(row.id),
                  },
                  () => '修改',
                )
              : null,
            canDelete.value
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'error',
                    onClick: () => removeDictData([row.id], row.label),
                  },
                  () => '删除',
                )
              : null,
          ]),
      })
    }
    return columns
  })

  const [registerTable, table] = useWhimTable<DictDataResult, DictDataQuery>(() => ({
    title: '字典数据',
    columns,
    rowKey: (row) => row.id,
    scrollX: 902,
    request: (query) => fetchDictDataPage({ ...query, type: props.type }),
    search: {
      defaultValues: { label: '' },
      fields: [{ key: 'label', label: '字典标签' }],
    },
    pagination: { pageSlot: 5 },
    toolbar: {
      buttons: ['delete'],
      permissions: { delete: ['system:dictData:delete'] },
    },
  }))

  /** 新增字典值时固定所属类型，避免保存到其他字典。 */
  function openAddDialog() {
    openFormDialog({
      title: `新增数据 · ${props.name}`,
      width: 560,
      formComponent: DictDataForm,
      formProps: { initialModel: { type: props.type } },
      onSubmit: insertDictData,
      successMessage: '新增字典值成功',
      onSuccess: table.reload,
    })
  }

  /** 读取最新数据后打开修改表单，切换类型后的旧请求不再打开弹窗。 */
  async function openEditDialog(id: string) {
    if (editingId.value !== undefined) return
    const version = ++editVersion
    editingId.value = id
    try {
      const response = await fetchDictDataDetail({ id })
      if (version !== editVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: `编辑数据 · ${props.name}`,
        width: 560,
        formComponent: DictDataForm,
        formProps: { initialModel: response.data },
        onSubmit: updateDictData,
        successMessage: '修改字典值成功',
        onSuccess: table.reload,
      })
    } catch {
      // 请求错误由公共拦截器提示。
    } finally {
      if (version === editVersion) editingId.value = undefined
    }
  }

  /** 单条和批量删除共用确认流程。 */
  function removeDictData(ids: Array<string | number>, label?: string) {
    openDeleteDialog({
      content: label
        ? `确定删除字典值“${label}”？删除后无法恢复。`
        : `确定删除选中的 ${ids.length} 个字典值？删除后无法恢复。`,
      onSubmit: () => deleteDictData({ dictDataIds: ids.join(',') }),
      onSuccess: async () => {
        table.setCheckedRowKeys([])
        await table.reload()
      },
    })
  }

  /** 导出当前类型下符合查询条件的字典值。 */
  async function handleExport() {
    if (exporting.value) return
    exporting.value = true
    try {
      await exportDictData({ ...table.getSearchValues(), type: props.type })
    } catch {
      // 下载错误由公共请求封装提示。
    } finally {
      exporting.value = false
    }
  }

  // 切换类型由父组件重建面板；离开页面后只需使未完成的详情请求失效。
  onScopeDispose(() => editVersion++)
  onDeactivated(() => {
    editVersion++
    editingId.value = undefined
  })

  return {
    registerTable,
    form: table.searchModel,
    canAdd,
    canExport,
    exporting,
    openAddDialog,
    removeDictData,
    handleExport,
  }
}
