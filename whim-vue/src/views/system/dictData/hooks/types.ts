export type TagType = 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'

/** 数据面板由父工作区明确指定归属，不自行读取路由。 */
export interface DictDataProps {
  type: string
  name: string
}

/** 字典数据查询字段。 */
export interface DictDataQuery {
  label: string
}

export interface DictData {
  id: string
  type: string
  label: string
  value: string
  listClass: TagType
  sort: number
  remark: string
}

export interface DictDataResult {
  id: string
  type: string
  label: string
  value: string
  listClass: TagType
  sort: number
  remark: string
  createTime: string
}

export interface DictDataPageResult {
  currentPage: number
  data: DictDataResult[]
  pages: number
  size: number
  total: number
}
/** 字典数据编辑表单的只读初始数据。 */
export interface DictDataFormProps {
  initialModel?: DictData
}
