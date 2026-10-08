import { downloadFile, request, RequestMethod } from '@/api'
import type { OperLogPageResult } from './types'

/** 分页查询操作日志。 */
export const fetchOperLogPage = (params: object) => {
  return request<OperLogPageResult>('/system/operLog/page', RequestMethod.GET, { params })
}

/** 导出操作日志。 */
export const exportOperLog = () => {
  return downloadFile('/system/operLog/export')
}

/** 删除指定操作日志。 */
export const deleteOperLog = (params: object) => {
  return request('/system/operLog/delete', RequestMethod.DELETE, { params })
}

/** 清空操作日志。 */
export const cleanOperLog = () => {
  return request('/system/operLog/clean', RequestMethod.DELETE)
}
