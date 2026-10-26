import { downloadFile, request, RequestMethod } from '@/api'
import type { DictDataPageResult, DictDataQuery, DictDataResult } from './types'

/** 分页查询字典数据。 */
export const fetchDictDataPage = (params: object) => {
  return request<DictDataPageResult>('/system/dictData/page', RequestMethod.GET, { params })
}

/** 查询字典数据详情。 */
export const fetchDictDataDetail = (params: object) => {
  return request<DictDataResult>('/system/dictData/detail', RequestMethod.GET, { params })
}

/** 新增字典数据。 */
export const insertDictData = (data: object) => {
  return request('/system/dictData/add', RequestMethod.POST, { data })
}

/** 修改字典数据。 */
export const updateDictData = (data: object) => {
  return request('/system/dictData/update', RequestMethod.PUT, { data })
}

/** 删除字典数据。 */
export const deleteDictData = (params: object) => {
  return request('/system/dictData/delete', RequestMethod.DELETE, { params })
}

/** 按字典类型查询数据，供字典选择组件复用。 */
export const fetchDictDataListByDictType = (params: string) => {
  return request<DictDataResult[]>('/system/dictData/type/' + params, RequestMethod.GET)
}

/** 导出指定类型下符合查询条件的字典值。 */
export const exportDictData = (data: DictDataQuery & { type: string }) => {
  return downloadFile('/system/dictData/export', null, { method: RequestMethod.POST, data })
}
