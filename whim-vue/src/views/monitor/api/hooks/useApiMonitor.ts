import { computed } from 'vue'
import dayjs from 'dayjs'
import type { DataTableColumns } from 'naive-ui'
import { useMonitorSampling } from '@/hooks/useMonitorSampling'
import { getApiMonitorSnapshot } from './api'
import type { ApiEndpointMetrics } from './types'

const numberFormat = new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 })
const metrics = [
  { field: 'requestsPerSecond', label: '每秒请求数', title: '请求吞吐趋势', unit: '次/秒', secondary: false },
  { field: 'averageDurationMillis', label: '平均耗时', title: '平均耗时趋势', unit: 'ms', secondary: true },
  { field: 'serverErrorPercent', label: '5xx 错误率', title: '5xx 错误率趋势', unit: '%', secondary: false },
] as const

const columns: DataTableColumns<ApiEndpointMetrics> = [
  { title: '方法', key: 'method', width: 85 },
  { title: '路由模板', key: 'path', minWidth: 215 },
  { title: '请求次数', key: 'requestCount', align: 'right', width: 100,
    render: row => numberFormat.format(BigInt(row.requestCount)) },
  { title: '平均耗时（ms）', key: 'averageDurationMillis', align: 'right', width: 140,
    render: row => numberFormat.format(row.averageDurationMillis) },
  { title: '5xx 次数', key: 'serverErrorCount', align: 'right', width: 100,
    render: row => numberFormat.format(BigInt(row.serverErrorCount)) },
]
const rowKey = (row: ApiEndpointMetrics) => `${row.method} ${row.path}`

/** 读取后端采集的完整窗口，打开页面时即可回看最近五分钟。 */
export function useApiMonitor() {
  const { samples, canView, loading, failed, refresh } = useMonitorSampling(
    'monitor:api:query',
    getApiMonitorSnapshot,
    false,
  )
  const monitor = computed(() => {
    const sample = samples.value.at(-1)
    if (!sample) return
    const latest = sample.trend.at(-1)
    return {
      start: dayjs(sample.windowStartedAt).format('HH:mm:ss'),
      end: dayjs(sample.sampledAt).format('HH:mm:ss.SSS'),
      hasTrend: Boolean(latest),
      metrics: metrics.map(metric => {
        const value = latest?.[metric.field]
        const points = sample.trend.map<[number, number | null]>(point => [point.sampledAt, point[metric.field]])
        return { ...metric, value: value == null ? '—' : numberFormat.format(value), points }
      }),
      endpoints: sample.endpoints,
    }
  })
  return { canView, loading, failed, monitor, columns, rowKey, refresh }
}
