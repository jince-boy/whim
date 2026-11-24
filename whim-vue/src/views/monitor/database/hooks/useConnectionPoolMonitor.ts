import { computed } from 'vue'
import dayjs from 'dayjs'
import { useMonitorSampling } from '@/hooks/useMonitorSampling'
import { getConnectionPoolSnapshot } from './api'

/** 按池名保留等待线程趋势，缺失的池或读数在图中断开。 */
export function useConnectionPoolMonitor() {
  const { samples, history, loading, failed, refresh } = useMonitorSampling(
    'monitor:database:query', getConnectionPoolSnapshot,
    sample => ({
      sampledAt: sample.sampledAt,
      pending: new Map(sample.pools.map(pool => [pool.name, pool.pending])),
    }),
  )
  const monitor = computed(() => {
    const sample = samples.value.at(-1)
    if (!sample) return
    return {
      time: dayjs(sample.sampledAt).format('HH:mm:ss'),
      pools: sample.pools.map(pool => ({
        ...pool,
        points: history.value.map<[number, number | null]>(point => [point.sampledAt, point.pending.get(pool.name) ?? null]),
      })),
    }
  })
  return { loading, failed, refresh, monitor }
}
