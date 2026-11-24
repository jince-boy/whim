import { request, RequestMethod } from '@/api'
import type { UserInfoResult } from '@/stores/modules/user/types'
import type { LoginSession, ProfilePasswordUpdate, ProfileUpdate, TwoFactorSetup, TwoFactorConfirm, TwoFactorDisable } from './types'

/** 读取本人当前有效的登录会话。 */
export function getLoginSessions(signal: AbortSignal) {
  return request<LoginSession[]>('/system/auth/sessions', RequestMethod.GET, { signal })
}

/** 仅撤销选中的其他终端。 */
export function revokeLoginSession(sessionId: string) {
  return request<void>(`/system/auth/sessions/${encodeURIComponent(sessionId)}`, RequestMethod.DELETE)
}

/** 退出本人所有终端，包括当前浏览器。 */
export function logoutAllSessions() {
  return request<void>('/system/auth/logoutAll', RequestMethod.POST)
}

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

/** 获取本人的实际绑定状态。 */
export function getTwoFactorStatus(signal: AbortSignal) {
  return request<boolean>('/system/auth/twoFactorStatus', RequestMethod.GET, { signal })
}

/** 验证密码，申请仅在本次绑定中展示的密钥和恢复码。 */
export function startTwoFactorSetup(data: { password: string }) {
  return request<TwoFactorSetup>('/system/auth/twoFactorSetup', RequestMethod.POST, { data })
}

/** 确认第一个动态码后正式开启。 */
export function enableTwoFactor(data: TwoFactorConfirm) {
  return request<void>('/system/auth/twoFactorEnable', RequestMethod.POST, { data })
}

/** 核验当前密码及第二因素后解除绑定。 */
export function disableTwoFactor(data: TwoFactorDisable) {
  return request<void>('/system/auth/twoFactorDisable', RequestMethod.POST, { data })
}
