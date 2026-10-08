import { request, RequestMethod } from '@/api'
import type { MenuItem } from './types'

/** 查询菜单列表。 */
export const fetchMenuList = (params?: object) => {
  return request<MenuItem[]>('/system/menu/list', RequestMethod.GET, { params })
}

/** 查询菜单详情。 */
export const fetchMenuDetail = (params: object) => {
  return request('/system/menu/detail', RequestMethod.GET, { params })
}

/** 新增菜单。 */
export const insertMenu = (data: object) => {
  return request('/system/menu/add', RequestMethod.POST, { data })
}

/** 修改菜单。 */
export const updateMenu = (data: object) => {
  return request('/system/menu/update', RequestMethod.PUT, { data })
}

/** 删除菜单。 */
export const deleteMenu = (params: object) => {
  return request('/system/menu/delete', RequestMethod.DELETE, { params })
}
