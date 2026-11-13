import axiosInstance, { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { PasswordFormModel, RoleFormModel, UserAvatarQuery, UserDetail, UserFormModel, UserId, UserItem, UserQuery, UserRoles } from './types'

export const fetchUserPage = (params: UserQuery & TablePageQuery) =>
  request<TablePage<UserItem>>('/system/user/page', RequestMethod.GET, { params })

export const fetchUserDetail = (id: UserId) =>
  request<UserDetail>('/system/user/detail', RequestMethod.GET, { params: { id } })

export const fetchUserRoles = (id: UserId) =>
  request<UserRoles>('/system/userRole/list', RequestMethod.GET, { params: { id } })

export const addUser = async (data: UserFormModel) => {
  await uploadSelectedAvatar(data)
  return request('/system/user/add', RequestMethod.POST, {
    data: { ...getUserFields(data), username: data.username.trim(), password: data.password },
  })
}

export const updateUser = async (data: UserFormModel) => {
  await uploadSelectedAvatar(data)
  return request('/system/user/update', RequestMethod.PUT, { data: { id: data.id, ...getUserFields(data) } })
}

/** 头像图片通过认证客户端读取，令牌不拼入图片地址。 */
export async function fetchUserAvatar(params: UserAvatarQuery, signal: AbortSignal): Promise<Blob | undefined> {
  const response = await axiosInstance.get<Blob>('/system/user/avatar', {
    params, responseType: 'blob', signal, timeout: 35000,
  })
  if (response.status === 204) return
  return response.data
}

/** 点击保存后才上传所选图片；上传成功后再提交用户资料。 */
async function uploadSelectedAvatar(data: UserFormModel) {
  if (!data.avatarFile) return
  const body = new FormData()
  body.append('file', data.avatarFile)
  const response = await request<string>('/system/user/uploadAvatar', RequestMethod.POST, {
    data: body, timeout: 60000,
  })
  if (response.code !== 200) throw new Error(response.message || '头像上传失败')
  data.avatar = response.data
  data.avatarFile = null
}

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
    avatar: data.avatar || null,
    deptId: data.deptId || null,
    email: data.email?.trim() || null,
    phone: data.phone?.trim() || null,
    gender: data.gender,
    status: data.status,
    postIds: data.postIds,
    remark: data.remark?.trim() || null,
  }
}

