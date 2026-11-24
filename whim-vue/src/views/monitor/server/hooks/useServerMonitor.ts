import { computed } from 'vue'
import dayjs from 'dayjs'
import { monitorSampleInterval, useMonitorSampling } from '@/hooks/useMonitorSampling'
import { getServerMonitorSnapshot } from './api'
import type { ByteCount } from './types'

const byteUnits = ['B', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB']
const amountFormat = new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 })

/** 使用二进制单位展示容量和每秒流量。 */
function formatBytes(value: ByteCount) {
  let amount = Number(value)
  if (!Number.isFinite(amount) || amount < 0) return '—'
  let unit = 0
  while (amount >= 1024 && unit < byteUnits.length - 1) {
    amount /= 1024
    unit++
  }
  return `${amountFormat.format(amount)} ${byteUnits[unit]}`
}

function usedPercent(total: ByteCount, free: ByteCount) {
  const capacity = Number(total)
  const available = Number(free)
  if (capacity <= 0 || available < 0 || available > capacity) return null
  return (1 - available / capacity) * 100
}

/** 对同一网卡的相邻计数先作精确差，再换算为速率；计数器重置时等待下一次采样。 */
function formatRate(current: ByteCount, previous: ByteCount | undefined, elapsed: number) {
  if (previous === undefined || elapsed <= 0 || elapsed > monitorSampleInterval * 3) return '—'
  const start = BigInt(previous)
  const delta = BigInt(current) - start
  if (start < 0n || delta < 0n) return '—'
  return `${formatBytes((Number(delta) * 1000) / elapsed)}/s`
}

/** 组织服务器资源概览、趋势、挂载卷与网卡读数。 */
export function useServerMonitor() {
  const { canView, samples, history, loading, failed, refresh } = useMonitorSampling(
    'monitor:server:query',
    getServerMonitorSnapshot,
    (sample) => ({
      sampledAt: sample.sampledAt,
      cpu: sample.cpuUsagePercent,
      memory: usedPercent(sample.totalMemoryBytes, sample.freeMemoryBytes),
    }),
  )
  const overview = computed(() => {
    const sample = samples.value.at(-1)
    if (!sample) return
    const prior = samples.value.at(-2)
    const elapsed = prior ? sample.sampledAt - prior.sampledAt : 0
    const previousInterfaces = new Map(
      prior?.networkInterfaces.map((network) => [network.index, network]),
    )
    const memory = history.value.at(-1)?.memory
    const hours = Math.floor(sample.uptimeSeconds / 3600)
    return {
      host: sample.hostName || '当前实例',
      time: dayjs(sample.sampledAt).format('HH:mm:ss'),
      system: `${sample.operatingSystem} · ${sample.architecture}`,
      uptime: `${Math.floor(hours / 24)} 天 ${hours % 24} 小时 ${Math.floor(sample.uptimeSeconds / 60) % 60} 分钟`,
      cpu: sample.cpuUsagePercent == null ? '—' : `${sample.cpuUsagePercent.toFixed(1)}%`,
      processors: sample.availableProcessors,
      memory: memory == null ? '—' : `${memory.toFixed(1)}%`,
      memoryTotal: formatBytes(sample.totalMemoryBytes),
      memoryFree: formatBytes(sample.freeMemoryBytes),
      disks: sample.disks.map((disk) => ({
        mount: disk.mount,
        type: disk.type,
        total: formatBytes(disk.totalBytes),
        usable: formatBytes(disk.usableBytes),
        used: usedPercent(disk.totalBytes, disk.freeBytes),
      })),
      networks: sample.networkInterfaces.map((network) => {
        const previousInterface = previousInterfaces.get(network.index)
        const sameInterface = previousInterface?.name === network.name ? previousInterface : undefined
        return {
          name: network.name,
          index: network.index,
          received: formatBytes(network.receivedBytes),
          sent: formatBytes(network.sentBytes),
          receiveRate: formatRate(network.receivedBytes, sameInterface?.receivedBytes, elapsed),
          sendRate: formatRate(network.sentBytes, sameInterface?.sentBytes, elapsed),
        }
      }),
    }
  })
  const trends = computed(() => {
    const cpu: [number, number | null][] = []
    const memory: [number, number | null][] = []
    for (const sample of history.value) {
      cpu.push([sample.sampledAt, sample.cpu])
      memory.push([sample.sampledAt, sample.memory])
    }
    return { cpu, memory }
  })
  return { canView, overview, trends, loading, failed, refresh }
}
