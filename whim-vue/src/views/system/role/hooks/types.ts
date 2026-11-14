import type { TablePageQuery } from '@/components/table'
import type { TreeOption } from 'naive-ui'

export type RoleId = string | number
export type RoleStatus = 0 | 1

export interface RoleItem {
  id: RoleId
  roleName: string
  roleCode: string
  dataScope: number
  sort: number
  status: RoleStatus
  remark: string | null
  createTime: string | null
  updateTime: string | null
}

export interface RoleQuery extends TablePageQuery {
  roleName: string
  roleCode: string
  status: RoleStatus | null
}

export interface RoleSave {
  id?: RoleId
  roleName: string
  roleCode: string
  sort: number | null
  status: RoleStatus
  remark: string | null
}

export interface RoleFormProps {
  initialModel?: RoleItem
}

export interface RoleDataScope {
  id: RoleId
  dataScope: number
  deptIds: RoleId[]
}

export interface DataScopeFormProps {
  initialModel: RoleDataScope
  readonly: boolean
}

export interface PermissionNode {
  id: RoleId
  label: string
  menuType: number
  status: RoleStatus
  children: PermissionNode[]
}

export interface RolePermissions {
  menus: PermissionNode[]
  checkedKeys: RoleId[]
}

export interface RolePermissionSave {
  id: RoleId
  permissionIds: RoleId[]
}

export interface PermissionFormProps {
  role: RoleItem
  initialModel: RolePermissions
  readonly: boolean
}

export interface PermissionTreeOption extends TreeOption {
  key: string
  label: string
  children?: PermissionTreeOption[]
}
