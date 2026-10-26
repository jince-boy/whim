import { computed, h, onScopeDispose, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
import type { DictData, DictDataProps, DictDataQuery, DictDataResult } from './types'
import DictDataForm from '../form.vue'

/** 复用字典值面板，兼容类型管理页嵌入及已有独立路由。 */
export function useDictData(props: DictDataProps) {
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const type = computed(() => props.type || String(route.params.type || ''))
  const exporting = ref(false)
  const editingId = ref<string | number>()
  let disposed = false

  /** 行操作与工具栏使用一致的权限。 */
  const can = (permission: string) =>
    userStore.permissionCode.includes('*') || userStore.permissionCode.includes(permission)
  const canEdit = computed(() => can('system:dictData:update') && can('system:dictData:detail'))
  const canExport = computed(() => can('system:dictData:export'))

  const columns = computed<DataTableColumns<DictDataResult>>(() => [
    { type: 'selection', width: 40 },
    {
      title: '字典标签',
      key: 'label',
      width: 130,
      ellipsis: { tooltip: true },
      render: (row) =>
        h(NTag, { size: 'small', bordered: false, type: row.listClass }, () => row.label),
    },
    { title: '字典值', key: 'value', width: 110, ellipsis: { tooltip: true } },
    { title: '排序', key: 'sort', width: 60, align: 'center' },
    {
      title: '备注',
      key: 'remark',
      minWidth: 120,
      ellipsis: { tooltip: true },
      render: (row) => row.remark || '—',
    },
    { title: '创建时间', key: 'createTime', width: 180 },
    ...(canEdit.value || can('system:dictData:delete')
      ? [
          {
            title: '操作',
            key: 'action',
            width: 112,
            fixed: 'right' as const,
            render: (row: DictDataResult) =>
              h(NSpace, { size: 12 }, () => [
                canEdit.value
                  ? h(
                      NButton,
                      {
                        text: true,
                        size: 'small',
                        type: 'warning',
                        loading: editingId.value === row.id,
                        disabled: editingId.value !== undefined && editingId.value !== row.id,
                        onClick: () => openEditDialog(row.id),
                      },
                      () => '修改',
                    )
                  : null,
                can('system:dictData:delete')
                  ? h(
                      NButton,
                      {
                        text: true,
                        size: 'small',
                        type: 'error',
                        onClick: () => removeDictData([row.id], row.label),
                      },
                      () => '删除',
                    )
                  : null,
              ]),
          },
        ]
      : []),
  ])

  const [registerTable, table] = useWhimTable<DictDataResult, DictDataQuery>(() => ({
    title: `字典值 · ${props.name || type.value}`,
    columns,
    size: 'small',
    minHeight: 320,
    maxHeight: 480,
    rowKey: (row) => row.id,
    scrollX: 752,
    immediate: !!type.value,
    request: (query) => fetchDictDataPage({ ...query, type: type.value }),
    search: {
      defaultValues: { label: '' },
      size: 'medium',
      grid: { cols: '1 400:2', responsive: 'self', collapsedRows: 3 },
    },
    pagination: { size: 'small', pageSlot: 5 },
    toolbar: {
      buttons: ['add', 'delete'],
      permissions: { add: ['system:dictData:add'], delete: ['system:dictData:delete'] },
      showStriped: false,
    },
  }))

  /** 新增字典值时固定所属类型，避免保存到其他字典。 */
  function openAddDialog() {
    openFormDialog({
      title: '新增字典值',
      width: 560,
      formComponent: DictDataForm,
      formProps: {
        initialModel: {
          id: '',
          type: type.value,
          label: '',
          value: '',
          listClass: 'default',
          sort: 0,
          remark: '',
        },
      },
      onSubmit: insertDictData,
      successMessage: '新增字典值成功',
      onSuccess: table.reload,
    })
  }

  /** 读取最新数据后打开修改表单，切换类型后的旧请求不再打开弹窗。 */
  async function openEditDialog(id: string | number) {
    if (editingId.value !== undefined) return
    const currentType = type.value
    editingId.value = id
    try {
      const response = await fetchDictDataDetail({ id })
      if (disposed || currentType !== type.value) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: '修改字典值',
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
      editingId.value = undefined
    }
  }

  /** 单条和批量删除共用确认流程。 */
  function removeDictData(ids: Array<string | number>, label?: DictData['label']) {
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
      await exportDictData({ ...table.getSearchValues(), type: type.value })
    } catch {
      // 下载错误由公共请求封装提示。
    } finally {
      exporting.value = false
    }
  }

  /** 独立访问字典值页面时可返回类型管理。 */
  function backDictTypePage() {
    void router.push('/system/dictType')
  }

  watch(type, () => {
    if (type.value) void table.resetSearch()
  })
  onScopeDispose(() => (disposed = true))

  return {
    registerTable,
    form: table.searchModel,
    type,
    canExport,
    exporting,
    openAddDialog,
    removeDictData,
    handleExport,
    backDictTypePage,
  }
}
