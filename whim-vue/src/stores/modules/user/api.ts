import { request, RequestMethod } from '@/api'
import type { UserInfoResult } from './types'

/** 获取当前登录用户及其角色、权限和菜单。 */
export const getUserInfo = () => {
  return request<UserInfoResult>('/system/user/getInfo', RequestMethod.GET)
}
