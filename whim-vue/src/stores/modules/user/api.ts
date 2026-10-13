import { request, RequestMethod } from '@/api'
import type { UserInfoResult } from './types'

/** 获取当前登录用户及其有效角色、权限。 */
export const getUserInfo = () => {
  return request<UserInfoResult>('/system/auth/userInfo', RequestMethod.GET)
}
