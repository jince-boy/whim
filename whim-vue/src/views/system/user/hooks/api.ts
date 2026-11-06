import { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { PasswordFormModel, RoleFormModel, UserDetail, UserFormModel, UserId, UserItem, UserQuery, UserRoles } from './types'

export const fetchUserPage = (params: UserQuery & TablePageQuery) =>
  request<TablePage<UserItem>>('/system/user/page', RequestMethod.GET, { params })

export const fetchUserDetail = (id: UserId) =>
  request<UserDetail>('/system/user/detail', RequestMethod.GET, { params: { id } })

export const fetchUserRoles = (id: UserId) =>
  request<UserRoles>('/system/userRole/list', RequestMethod.GET, { params: { id } })

export const addUser = (data: UserFormModel) =>
  request('/system/user/add', RequestMethod.POST, {
    data: { ...getUserFields(data), username: data.username.trim(), password: data.password },
  })

export const updateUser = (data: UserFormModel) =>
  request('/system/user/update', RequestMethod.PUT, { data: { id: data.id, ...getUserFields(data) } })

export const deleteUser = (id: UserId) =>
  request('/system/user/delete', RequestMethod.DELETE, { params: { id } })

export const resetUserPassword = (data: PasswordFormModel) =>
  request('/system/user/resetPassword', RequestMethod.PUT, {
    data: { id: data.id, password: data.password },
  })

export const assignUserRoles = (data: RoleFormModel) =>
  request('/system/userRole/assign', RequestMethod.PUT, {
    data: { id: data.id, roleIds: data.roleIds },
  })

/** 仅发送可维护字段；空值明确清空，密码和确认密码不混入资料修改请求。 */
function getUserFields(data: UserFormModel) {
  return {
    name: data.name.trim(),
    deptId: data.deptId || null,
    email: data.email?.trim() || null,
    phone: data.phone?.trim() || null,
    gender: data.gender,
    status: data.status,
    postIds: data.postIds,
    remark: data.remark?.trim() || null,
  }
}

