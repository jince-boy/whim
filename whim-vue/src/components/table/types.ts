import type { ComputedRef, MaybeRefOrGetter, Ref } from 'vue'
import type {
  DataTableColumns,
  DataTableInst,
  DataTableProps,
  DataTableRowKey,
  PaginationProps,
} from 'naive-ui'
import type { ApiResponse } from '@/api'
import type { SearchFormOptions, SearchFormRegister } from '../form/types'

export type TableButton = 'add' | 'edit' | 'delete' | 'export'

export interface TableToolbarOptions {
  buttons?: TableButton[]
  permissions?: Partial<Record<TableButton, string[]>>
  roles?: Partial<Record<TableButton, string[]>>
  showStriped?: boolean
  showColumns?: boolean
  showRefresh?: boolean
  showFullscreen?: boolean
}

/** 后端通用分页结构。 */
export interface TablePage<Row> {
  data: Row[]
  total: number
  currentPage: number
  size: number
  pages: number
}

export interface TablePageQuery {
  pageNum: number
  pageSize: number
}

/** 普通 TS 配置继承原生属性和事件，不经过 defineProps 的类型展开。 */
export type WhimTableOptions<Row extends object, Query extends object> = Omit<
  DataTableProps,
  | 'columns'
  | 'data'
  | 'loading'
  | 'pagination'
  | 'rowKey'
  | 'remote'
  | 'checkedRowKeys'
  | 'expandedRowKeys'
  | 'onPageChange'
  | 'onPageSizeChange'
  | 'onSorterChange'
  | 'onFiltersChange'
  | 'onCheckedRowKeysChange'
> & {
  title?: string
  columns: MaybeRefOrGetter<DataTableColumns<Row>>
  rowKey: (row: Row) => DataTableRowKey
  request?: (query: Query & TablePageQuery) => Promise<ApiResponse<TablePage<Row> | Row[]>>
  data?: MaybeRefOrGetter<Row[]>
  /** 纯展示转换，保留原始数据供详情、编辑等业务使用。 */
  transformData?: (rows: Row[], query: Readonly<Query>) => Row[]
  immediate?: boolean
  search?: SearchFormOptions<Query>
  pagination?: false | Omit<PaginationProps, 'page' | 'pageCount' | 'itemCount'>
  toolbar?: TableToolbarOptions
  /** 默认只保留当前数据中的选择；跨页选择需显式开启。 */
  preserveSelection?: boolean
}

/** 组件消费的视图接口，业务泛型留在 hook 的公开 API 中。 */
export interface WhimTableBinding {
  title: ComputedRef<string>
  tableProps: ComputedRef<DataTableProps>
  toolbar: ComputedRef<TableToolbarOptions>
  columnOptions: ComputedRef<Array<{ key: DataTableRowKey; label: string }>>
  visibleColumnKeys: ComputedRef<DataTableRowKey[]>
  checkedRowKeys: Ref<DataTableRowKey[]>
  striped: Ref<boolean>
  loading: Ref<boolean>
  error: Ref<string | undefined>
  registerSearch?: SearchFormRegister
  reload: () => Promise<boolean>
  search: () => Promise<boolean>
  resetSearch: () => Promise<boolean>
  setVisibleColumns: (keys: DataTableRowKey[]) => void
}

export interface WhimTableRegistration {
  connect: (binding: WhimTableBinding) => void
  getInstance: () => DataTableInst | undefined
}

export type WhimTableRegister = (instance: WhimTableRegistration | undefined) => void

export interface WhimTableEmits {
  register: [instance: WhimTableRegistration | undefined]
  add: []
  edit: [key: DataTableRowKey]
  delete: [keys: DataTableRowKey[]]
  export: [keys: DataTableRowKey[]]
  refresh: []
}
