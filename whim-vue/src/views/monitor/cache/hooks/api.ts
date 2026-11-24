import { request, RequestMethod } from '@/api'
import type { CacheMonitorSnapshot } from './types'

/** 查询一次 Redis 运行指标，离开监控页面时可取消采样。 */
export const getCacheMonitorSnapshot = (signal: AbortSignal) =>
  request<CacheMonitorSnapshot>('/system/monitor/cache/snapshot', RequestMethod.GET, { signal })
