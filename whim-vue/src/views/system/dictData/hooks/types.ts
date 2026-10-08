export type TagType = 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'

export interface DictData {
  id: string
  dictType: string
  label: string
  value: string
  listClass: TagType
  sort: number
  remark: string
}

export interface DictDataResult {
  id: string
  dictType: string
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
