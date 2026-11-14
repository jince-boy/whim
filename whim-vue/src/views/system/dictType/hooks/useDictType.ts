import {
  computed,
  h,
  onActivated,
  onDeactivated,
  onScopeDispose,
  ref,
  shallowRef,
  watch,
} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { isAxiosError } from 'axios'
import {
  NButton,
  NSpace,
  useThemeVars,
  type DataTableColumns,
  type DataTableRowKey,
} from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import {
  deleteDictType,
  exportDictType,
  fetchDictTypeDetail,
  fetchDictTypeList,
  insertDictType,
  resetDictCache,
  updateDictType,
} from './api'
import type { DictTypeQuery, DictTypeResult } from './types'
import DictTypeForm from '../form.vue'

/** 类型在主列表维护，选中类型的数据通过原生抽屉独立管理。 */
export function useDictType() {
  const dictionary = useDictStore()
  const userStore = useUserStore()
  const route = useRoute()
  const router = useRouter()
  const themeVars = useThemeVars()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const selectedType = shallowRef<DictTypeResult>()
  const showData = ref(false)
  const resettingCache = ref(false)
  const exporting = ref(false)
  const editingId = ref<string>()
  let active = true
  let editVersion = 0

  /** 类型和数据分别按对应的业务权限显示操作。 */
  const can = (permission: string) => userStore.hasPermission(permission)
  const canViewTypes = computed(() => can('system:dictType:page'))
  const canEdit = computed(() => can('system:dictType:update') && can('system:dictType:detail'))
  const canDelete = computed(() => can('system:dictType:delete'))
  const canViewData = computed(() => can('system:dictData:page'))
  const canResetCache = computed(() => can('system:dictType:reset'))
  const canExport = computed(() => can('system:dictType:export'))

  const columns = computed(() => {
    const columns: DataTableColumns<DictTypeResult> = [
      ...(canDelete.value ? [{ type: 'selection' as const, width: 40 }] : []),
      { title: '字典名称', key: 'name', ellipsis: { tooltip: true } },
      { title: '字典编码', key: 'type', ellipsis: { tooltip: true } },
      {
        title: '备注',
        key: 'remark',
        ellipsis: { tooltip: true },
        render: (row) => row.remark || '—',
      },
      { title: '创建时间', key: 'createTime', ellipsis: { tooltip: true } },
    ]
    if (canViewData.value || canEdit.value || canDelete.value) {
      columns.push({
        title: '操作',
        key: 'action',
        fixed: 'right',
        render: (row) =>
          h(NSpace, { size: 12, wrap: false }, () => [
            canViewData.value
              ? h(NButton, { text: true, type: 'info', onClick: () => openData(row) }, () => '查看')
              : null,
            canEdit.value
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'warning',
                    loading: editingId.value === row.id,
                    disabled: editingId.value !== undefined,
                    onClick: () => openEditDialog(row.id),
                  },
                  () => '修改',
                )
              : null,
            canDelete.value
              ? h(
                  NButton,
                  { text: true, type: 'error', onClick: () => removeDictTypes([row.id]) },
                  () => '删除',
                )
              : null,
          ]),
      })
    }
    return columns
  })

  const [registerTable, table] = useWhimTable<DictTypeResult, DictTypeQuery>(() => ({
    title: '字典管理',
    columns,
    rowKey: (row) => row.id,
    scrollX: 840,
    immediate: canViewTypes.value,
    request: fetchDictTypeList,
    transformData: (rows, query) => {
      const name = query.name.trim().toLocaleLowerCase()
      const type = query.type.trim().toLocaleLowerCase()
      return rows.filter(
        (row) =>
          row.name.toLocaleLowerCase().includes(name) &&
          row.type.toLocaleLowerCase().includes(type),
      )
    },
    search: {
      defaultValues: { name: '', type: '' },
      fields: [
        { key: 'name', label: '字典名称' },
        { key: 'type', label: '字典编码' },
      ],
    },
    toolbar: {
      buttons: ['add', 'delete'],
      permissions: {
        add: ['system:dictType:add'],
        delete: ['system:dictType:delete'],
      },
    },
  }))

  /** 打开指定类型的数据，不向数据抽屉提供类型修改或删除操作。 */
  function openData(row: DictTypeResult) {
    if (!canViewData.value) return
    selectedType.value = row
    showData.value = true
    if (route.query.type !== row.type)
      void router.replace({ query: { ...route.query, type: row.type } })
  }

  /** 类型维护成功后刷新主列表和全局字典，移除旧编码的缓存。 */
  async function refreshDictionaryTypes() {
    await Promise.all([table.reload(), dictionary.refreshDictionaries()])
  }

  /** 新增类型后刷新主列表，数据维护由查看数据入口打开。 */
  function openAddDialog() {
    openFormDialog({
      title: '新增字典',
      width: 520,
      formComponent: DictTypeForm,
      onSubmit: insertDictType,
      successMessage: '新增字典成功',
      onSuccess: refreshDictionaryTypes,
    })
  }

  /** 读取最新详情，修改类型只发生在主列表的编辑表单中。 */
  async function openEditDialog(id: string) {
    if (editingId.value !== undefined) return
    const version = ++editVersion
    editingId.value = id
    try {
      const response = await fetchDictTypeDetail({ id })
      if (version !== editVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: '修改字典',
        width: 520,
        formComponent: DictTypeForm,
        formProps: { initialModel: response.data },
        onSubmit: updateDictType,
        successMessage: '字典已更新',
        onSuccess: refreshDictionaryTypes,
      })
    } catch {
      // 请求错误由公共拦截器提示。
    } finally {
      if (version === editVersion) editingId.value = undefined
    }
  }

  /** 单条和批量删除共用后端校验与确认流程。 */
  function removeDictTypes(ids: DataTableRowKey[]) {
    if (!ids.length) return
    const name =
      ids.length === 1 ? table.data.value.find((item) => item.id === ids[0])?.name : undefined
    openDeleteDialog({
      content: `确定删除${name ? `字典“${name}”` : `选中的 ${ids.length} 个字典`}？字典下存在数据时无法删除，删除后无法恢复。`,
      onSubmit: () => deleteDictType({ dictTypeIds: ids.join(',') }),
      onSuccess: async () => {
        table.setCheckedRowKeys([])
        await refreshDictionaryTypes()
      },
    })
  }

  /** 导出主列表当前输入的名称和编码条件。 */
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
      if (response.code === 200) {
        await dictionary.refreshDictionaries()
        message.success('字典缓存已刷新')
      } else message.error(response.message)
    } catch (error) {
      if (!isAxiosError(error))
        message.error(error instanceof Error ? error.message : '字典缓存刷新失败，请重试')
    } finally {
      resettingCache.value = false
    }
  }

  watch([() => route.query.type, table.data], ([type, rows]) => {
    if (!active || !canViewData.value) return
    const row = rows.find((item) => item.type === type)
    if (row) {
      selectedType.value = row
    }
    showData.value = !!row
  })
  watch(showData, (show) => {
    if (show || !active || route.query.type === undefined) return
    const query = { ...route.query }
    delete query.type
    void router.replace({ query })
  })
  onDeactivated(() => {
    active = false
    editVersion++
    editingId.value = undefined
    showData.value = false
  })
  onActivated(() => {
    if (active) return
    active = true
    void table.reload()
  })
  onScopeDispose(() => editVersion++)

  return {
    registerTable,
    form: table.searchModel,
    selectedType,
    showData,
    themeVars,
    canViewTypes,
    canResetCache,
    canExport,
    resettingCache,
    exporting,
    openAddDialog,
    removeDictTypes,
    handleExport,
    handleResetDictCache,
  }
}
