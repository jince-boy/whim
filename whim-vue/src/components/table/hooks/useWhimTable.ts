import { computed, onScopeDispose, ref, shallowRef, toValue, watch } from 'vue'
import type { MaybeRefOrGetter } from 'vue'
import type { DataTableProps, DataTableRowKey } from 'naive-ui'
import { useSearchForm } from '../../form/hooks/useSearchForm'
import { useThemeStore } from '@/stores/modules/theme'
import type {
  WhimTableBinding,
  WhimTableOptions,
  WhimTableRegister,
  WhimTableRegistration,
} from '../types'

type NativeHandler<Value> = Extract<NonNullable<Value>, (...args: never[]) => unknown>

/** 原生事件允许函数或函数数组，每个监听器只调用一次。 */
function notify<Args extends unknown[]>(
  handlers: ((...args: Args) => void) | Array<(...args: Args) => void> | undefined,
  ...args: Args
) {
  if (Array.isArray(handlers)) handlers.forEach((handler) => handler(...args))
  else handlers?.(...args)
}

/** 创建完整列表状态；注册仅连接视图，数据和配置始终由本次 hook 调用持有。 */
export function useWhimTable<Row extends object, Query extends object = Record<string, never>>(
  source: MaybeRefOrGetter<WhimTableOptions<Row, Query>>,
) {
  const options = computed(() => toValue(source))
  const themeStore = useThemeStore()
  const instance = shallowRef<WhimTableRegistration>()
  const [registerSearch, form] = useSearchForm<Query>(
    () => options.value.search ?? { defaultValues: {} as Query },
  )
  const submittedQuery = form.submittedValues
  const rows = shallowRef<Row[]>([])
  const data = computed(() =>
    options.value.request ? rows.value : (toValue(options.value.data) ?? []),
  )
  const displayedData = computed(
    () => options.value.transformData?.(data.value, submittedQuery.value) ?? data.value,
  )
  const remote = ref(!!options.value.request)
  const loading = ref(false)
  const error = ref<string>()
  const page = ref(1)
  const pageSize = ref(options.value.pagination ? (options.value.pagination.pageSize ?? 10) : 10)
  const total = ref(0)
  const checkedRowKeys = ref<DataTableRowKey[]>([...(options.value.defaultCheckedRowKeys ?? [])])
  const expandedRowKeys = ref<DataTableRowKey[] | undefined>(
    options.value.defaultExpandedRowKeys ? [...options.value.defaultExpandedRowKeys] : undefined,
  )
  const bordered = computed(() => options.value.bordered ?? themeStore.config.tableBordered)
  const striped = ref(options.value.striped ?? false)
  const hiddenColumns = ref<DataTableRowKey[]>([])
  let requestVersion = 0
  let started = false
  let disposed = false

  const columns = computed(() => toValue(options.value.columns))
  const columnOptions = computed(() =>
    columns.value.flatMap((column) =>
      'key' in column && column.key !== undefined && column.key !== 'action'
        ? [
            {
              key: column.key,
              label: typeof column.title === 'string' ? column.title : String(column.key),
            },
          ]
        : [],
    ),
  )
  const visibleColumnKeys = computed(() =>
    columnOptions.value
      .map((column) => column.key)
      .filter((key) => !hiddenColumns.value.includes(key)),
  )
  const visibleColumns = computed(() =>
    columns.value.filter(
      (column) =>
        !('key' in column) || column.key === 'action' || !hiddenColumns.value.includes(column.key),
    ),
  )

  /** 只记录主动隐藏的列，因此异步新增的列默认可见。 */
  function setVisibleColumns(keys: DataTableRowKey[]) {
    hiddenColumns.value = columnOptions.value
      .map((column) => column.key)
      .filter((key) => !keys.includes(key))
  }

  /** 使用已提交条件刷新；最新请求才能写回，删除末页后最多补查一次。 */
  async function reload(): Promise<boolean> {
    if (disposed) return false
    const request = options.value.request
    if (!request) return true
    started = true
    const version = ++requestVersion
    loading.value = true
    error.value = undefined
    try {
      for (let attempt = 0; attempt < 2; attempt++) {
        const response = await request({
          ...structuredClone(submittedQuery.value),
          pageNum: page.value,
          pageSize: pageSize.value,
        })
        if (version !== requestVersion || disposed) return false
        if (response.code !== 200) throw new Error(response.message || '表格数据加载失败')
        if (Array.isArray(response.data)) {
          remote.value = false
          rows.value = response.data
          total.value = response.data.length
          return true
        }
        const result = response.data
        remote.value = true
        total.value = result.total
        const lastPage = Math.max(1, Math.ceil(result.total / pageSize.value))
        if (page.value > lastPage && attempt === 0) {
          page.value = lastPage
          continue
        }
        rows.value = result.data
        page.value = Math.max(1, result.currentPage)
        pageSize.value = result.size
        return true
      }
      return false
    } catch (reason) {
      if (version === requestVersion && !disposed)
        error.value = reason instanceof Error ? reason.message : '表格数据加载失败'
      return false
    } finally {
      if (version === requestVersion && !disposed) loading.value = false
    }
  }

  /** 提交当前输入，搜索条件变化后回到第一页并清空选择。 */
  async function search() {
    form.commitValues()
    return applySearch()
  }

  /** 使用表单已提交的快照查询第一页，保留其他尚未提交的输入。 */
  function applySearch() {
    page.value = 1
    checkedRowKeys.value = []
    return reload()
  }

  /** 恢复表单默认值并查询第一页。 */
  function resetSearch() {
    form.resetFields()
    return search()
  }

  /** 更新页码；分页配置与表格级原生监听各自只通知一次。 */
  const changePage: NativeHandler<DataTableProps['onUpdatePage']> = (value) => {
    if (page.value === value) return
    page.value = value
    notify(options.value.onUpdatePage, value)
    notify(options.value['onUpdate:page'], value)
    if (options.value.pagination) {
      notify(options.value.pagination.onUpdatePage, value)
      notify(options.value.pagination['onUpdate:page'], value)
    }
    void reload()
  }

  /** 每页条数变化时回到第一页，只发起一次请求。 */
  const changePageSize: NativeHandler<DataTableProps['onUpdatePageSize']> = (value) => {
    if (pageSize.value === value) return
    pageSize.value = value
    page.value = 1
    notify(options.value.onUpdatePageSize, value)
    notify(options.value['onUpdate:pageSize'], value)
    if (options.value.pagination) {
      notify(options.value.pagination.onUpdatePageSize, value)
      notify(options.value.pagination['onUpdate:pageSize'], value)
    }
    void reload()
  }

  /** 保存选择并保留原生事件中的行数据和操作信息。 */
  const changeChecked: NativeHandler<DataTableProps['onUpdateCheckedRowKeys']> = (...args) => {
    checkedRowKeys.value = [...args[0]]
    notify(options.value.onUpdateCheckedRowKeys, ...args)
    notify(options.value['onUpdate:checkedRowKeys'], ...args)
  }

  /** 保存展开状态并保留原生事件参数。 */
  const changeExpanded: NativeHandler<DataTableProps['onUpdateExpandedRowKeys']> = (...args) => {
    expandedRowKeys.value = [...args[0]]
    notify(options.value.onUpdateExpandedRowKeys, ...args)
    notify(options.value['onUpdate:expandedRowKeys'], ...args)
  }

  const pagination = computed(() => {
    if (options.value.pagination === false) return false
    const config = { ...options.value.pagination }
    delete config.onUpdatePage
    delete config['onUpdate:page']
    delete config.onUpdatePageSize
    delete config['onUpdate:pageSize']
    return {
      showSizePicker: true,
      pageSizes: [10, 20, 50],
      ...config,
      page: page.value,
      pageSize: pageSize.value,
      ...(options.value.request ? { itemCount: total.value } : {}),
    }
  })

  const tableProps = computed<DataTableProps>(() => {
    const nativeProps: Record<string, unknown> = { ...options.value }
    for (const key of [
      'title',
      'request',
      'immediate',
      'search',
      'toolbar',
      'preserveSelection',
      'transformData',
      'onUpdatePage',
      'onUpdate:page',
      'onUpdatePageSize',
      'onUpdate:pageSize',
      'onUpdateCheckedRowKeys',
      'onUpdate:checkedRowKeys',
      'onUpdateExpandedRowKeys',
      'onUpdate:expandedRowKeys',
    ])
      delete nativeProps[key]
    return {
      size: 'small',
      ...nativeProps,
      // 泛型行配置在公开入口检查，此处只擦除视图层不需要的业务类型。
      columns: visibleColumns.value as DataTableProps['columns'],
      rowKey: options.value.rowKey as DataTableProps['rowKey'],
      data: displayedData.value,
      remote: remote.value,
      loading: loading.value,
      pagination: pagination.value,
      bordered: bordered.value,
      singleLine: options.value.singleLine ?? !bordered.value,
      striped: striped.value,
      checkedRowKeys: checkedRowKeys.value,
      expandedRowKeys: expandedRowKeys.value,
      onUpdatePage: changePage,
      onUpdatePageSize: changePageSize,
      onUpdateCheckedRowKeys: changeChecked,
      onUpdateExpandedRowKeys: changeExpanded,
    }
  })

  watch(
    () => options.value.striped,
    (value) => {
      striped.value = value ?? false
    },
  )
  watch(displayedData, (values) => {
    if (options.value.preserveSelection) return
    const validKeys = new Set<DataTableRowKey>()
    const childrenKey = options.value.childrenKey ?? 'children'
    /** 树表格同时保留子节点的选中状态。 */
    function collect(items: Row[]) {
      for (const item of items) {
        validKeys.add(options.value.rowKey(item))
        const children = (item as Record<string, unknown>)[childrenKey]
        if (Array.isArray(children)) collect(children as Row[])
      }
    }
    collect(values)
    checkedRowKeys.value = checkedRowKeys.value.filter((key) => validKeys.has(key))
  })

  const binding: WhimTableBinding = {
    title: computed(() => options.value.title ?? '数据列表'),
    tableProps,
    toolbar: computed(() => options.value.toolbar ?? {}),
    columnOptions,
    visibleColumnKeys,
    checkedRowKeys,
    striped,
    loading,
    error,
    registerSearch: options.value.search ? registerSearch : undefined,
    reload,
    applySearch,
    setVisibleColumns,
  }

  /** 注册不重复初始化请求，卸载使尚未结束的请求失效。 */
  const register: WhimTableRegister = (target) => {
    if (target && instance.value && instance.value !== target)
      throw new Error('一个 useWhimTable 只能注册一个表格')
    if (instance.value === target) return
    instance.value = target
    if (!target) {
      requestVersion++
      loading.value = false
      started = false
      return
    }
    target.connect(binding)
    if (options.value.immediate !== false && !started) void reload()
  }

  /** 获取原生实例，仅允许在注册和挂载完成后调用。 */
  function getTableInstance() {
    const table = instance.value?.getInstance()
    if (!table) throw new Error('表格尚未注册，请在组件挂载后调用')
    return table
  }

  /** 返回选择快照，调用方不能修改内部数组。 */
  const getCheckedRowKeys = () => [...checkedRowKeys.value]
  /** 返回展开主键快照。 */
  const getExpandedRowKeys = () => [...(expandedRowKeys.value ?? [])]
  /** 通过命令更新选择。 */
  const setCheckedRowKeys = (keys: DataTableRowKey[]) => {
    checkedRowKeys.value = [...keys]
  }
  /** 通过命令更新树节点展开状态。 */
  const setExpandedRowKeys = (keys: DataTableRowKey[]) => {
    expandedRowKeys.value = [...keys]
  }

  onScopeDispose(() => {
    disposed = true
    requestVersion++
    instance.value = undefined
  })
  return [
    register,
    {
      reload,
      search,
      resetSearch,
      searchModel: form.model,
      setSearchValues: form.setValues,
      getSearchValues: form.getValues,
      getCheckedRowKeys,
      getExpandedRowKeys,
      setCheckedRowKeys,
      setExpandedRowKeys,
      getTableInstance,
      data: computed(() => data.value),
      loading: computed(() => loading.value),
      error: computed(() => error.value),
    },
  ] as const
}
