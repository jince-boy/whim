import type { TreeSelectOption } from 'naive-ui'
import type { TablePageQuery } from '@/components/table'

export type DeptId = string | number
export type OrganizationStatus = 0 | 1

/** 部门详情和树节点，ID 保留后端原始精度。 */
export interface DeptItem {
  id: DeptId
  parentId: DeptId
  ancestors: string
  deptName: string
  deptCode: string
  leaderUserId: DeptId | null
  leaderName: string | null
  leaderUsername: string | null
  phone: string | null
  email: string | null
  sort: number
  status: OrganizationStatus
  remark: string | null
  createTime: string | null
  updateTime: string | null
  children: DeptItem[]
}

export interface DeptQuery {
  deptName: string
  status: OrganizationStatus | null
}

/** 修改时携带目标ID；新增时省略ID，不携带路径或审计信息。 */
export interface DeptSave {
  id?: DeptId
  parentId: string | null
  deptName: string
  deptCode: string
  leaderUserId: string | null
  phone: string | null
  email: string | null
  sort: number | null
  status: OrganizationStatus
  remark: string | null
}

export interface DeptFormProps {
  initialModel?: DeptItem
  parentDept?: DeptItem
}

/** 部门ID、分页和搜索条件统一传递。 */
export interface DeptLeaderQuery extends TablePageQuery {
  deptId: DeptId
  keyword?: string
}

export interface DeptLeaderOption {
  id: DeptId
  username: string
  name: string
}

export interface DepartmentSelection {
  options: TreeSelectOption[]
  names: ReadonlyMap<string, string>
}
