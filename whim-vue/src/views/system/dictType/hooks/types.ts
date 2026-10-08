export interface DictType {
  id: string
  name: string
  type: string
  remark: string
}

export interface DictTypeResult {
  id: string
  name: string
  type: string
  status: number
  remark: string
  createTime: string
}

export interface DictTypePageResult {
  currentPage: number
  data: DictTypeResult[]
  pages: number
  size: number
  total: number
}
/** 字典类型编辑表单的只读初始数据。 */
export interface DictTypeFormProps {
  initialModel?: DictType
}
