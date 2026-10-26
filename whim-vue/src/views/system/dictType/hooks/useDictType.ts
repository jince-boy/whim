import {
  computed,
  h,
  onActivated,
  onDeactivated,
  onMounted,
  onScopeDispose,
  ref,
  shallowRef,
  watch,
} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NSpace,
  useThemeVars,
  type DataTableColumns,
  type DataTableRowKey,
} from 'naive-ui'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
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
import type { DictTypeResult } from './types'
import DictTypeForm from '../form.vue'

/** 以类型目录协调数据工作区和类型管理抽屉，所有数据来自真实接口。 */
export function useDictType() {
  const userStore = useUserStore()
  const route = useRoute()
  const router = useRouter()
  const themeVars = useThemeVars()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const types = shallowRef<DictTypeResult[]>([])
  const selectedId = ref<string>()
  const keyword = ref('')
  const loading = ref(false)
  const error = ref('')
  const managingTypes = ref(false)
  const checkedTypeIds = ref<DataTableRowKey[]>([])
  const resettingCache = ref(false)
  const exporting = ref(false)
  const editingId = ref<string>()
  let requestVersion = 0
  let disposed = false
  let active = true
  let editVersion = 0

  /** 类型和数据分别按对应的业务权限显示操作。 */
  const can = (permission: string) =>
    userStore.permissionCode.includes('*') || userStore.permissionCode.includes(permission)
  const canViewTypes = computed(() => can('system:dictType:page'))
  const canAdd = computed(() => can('system:dictType:add'))
  const canEdit = computed(() => can('system:dictType:update') && can('system:dictType:detail'))
  const canDelete = computed(() => can('system:dictType:delete'))
  const canViewData = computed(() => can('system:dictData:page'))
  const canResetCache = computed(() => can('system:dictType:reset'))
  const canExport = computed(() => can('system:dictType:export'))
  const selectedType = computed(() => types.value.find((item) => item.id === selectedId.value))
  const filteredTypes = computed(() => {
    const text = keyword.value.trim().toLocaleLowerCase()
    return types.value.filter((item) =>
      `${item.name}\n${item.type}`.toLocaleLowerCase().includes(text),
    )
  })
  const typeOptions = computed(() =>
    types.value.map((item) => ({ label: `${item.name} · ${item.type}`, value: item.id })),
  )
  /** 刷新保留当前类型；新增或改名后优先定位刚保存的类型。 */
  async function reloadTypes(preferredType?: string) {
    if (!canViewTypes.value || disposed || !active) return
    const version = ++requestVersion
    loading.value = true
    error.value = ''
    try {
      const response = await fetchDictTypeList()
      if (disposed || version !== requestVersion) return
      if (response.code !== 200) throw new Error(response.message || '字典类型加载失败')
      types.value = response.data
      const requestedType = preferredType ?? route.query.type
      selectedId.value = (
        types.value.find((item) => item.type === requestedType) ??
        types.value.find((item) => item.id === selectedId.value) ??
        types.value[0]
      )?.id
      checkedTypeIds.value = checkedTypeIds.value.filter((id) =>
        types.value.some((item) => item.id === id),
      )
      if (preferredType) keyword.value = ''
      syncSelectedType()
    } catch (cause) {
      if (!disposed && version === requestVersion)
        error.value = cause instanceof Error ? cause.message : '字典类型加载失败，请重试'
    } finally {
      if (!disposed && version === requestVersion) loading.value = false
    }
  }

  /** 将当前类型写入页面地址，刷新和旧链接都能定位到同一工作区。 */
  function syncSelectedType() {
    if (!active || disposed) return
    const type = selectedType.value?.type
    if (route.query.type === type) return
    void router.replace({ query: { ...route.query, type } })
  }

  /** 搜索只缩小目录范围，主动选择时才切换右侧的数据归属。 */
  function selectType(id: string) {
    selectedId.value = id
    syncSelectedType()
  }

  /** 新增后清除目录搜索并定位新类型，便于继续添加数据。 */
  function openAddDialog() {
    let savedType = ''
    openFormDialog({
      title: '新增字典类型',
      width: 520,
      formComponent: DictTypeForm,
      onSubmit: (model) => {
        savedType = model.type
        return insertDictType(model)
      },
      successMessage: '新增字典类型成功',
      onSuccess: () => reloadTypes(savedType),
    })
  }

  /** 读取最新详情，保存后同步类型名称、编码与数据面板。 */
  async function openEditDialog(id: string) {
    if (editingId.value !== undefined) return
    const version = ++editVersion
    editingId.value = id
    try {
      const response = await fetchDictTypeDetail({ id })
      if (disposed || !active || version !== editVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      let savedType = response.data.type
      openFormDialog({
        title: '编辑字典类型',
        width: 520,
        formComponent: DictTypeForm,
        formProps: { initialModel: response.data },
        onSubmit: (model) => {
          savedType = model.type
          return updateDictType(model)
        },
        successMessage: '字典类型已更新',
        onSuccess: () => reloadTypes(savedType),
      })
    } catch {
      // 请求错误由公共拦截器提示。
    } finally {
      if (version === editVersion) editingId.value = undefined
    }
  }

  /** 删除由后端校验是否存在数据；失败保留选择，成功重新确认归属。 */
  function removeDictTypes(ids: DataTableRowKey[]) {
    if (!ids.length) return
    const name = ids.length === 1 ? types.value.find((item) => item.id === ids[0])?.name : undefined
    openDeleteDialog({
      content: `确定删除${name ? `字典类型“${name}”` : `选中的 ${ids.length} 个字典类型`}？类型下存在数据时无法删除，删除后无法恢复。`,
      onSubmit: () => deleteDictType({ dictTypeIds: ids.join(',') }),
      onSuccess: async () => {
        checkedTypeIds.value = []
        // 先移除已删除项，即使刷新失败也不能继续编辑已不存在的类型。
        types.value = types.value.filter((item) => !ids.includes(item.id))
        if (selectedId.value && ids.includes(selectedId.value)) selectedId.value = undefined
        await reloadTypes()
      },
    })
  }

  /** 导出全部类型，目录的本地关键字不影响导出范围。 */
  async function handleExport() {
    if (exporting.value) return
    exporting.value = true
    try {
      await exportDictType({ name: '', type: '' })
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

  const managementColumns = computed<DataTableColumns<DictTypeResult>>(() => [
    ...(canDelete.value ? [{ type: 'selection' as const, width: 40 }] : []),
    { title: '字典名称', key: 'name', minWidth: 130, ellipsis: { tooltip: true } },
    { title: '类型编码', key: 'type', minWidth: 150, ellipsis: { tooltip: true } },
    {
      title: '备注',
      key: 'remark',
      minWidth: 140,
      ellipsis: { tooltip: true },
      render: (row) => row.remark || '—',
    },
    {
      title: '操作',
      key: 'action',
      width: 182,
      fixed: 'right',
      render: (row) =>
        h(NSpace, { size: 12 }, () => [
          h(
            NButton,
            {
              text: true,
              type: 'info',
              onClick: () => {
                selectType(row.id)
                managingTypes.value = false
              },
            },
            () => '查看数据',
          ),
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
    },
  ])

  watch(
    () => route.query.type,
    (type) => {
      if (!active) return
      const item = types.value.find((entry) => entry.type === type)
      if (item) selectedId.value = item.id
    },
  )
  watch(filteredTypes, (rows) => {
    checkedTypeIds.value = checkedTypeIds.value.filter((id) => rows.some((item) => item.id === id))
  })
  onMounted(() => void reloadTypes())
  onDeactivated(() => {
    active = false
    requestVersion++
    editVersion++
    loading.value = false
    editingId.value = undefined
    managingTypes.value = false
  })
  onActivated(() => {
    if (active) return
    active = true
    void reloadTypes()
  })
  onScopeDispose(() => {
    disposed = true
    requestVersion++
  })

  return {
    types,
    filteredTypes,
    typeOptions,
    keyword,
    selectedId,
    selectedType,
    loading,
    error,
    managingTypes,
    checkedTypeIds,
    managementColumns,
    themeVars,
    canViewTypes,
    canAdd,
    canDelete,
    canViewData,
    canResetCache,
    canExport,
    resettingCache,
    exporting,
    selectType,
    reloadTypes,
    openAddDialog,
    removeDictTypes,
    handleExport,
    handleResetDictCache,
  }
}
