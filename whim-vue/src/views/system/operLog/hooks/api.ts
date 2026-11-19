import { downloadFile, request, RequestMethod } from '@/api'
import type { OperLogDetail, OperLogFilters, OperLogPageResult } from './types'

/** 分页查询操作日志。 */
export function fetchOperLogPage(params: OperLogFilters & { pageNum: number; pageSize: number }) {
  return request<OperLogPageResult>('/system/operLog/page', RequestMethod.GET, { params })
}

/** 按权限单独读取请求和响应摘要。 */
export function fetchOperLogDetail(id: string | number) {
  return request<OperLogDetail>('/system/operLog/detail', RequestMethod.GET, { params: { id } })
}

/** 按列表筛选条件导出。 */
export function exportOperLog(params: OperLogFilters) {
  return downloadFile('/system/operLog/export', null, { params })
}

/** 批量逻辑删除。 */
export function deleteOperLog(ids: Array<string | number>) {
  return request('/system/operLog/delete', RequestMethod.DELETE, {
    params: { operLogIds: ids.join(',') },
  })
}

/** 清空全部操作日志。 */
export function cleanOperLog() {
  return request('/system/operLog/clean', RequestMethod.DELETE)
}
