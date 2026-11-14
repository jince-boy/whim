import { request, RequestMethod } from '@/api'
import type { UserInfoResult } from '@/stores/modules/user/types'
import type { ProfilePasswordUpdate, ProfileUpdate } from './types'

/** 上传本人的头像文件，不要求用户管理权限。 */
export function uploadProfileAvatar(file: File) {
  const data = new FormData()
  data.append('file', file)
  return request<string>('/system/auth/uploadAvatar', RequestMethod.POST, { data, timeout: 60000 })
}

/** 保存当前账号允许自行维护的个人资料。 */
export function updateUserProfile(data: ProfileUpdate) {
  return request<UserInfoResult>('/system/auth/profile', RequestMethod.PUT, { data })
}

/** 原密码和新密码由公共请求层加密后提交，不包含目标用户ID。 */
export function updateUserPassword(data: ProfilePasswordUpdate) {
  return request<void>('/system/auth/password', RequestMethod.PUT, { data })
}
