import { request, RequestMethod } from '@/api'
import type { ApiMonitorSnapshot } from './types'

/** 查询当前实例的完整接口统计窗口。 */
export const getApiMonitorSnapshot = (signal: AbortSignal) =>
  request<ApiMonitorSnapshot>('/system/monitor/api/snapshot', RequestMethod.GET, { signal })
