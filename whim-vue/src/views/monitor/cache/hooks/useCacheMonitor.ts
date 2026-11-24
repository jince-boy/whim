import { computed } from 'vue'
import dayjs from 'dayjs'
import { useMonitorSampling } from '@/hooks/useMonitorSampling'
import { getCacheMonitorSnapshot } from './api'

const bytesPerMiB = 1024 * 1024
const numberFormat = new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 })

/** 保留超出 JavaScript 安全整数范围的 Redis 计数。 */
function formatCount(value: number | string) {
  return numberFormat.format(BigInt(value))
}

/** 管理页面可见期间的 Redis 采样、指标和趋势图。 */
export function useCacheMonitor() {
  const { samples, canView, loading, failed, refresh } = useMonitorSampling(
    'monitor:cache:query',
    getCacheMonitorSnapshot,
  )

  const overview = computed(() => {
    const sample = samples.value.at(-1)
    if (!sample) return
    const memoryMiB = Number(sample.usedMemoryBytes) / bytesPerMiB
    const useGiB = memoryMiB >= 1024
    const hits = Number(sample.keyspaceHits)
    const lookups = hits + Number(sample.keyspaceMisses)
    return {
      scope: sample.cluster ? `集群 · ${sample.primaryNodeCount} 个主节点合计` : '单机实例',
      time: dayjs(sample.sampledAt).format('HH:mm:ss'),
      memory: numberFormat.format(useGiB ? memoryMiB / 1024 : memoryMiB),
      memoryUnit: useGiB ? 'GiB' : 'MiB',
      commandsPerSecond: formatCount(sample.commandsPerSecond),
      connectedClients: formatCount(sample.connectedClients),
      hitRate: lookups ? `${((hits / lookups) * 100).toFixed(2)}%` : '—',
      hits: formatCount(sample.keyspaceHits),
      misses: formatCount(sample.keyspaceMisses),
      expiredKeys: formatCount(sample.expiredKeys),
      evictedKeys: formatCount(sample.evictedKeys),
    }
  })

  const trends = computed(() => {
    const memory: [number, number][] = []
    const commands: [number, number][] = []
    for (const sample of samples.value) {
      memory.push([sample.sampledAt, Number(sample.usedMemoryBytes) / bytesPerMiB])
      commands.push([sample.sampledAt, Number(sample.commandsPerSecond)])
    }
    return { memory, commands }
  })
  return { canView, loading, failed, overview, trends, refresh }
}
