import { computed, h, onScopeDispose, ref, shallowRef, watch } from 'vue'
import { NButton, useThemeVars, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { message } from '@/plugins'
import {
  deleteDictType,
  exportDictType,
  fetchDictTypeDetail,
  fetchDictTypePage,
  insertDictType,
  resetDictCache,
  updateDictType,
} from './api'
import type { DictTypeQuery, DictTypeResult } from './types'
import DictTypeForm from '../form.vue'

/** 管理字典类型及当前选中项，右侧字典值由选中类型驱动。 */
export function useDictType() {
  const userStore = useUserStore()
  const themeVars = useThemeVars()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const selectedType = shallowRef<DictTypeResult>()
  const resettingCache = ref(false)
  const exporting = ref(false)
  const editingId = ref<string | number>()
  let disposed = false

  /** 按当前用户授权显示业务操作。 */
  const can = (permission: string) =>
    userStore.permissionCode.includes('*') || userStore.permissionCode.includes(permission)
  const canEdit = computed(() => can('system:dictType:update') && can('system:dictType:detail'))
  const canViewData = computed(() => can('system:dictData:page'))
  const canResetCache = computed(() => can('system:dictType:reset'))
  const canExport = computed(() => can('system:dictType:export'))

  const columns = computed<DataTableColumns<DictTypeResult>>(() => [
    { type: 'selection', width: 40 },
    {
      title: '字典名称',
      key: 'name',
      width: 130,
      ellipsis: { tooltip: true },
      render: (row) =>
        h(
          NButton,
          {
            text: true,
            class: 'dict-type-name',
            type: selectedType.value?.id === row.id ? 'primary' : 'default',
            'aria-pressed': selectedType.value?.id === row.id,
            onClick: () => (selectedType.value = row),
          },
          () => row.name,
        ),
    },
    {
      title: '类型编码',
      key: 'type',
      width: 150,
      ellipsis: { tooltip: true },
      render: (row) => h('span', { class: 'dict-type-code' }, row.type),
    },
    {
      title: '备注',
      key: 'remark',
      minWidth: 120,
      ellipsis: { tooltip: true },
      render: (row) => row.remark || '—',
    },
    { title: '创建时间', key: 'createTime', width: 180 },
    ...(canEdit.value
      ? [
          {
            title: '操作',
            key: 'action',
            width: 70,
            fixed: 'right' as const,
            render: (row: DictTypeResult) =>
              h(
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
              ),
          },
        ]
      : []),
  ])

  const [registerTable, table] = useWhimTable<DictTypeResult, DictTypeQuery>({
    title: '字典类型',
    columns,
    size: 'small',
    minHeight: 320,
    maxHeight: 480,
    scrollX: 790,
    rowKey: (row) => row.id,
    request: fetchDictTypePage,
    rowClassName: (row) => (row.id === selectedType.value?.id ? 'dict-type-active' : ''),
    rowProps: (row) => ({
      style: 'cursor: pointer',
      onClick: (event: MouseEvent) => {
        if ((event.target as HTMLElement).closest('button, input, .n-checkbox')) return
        selectedType.value = row as DictTypeResult
      },
    }),
    search: {
      defaultValues: { name: '', type: '' },
      fields: [
        { key: 'name', label: '字典名称' },
        { key: 'type', label: '类型编码' },
      ],
      size: 'medium',
      collapsed: false,
    },
    pagination: { size: 'small', pageSlot: 5 },
    toolbar: {
      buttons: ['add', 'delete'],
      permissions: { add: ['system:dictType:add'], delete: ['system:dictType:delete'] },
      showStriped: false,
    },
  })

  // 刷新保留当前类型；查询、翻页或删除后选中当前页第一项。
  watch(table.data, (rows) => {
    selectedType.value = rows.find((row) => row.id === selectedType.value?.id) ?? rows[0]
  })

  /** 打开字典类型新增表单。 */
  function openAddDialog() {
    openFormDialog({
      title: '新增字典类型',
      width: 520,
      formComponent: DictTypeForm,
      onSubmit: insertDictType,
      successMessage: '新增字典类型成功',
      onSuccess: table.reload,
    })
  }

  /** 读取最新详情后编辑，避免重复打开弹窗。 */
  async function openEditDialog(id: string | number) {
    if (editingId.value !== undefined) return
    editingId.value = id
    try {
      const response = await fetchDictTypeDetail({ id })
      if (disposed) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: '修改字典类型',
        width: 520,
        formComponent: DictTypeForm,
        formProps: { initialModel: response.data },
        onSubmit: updateDictType,
        successMessage: '修改字典类型成功',
        onSuccess: table.reload,
      })
    } catch {
      // 请求错误由公共拦截器提示。
    } finally {
      editingId.value = undefined
    }
  }

  /** 删除勾选类型，并在刷新后重新确认右侧归属。 */
  function removeDictTypeBatch(ids: Array<string | number>) {
    openDeleteDialog({
      content: `确定删除选中的 ${ids.length} 个字典类型？请先清空类型下的字典值，删除后无法恢复。`,
      onSubmit: () => deleteDictType({ dictTypeIds: ids.join(',') }),
      onSuccess: async () => {
        table.setCheckedRowKeys([])
        if (selectedType.value && ids.includes(selectedType.value.id))
          selectedType.value = undefined
        await table.reload()
      },
    })
  }

  /** 导出当前筛选条件下的字典类型，不受分页和勾选影响。 */
  async function handleExport() {
    if (exporting.value) return
    exporting.value = true
    try {
      await exportDictType(table.getSearchValues())
    } catch {
      // 下载错误由公共请求封装提示。
    } finally {
      exporting.value = false
    }
  }

  /** 刷新缓存期间禁用重复提交。 */
  async function handleResetDictCache() {
    if (resettingCache.value) return
    resettingCache.value = true
    try {
      const response = await resetDictCache()
      if (response.code === 200) message.success('字典缓存已刷新')
      else message.error(response.message)
    } catch {
      // 请求错误由公共拦截器提示。
    } finally {
      resettingCache.value = false
    }
  }

  onScopeDispose(() => (disposed = true))

  return {
    registerTable,
    form: table.searchModel,
    loading: table.loading,
    error: table.error,
    selectedType,
    themeVars,
    canViewData,
    canResetCache,
    canExport,
    resettingCache,
    exporting,
    openAddDialog,
    removeDictTypeBatch,
    handleExport,
    handleResetDictCache,
  }
}
