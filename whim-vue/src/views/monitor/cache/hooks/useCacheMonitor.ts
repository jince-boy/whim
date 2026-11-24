import {
  computed,
  onActivated,
  onDeactivated,
  onMounted,
  onUnmounted,
  ref,
  shallowRef,
  watch,
} from 'vue'
import { useThemeVars } from 'naive-ui'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/modules/user'
import { getCacheMonitorSnapshot } from './api'
import type { CacheMonitorSnapshot } from './types'

use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const sampleInterval = 5000
const historyWindow = 5 * 60 * 1000
const maxSamples = 120
const bytesPerMiB = 1024 * 1024
const numberFormat = new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 })

/** 保留超出 JavaScript 安全整数范围的 Redis 计数。 */
function formatCount(value: number | string) {
  return numberFormat.format(BigInt(value))
}

/** 管理页面可见期间的 Redis 采样、指标和趋势图。 */
export function useCacheMonitor() {
  const user = useUserStore()
  const theme = useThemeVars()
  const canView = computed(() => user.hasPermission('monitor:cache:query'))
  const active = ref(false)
  const visible = ref(!document.hidden)
  const refreshKey = ref(0)
  const loading = ref(false)
  const failed = ref(false)
  const samples = shallowRef<CacheMonitorSnapshot[]>([])

  function refresh() {
    if (!loading.value) refreshKey.value++
  }

  watch(
    [canView, active, visible, refreshKey],
    async ([allowed, pageActive, tabVisible], _, onCleanup) => {
      if (!allowed) {
        samples.value = []
        failed.value = false
      }
      loading.value = allowed && pageActive && tabVisible
      if (!loading.value) return

      const controller = new AbortController()
      let timer: number | undefined
      // 页面隐藏、权限收回和卸载都会结束本轮采样，旧响应不能覆盖新数据。
      onCleanup(() => {
        controller.abort()
        window.clearTimeout(timer)
      })
      try {
        const { code, data } = await getCacheMonitorSnapshot(controller.signal)
        if (controller.signal.aborted) return
        failed.value = code !== 200
        if (failed.value) return
        const cutoff = data.sampledAt - historyWindow
        const recent = samples.value.filter(
          (sample) => sample.sampledAt >= cutoff && sample.sampledAt < data.sampledAt,
        )
        samples.value = [...recent, data].slice(-maxSamples)
        timer = window.setTimeout(refresh, sampleInterval)
      } catch {
        // 采集失败时保留上次成功读数，等待手动重试。
        if (!controller.signal.aborted) failed.value = true
      } finally {
        if (!controller.signal.aborted) loading.value = false
      }
    },
    { flush: 'sync' },
  )

  function syncVisibility() {
    visible.value = !document.hidden
  }
  onMounted(() => {
    document.addEventListener('visibilitychange', syncVisibility)
    active.value = true
  })
  onUnmounted(() => document.removeEventListener('visibilitychange', syncVisibility))
  onActivated(() => {
    active.value = true
  })
  onDeactivated(() => {
    active.value = false
  })

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
      metrics: [
        {
          label: '已用内存',
          icon: 'database',
          value: numberFormat.format(useGiB ? memoryMiB / 1024 : memoryMiB),
          unit: useGiB ? 'GiB' : 'MiB',
        },
        {
          label: '每秒命令数',
          icon: 'activity',
          value: formatCount(sample.commandsPerSecond),
          unit: '次/秒',
        },
        {
          label: '客户端连接',
          icon: 'cable',
          value: formatCount(sample.connectedClients),
          unit: '个',
        },
        {
          label: '累计命中率',
          icon: 'crosshair',
          value: lookups ? ((hits / lookups) * 100).toFixed(2) : '—',
          unit: lookups ? '%' : '',
        },
      ],
      counters: [
        { label: '键查找命中', value: formatCount(sample.keyspaceHits) },
        { label: '键查找未命中', value: formatCount(sample.keyspaceMisses) },
        { label: '过期键', value: formatCount(sample.expiredKeys) },
        { label: '内存淘汰键', value: formatCount(sample.evictedKeys) },
      ],
    }
  })

  const trends = computed(() => {
    const memory: [number, number | null][] = []
    const commands: [number, number | null][] = []
    let previousTime: number | undefined
    for (const sample of samples.value) {
      // 两张图在同一处断线，避免把暂停期间的空白画成连续趋势。
      if (previousTime !== undefined && sample.sampledAt - previousTime > sampleInterval * 3) {
        const gap = previousTime + sampleInterval
        memory.push([gap, null])
        commands.push([gap, null])
      }
      memory.push([sample.sampledAt, Number(sample.usedMemoryBytes) / bytesPerMiB])
      commands.push([sample.sampledAt, Number(sample.commandsPerSecond)])
      previousTime = sample.sampledAt
    }
    const colors = theme.value
    const charts = [
      {
        title: '内存趋势',
        unit: 'MiB',
        data: memory,
        color: colors.primaryColor,
        axis: { scale: true },
      },
      {
        title: '命令吞吐趋势',
        unit: '次/秒',
        data: commands,
        color: colors.infoColor,
        axis: { minInterval: 1 },
      },
    ]
    return charts.map(({ title, unit, data, color, axis }) => ({
      title,
      unit,
      option: {
        animation: false,
        grid: { top: 14, right: 18, left: 10, bottom: 12, outerBoundsMode: 'same' },
        textStyle: { color: colors.textColor2 },
        tooltip: {
          trigger: 'axis',
          confine: true,
          valueFormatter: (value) =>
            value == null ? '—' : `${numberFormat.format(Number(value))} ${unit}`,
        },
        xAxis: {
          type: 'time',
          splitNumber: 4,
          axisLabel: {
            color: colors.textColor3,
            hideOverlap: true,
            formatter: (time: number) => dayjs(time).format('HH:mm:ss'),
          },
          axisLine: { lineStyle: { color: colors.dividerColor } },
        },
        yAxis: {
          type: 'value',
          ...axis,
          axisLabel: { color: colors.textColor3 },
          splitLine: { lineStyle: { color: colors.dividerColor } },
        },
        series: [
          {
            name: title,
            type: 'line',
            data,
            showSymbol: data.length === 1,
            symbolSize: 7,
            itemStyle: { color },
            lineStyle: { width: 2 },
            areaStyle: { opacity: 0.08 },
          },
        ],
      } satisfies EChartsOption,
    }))
  })
  return { canView, loading, failed, overview, trends, refresh }
}
