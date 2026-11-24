/** 同一采样区间的吞吐量、平均耗时与 HTTP 5xx 占比。 */
interface ApiTrendPoint {
  sampledAt: number
  requestsPerSecond: number
  averageDurationMillis: number | null
  serverErrorPercent: number | null
}

/** 最近五分钟内同一 HTTP 方法和路由模板的统计。 */
export interface ApiEndpointMetrics {
  method: string
  path: string
  requestCount: number | string
  averageDurationMillis: number
  serverErrorCount: number | string
}

export interface ApiMonitorSnapshot {
  sampledAt: number
  windowStartedAt: number
  trend: ApiTrendPoint[]
  endpoints: ApiEndpointMetrics[]
}
