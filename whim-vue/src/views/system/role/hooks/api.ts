import { request, RequestMethod } from '@/api'
import type { TablePage } from '@/components/table'
import type {
  RoleDataScope,
  RoleId,
  RoleItem,
  RolePermissions,
  RolePermissionSave,
  RoleQuery,
  RoleSave,
  RoleStatus,
} from './types'

export const fetchRolePage = (params: RoleQuery) =>
  request<TablePage<RoleItem>>('/system/role/page', RequestMethod.GET, { params })

export const fetchRoleDetail = (id: RoleId) =>
  request<RoleItem>('/system/role/detail', RequestMethod.GET, { params: { id } })

export const fetchRoleDataScope = (id: RoleId) =>
  request<RoleDataScope>('/system/role/dataScope', RequestMethod.GET, { params: { id } })

/** 复用已有的角色菜单树接口和实际勾选回显。 */
export const fetchRolePermissions = (id: RoleId) =>
  request<RolePermissions>(`/system/permission/roleTree/${id}`, RequestMethod.GET)

export const addRole = (model: RoleSave) =>
  request('/system/role/add', RequestMethod.POST, {
    data: { ...getRoleFields(model), status: model.status },
  })

/** 基础资料请求不携带状态、数据范围或权限，保持各操作权限独立。 */
export const updateRole = (model: RoleSave) =>
  request('/system/role/update', RequestMethod.PUT, {
    data: { id: model.id, ...getRoleFields(model) },
  })

export const updateRoleStatus = (data: { id: RoleId; status: RoleStatus }) =>
  request('/system/role/updateStatus', RequestMethod.PUT, { data })

export const assignRolePermissions = (data: RolePermissionSave) =>
  request('/system/role/assignPermissions', RequestMethod.PUT, { data })

export const assignRoleDataScope = (data: RoleDataScope) =>
  request('/system/role/assignDataScope', RequestMethod.PUT, { data })

export const deleteRole = (id: RoleId) =>
  request('/system/role/delete', RequestMethod.DELETE, { params: { id } })

function getRoleFields(model: RoleSave) {
  return {
    roleName: model.roleName.trim(),
    roleCode: model.roleCode.trim(),
    sort: model.sort,
    remark: model.remark?.trim() || null,
  }
}
