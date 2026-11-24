import { computed, ref } from 'vue'
import dayjs from 'dayjs'
import type { DataTableColumns } from 'naive-ui'
import { useMonitorSampling } from '@/hooks/useMonitorSampling'
import { getSlowSqlSnapshot } from './api'
import type { SlowSqlStatement } from './types'

const periods = [5, 15, 60].map(value => ({ value, label: `最近 ${value} 分钟` }))
const numberFormat = new Intl.NumberFormat('zh-CN')
const columns: DataTableColumns<SlowSqlStatement> = [
  { title: 'SQL 模板', key: 'sqlTemplate', minWidth: 300, ellipsis: { tooltip: true } },
  { title: '超阈值次数', key: 'count', align: 'right', width: 100,
    render: row => numberFormat.format(BigInt(row.count)) },
  { title: '累计耗时（ms）', key: 'totalDurationMillis', align: 'right', width: 135,
    render: row => numberFormat.format(BigInt(row.totalDurationMillis)) },
  { title: '最长耗时（ms）', key: 'maxDurationMillis', align: 'right', width: 135,
    render: row => numberFormat.format(BigInt(row.maxDurationMillis)) },
  { title: '最近发生', key: 'lastOccurredAt', width: 155,
    render: row => dayjs(row.lastOccurredAt).format('MM-DD HH:mm:ss') },
]
const rowKey = (row: SlowSqlStatement) => row.sqlTemplate

/** 查询后端保留的慢 SQL 统计，切换时段时立即重新采样。 */
export function useSlowSqlMonitor() {
  const minutes = ref(5)
  const { samples, loading, failed, refresh } = useMonitorSampling(
    'monitor:database:query', signal => getSlowSqlSnapshot(minutes.value, signal), false, () => minutes.value,
  )
  const monitor = computed(() => {
    const sample = samples.value.at(-1)
    if (!sample) return
    return {
      start: dayjs(sample.windowStartedAt).format('HH:mm:ss'),
      end: dayjs(sample.sampledAt).format('HH:mm:ss'),
      threshold: sample.thresholdMillis,
      count: numberFormat.format(sample.trend.reduce((sum, point) => sum + BigInt(point.count), 0n)),
      points: sample.trend.map<[number, number]>(point => [point.startedAt, Number(point.count)]),
      statements: sample.statements,
    }
  })
  return { minutes, periods, loading, failed, refresh, monitor, columns, rowKey }
}
