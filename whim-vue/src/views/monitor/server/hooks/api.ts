import { request, RequestMethod } from '@/api'
import type { ServerMonitorSnapshot } from './types'

/** 读取当前后端实例的服务器指标。 */
export const getServerMonitorSnapshot = (signal: AbortSignal) =>
  request<ServerMonitorSnapshot>('/system/monitor/server/snapshot', RequestMethod.GET, { signal })
