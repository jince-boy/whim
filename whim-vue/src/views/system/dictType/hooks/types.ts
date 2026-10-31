export interface DictType {
  id: string
  name: string
  type: string
  remark: string
}

/** 字典类型查询字段。 */
export interface DictTypeQuery {
  name: string
  type: string
}

export interface DictTypeResult extends DictType {
  createTime: string
}

/** 字典类型编辑表单的只读初始数据。 */
export interface DictTypeFormProps {
  initialModel?: DictType
}
