/** Long 超出 JavaScript 安全整数范围时，后端改用十进制字符串。 */
type RedisCounter = number | string

/** 单机或集群主节点的完整采样结果。 */
export interface CacheMonitorSnapshot {
  sampledAt: number
  cluster: boolean
  primaryNodeCount: number
  usedMemoryBytes: RedisCounter
  connectedClients: RedisCounter
  commandsPerSecond: RedisCounter
  keyspaceHits: RedisCounter
  keyspaceMisses: RedisCounter
  expiredKeys: RedisCounter
  evictedKeys: RedisCounter
}
