/** 超出 JavaScript 安全整数范围的计数由后端编码为十进制字符串。 */
export type Count = number | string

export interface SlowSqlStatement {
  sqlTemplate: string
  count: Count
  totalDurationMillis: Count
  maxDurationMillis: Count
  lastOccurredAt: number
}

export interface SlowSqlSnapshot {
  sampledAt: number
  windowStartedAt: number
  thresholdMillis: Count
  trend: { startedAt: number; count: Count }[]
  statements: SlowSqlStatement[]
}

export interface ConnectionPool {
  name: string
  active: number | null
  idle: number | null
  maximum: number | null
  pending: number | null
  timeoutCount: Count | null
}

export interface ConnectionPoolSnapshot {
  sampledAt: number
  instanceId: string
  pools: ConnectionPool[]
}
