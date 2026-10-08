import type { Ref } from 'vue'
import type {
  DataTableColumn,
  DataTableRowKey,
  DataTableColumnGroup,
  DataTableBaseColumn,
  DataTableProps,
  DataTableRowData,
  DataTableInst,
  PaginationProps,
} from 'naive-ui'

/**
 * 按钮类型
 */
export type ButtonType = 'add' | 'edit' | 'delete' | 'export'
/**
 * 按钮权限控制
 */
interface ButtonPermission {
  add?: string[]
  edit?: string[]
  delete?: string[]
  export?: string[]
}
/**
 * naive-ui的data-table属性
 */
type BaseProps = Omit<
  DataTableProps,
  | Extract<keyof DataTableProps, `on${string}`>
  | 'page'
  | 'pageSize'
  | 'checkedRowKeys'
  | 'expandedRowKeys'
  | 'bordered'
  | 'striped'
>

/**
 * 自定义属性
 */
export interface WhimDataTableProps extends /* @vue-ignore */ BaseProps {
  columns: DataTableColumn[]
  data: DataTableRowData[]
  pagination?: PaginationProps | false
  rowKey?: (row: DataTableRowData) => DataTableRowKey
  loading?: boolean
  // 允许显示的按钮
  visibleButtons?: ButtonType[]
  // 按钮权限控制
  buttonPermission?: ButtonPermission
  buttonRole?: ButtonPermission
  // 允许显示的边框和斑马纹功能开关
  visibleBorderSwitch?: boolean
  visibleStripedSwitch?: boolean
}

export interface WhimDataTableEmits {
  add: []
  edit: [rowKey: DataTableRowKey]
  delete: [rowKeys: DataTableRowKey[]]
  export: [rowKeys: DataTableRowKey[]]
  refresh: []
}

/** 表格组件通过 defineModel 声明的分页、选中行和展开行状态。 */
export interface WhimDataTableModels {
  page: Ref<number>
  pageSize: Ref<number>
  checkedRowKeys: Ref<DataTableRowKey[]>
  expandedRowKeys: Ref<DataTableRowKey[] | undefined>
  bordered: Ref<boolean>
  striped: Ref<boolean>
}

/**
 * 合并表格属性并管理行选择、列显示和分页状态。
 */
export function useWhimTable(props: WhimDataTableProps, models: WhimDataTableModels) {
  // 显式属性由 props 管理，其余属性在组件模板中直接透传给表格。
  const tableProps = computed(() => ({
    ...props,
    rowKey: props.rowKey ?? ((row: DataTableRowData) => row.id as DataTableRowKey),
  }))
  // 表格的实例引用
  const tableRef = useTemplateRef<DataTableInst>('tableRef')
  // 表格边框和斑马纹开关
  const bordered = models.bordered
  const striped = models.striped
  // 表格选中行数组
  const checkedRowKeys = models.checkedRowKeys

  const paginationConfig = computed(() =>
    props.pagination
      ? {
          ...props.pagination,
          page: models.page.value,
          pageSize: models.pageSize.value,
        }
      : false,
  )

  /**
   * 计算实际显示的列是否包含key属性（类型保护）
   * @param col - 表格列对象
   * @returns 是否包含key属性
   */
  const isColumnWithKey = (
    col: DataTableColumn,
  ): col is DataTableColumnGroup | DataTableBaseColumn => {
    return 'key' in col && typeof col.key === 'string'
  }

  /**
   * 可选择的列集合（排除 selection 和 action 列）
   */
  const selectTableColumns = computed(() =>
    tableProps.value.columns.filter(
      (col: DataTableColumn): col is DataTableColumnGroup | DataTableBaseColumn => {
        if (col.type === 'selection') return false
        if (!isColumnWithKey(col)) return false
        return col.key !== 'action'
      },
    ),
  )

  // 默认选中所有可选择的列 key，用于控制列的显示隐藏
  const checkedColumnKeys = ref(selectTableColumns.value.map((col) => col.key))

  /**
   * 根据选中的列 key 计算实际显示的列集合
   */
  const tableColumns = computed(() => {
    return tableProps.value.columns.filter((col) => {
      if (!isColumnWithKey(col)) return true // 非普通列，直接显示
      // selection 类型的列直接显示
      if (col.type === 'selection') return true
      // action 列默认显示
      if (col.key === 'action') return true
      // 其他列根据 checkedColumnKeys 决定
      return checkedColumnKeys.value.includes(col.key)
    })
  })

  /**
   * 表格行选中事件处理函数
   * @param rowKeys
   */
  const handleCheck = (rowKeys: DataTableRowKey[]) => {
    checkedRowKeys.value = rowKeys
  }

  /**
   * 过滤无效的 key
   * @param data
   * @param keys
   */
  const filterValidKeys = (data: DataTableRowData[], keys: DataTableRowKey[]) => {
    const validKeys = data.map(tableProps.value.rowKey)
    return keys.filter((key) => validKeys.includes(key))
  }
  /**
   * 页码改变事件处理函数
   * @param currentPage
   */
  const handlePageChange = (currentPage: number) => {
    models.page.value = currentPage
  }

  /**
   * 页码大小改变事件处理函数
   * @param currentPageSize
   */
  const handlePageSizeChange = (currentPageSize: number) => {
    models.pageSize.value = currentPageSize
  }
  /**
   * 监听 data 属性变化，同步选中行
   * 假如我选中了两行，然后删除了一行之后，选中行Key数组中还存在已删除的行key，那么此时应该更新选中行
   */
  watch(
    () => tableProps.value.data,
    (newData) => {
      if (newData) {
        const newKeys = filterValidKeys(newData, checkedRowKeys.value)
        if (newKeys.length !== checkedRowKeys.value.length) {
          checkedRowKeys.value = newKeys
        }
      }
    },
    { immediate: true },
  )

  return {
    tableProps,
    paginationConfig,
    tableRef,
    bordered,
    striped,
    checkedRowKeys,
    handleCheck,
    tableColumns,
    selectTableColumns,
    checkedColumnKeys,
    handlePageChange,
    handlePageSizeChange,
  }
}
