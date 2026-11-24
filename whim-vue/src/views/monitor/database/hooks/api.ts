import { request, RequestMethod } from '@/api'
import type { ConnectionPoolSnapshot, SlowSqlSnapshot } from './types'

export const getSlowSqlSnapshot = (minutes: number, signal: AbortSignal) =>
  request<SlowSqlSnapshot>('/system/monitor/database/slowSql', RequestMethod.GET, { params: { minutes }, signal })

export const getConnectionPoolSnapshot = (signal: AbortSignal) =>
  request<ConnectionPoolSnapshot>('/system/monitor/database/connectionPool', RequestMethod.GET, { signal })
