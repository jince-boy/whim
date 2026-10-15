import { request, RequestMethod } from '@/api'
import type { MenuItem } from '@/views/system/menu/hooks/types'
/** 获取当前用户导航，不要求拥有菜单管理权限。 */
export const fetchUserMenus = () => request<MenuItem[]>('/system/permission/userMenus', RequestMethod.GET)
