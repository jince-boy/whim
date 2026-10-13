import { request, RequestMethod } from '@/api'
import type { MenuId, MenuItem, MenuSave } from './types'
/** 查询完整管理树或保留祖先的搜索结果。 */
export const fetchMenuList = (params?: { menuName?: string }) =>
  request<MenuItem[]>('/system/menu', RequestMethod.GET, { params })
/** 获取编辑详情。 */
export const fetchMenuDetail = (id: MenuId) =>
  request<MenuItem>(`/system/menu/${id}`, RequestMethod.GET)
/** 新建节点。 */
export const insertMenu = (data: MenuSave) =>
  request<MenuId>('/system/menu', RequestMethod.POST, { data })
/** 更新完整配置。 */
export const updateMenu = (id: MenuId, data: MenuSave) =>
  request<void>(`/system/menu/${id}`, RequestMethod.PUT, { data })
/** 删除无引用的自定义节点。 */
export const deleteMenu = (id: MenuId) => request<void>(`/system/menu/${id}`, RequestMethod.DELETE)
