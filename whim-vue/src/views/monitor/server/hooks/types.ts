/** 后端 Long 超过 JavaScript 安全整数范围时以十进制字符串返回。 */
export type ByteCount = number | string

export interface ServerMonitorSnapshot {
  sampledAt: number
  instanceId: string
  hostName: string
  operatingSystem: string
  architecture: string
  uptimeSeconds: number
  availableProcessors: number
  cpuUsagePercent: number | null
  totalMemoryBytes: ByteCount
  freeMemoryBytes: ByteCount
  disks: {
    mount: string
    type: string
    totalBytes: ByteCount
    freeBytes: ByteCount
    usableBytes: ByteCount
  }[]
  networkInterfaces: {
    index: number
    name: string
    receivedBytes: ByteCount
    sentBytes: ByteCount
  }[]
}
