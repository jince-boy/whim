<script setup lang="ts">
import { computed } from 'vue'
import { useThemeVars } from 'naive-ui'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { monitorSampleInterval } from '@/hooks/useMonitorSampling'

use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const props = defineProps<{
  title: string
  unit: string
  points: [number, number | null][]
  secondary?: boolean
  scale?: boolean
  maximum?: number
  minInterval?: number
}>()
const theme = useThemeVars()
const numberFormat = new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 })
const option = computed<EChartsOption>(() => {
  const colors = theme.value
  const data: [number, number | null][] = []
  let previous: number | undefined
  for (const point of props.points) {
    // 让图中断线，避免跨越页面休眠或采集失败的时间段。
    if (previous !== undefined && point[0] - previous > monitorSampleInterval * 3) {
      data.push([previous + monitorSampleInterval, null])
    }
    data.push(point)
    previous = point[0]
  }
  return {
    animation: false,
    grid: { top: 14, right: 18, left: 10, bottom: 12, outerBoundsMode: 'same' },
    textStyle: { color: colors.textColor2 },
    tooltip: { trigger: 'axis', confine: true, valueFormatter: value =>
      value == null ? '—' : `${numberFormat.format(Number(value))} ${props.unit}` },
    xAxis: { type: 'time', splitNumber: 4,
      axisLabel: { color: colors.textColor3, hideOverlap: true, formatter: (time: number) => dayjs(time).format('HH:mm:ss') },
      axisLine: { lineStyle: { color: colors.dividerColor } } },
    yAxis: { type: 'value', scale: props.scale, max: props.maximum, minInterval: props.minInterval,
      axisLabel: { color: colors.textColor3 }, splitLine: { lineStyle: { color: colors.dividerColor } } },
    series: [{ name: props.title, type: 'line', data, symbolSize: 4,
      itemStyle: { color: props.secondary ? colors.infoColor : colors.primaryColor },
      lineStyle: { width: 2 }, areaStyle: { opacity: 0.08 } }],
  }
})
</script>

<template>
  <n-card :title="props.title" size="small">
    <template #header-extra><n-text depth="3">{{ props.unit }}</n-text></template>
    <VChart class="trend" :option="option" autoresize role="img" :aria-label="`最近五分钟的${props.title}`" />
  </n-card>
</template>

<style scoped>
.trend { width: 100%; height: 230px; }
</style>
