import { request, RequestMethod } from '@/api'
import type { HomeRoute, UserMenu } from './types'
/** 获取当前用户导航，不要求拥有菜单管理权限。 */
export const fetchUserMenus = () =>
  request<UserMenu[]>('/system/permission/userMenus', RequestMethod.GET)

export const fetchCurrentHomeRoutes = () =>
  request<HomeRoute[]>('/system/homePage/current', RequestMethod.GET)
