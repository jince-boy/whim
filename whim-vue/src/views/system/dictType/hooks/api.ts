import { request, downloadFile, RequestMethod } from '@/api'
import type { DictTypePageResult, DictTypeResult } from './types'

/** 分页查询字典类型。 */
export const fetchDictTypePage = (params: object) => {
  return request<DictTypePageResult>('/system/dictType/page', RequestMethod.GET, { params })
}

/** 查询字典类型详情。 */
export const fetchDictTypeDetail = (params: object) => {
  return request<DictTypeResult>('/system/dictType/detail', RequestMethod.GET, { params })
}

/** 新增字典类型。 */
export const insertDictType = (data: object) => {
  return request('/system/dictType/add', RequestMethod.POST, { data })
}

/** 修改字典类型。 */
export const updateDictType = (data: object) => {
  return request('/system/dictType/update', RequestMethod.PUT, { data })
}

/** 删除字典类型。 */
export const deleteDictType = (params: object) => {
  return request('/system/dictType/delete', RequestMethod.DELETE, { params })
}

/** 重置字典缓存。 */
export const resetDictCache = () => {
  return request('/system/dictType/reset', RequestMethod.DELETE)
}

/** 导出字典类型。 */
export const exportDictType = () => {
  return downloadFile('/system/dictType/export')
}
