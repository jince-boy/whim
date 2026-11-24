<script setup lang="ts">
import VChart from 'vue-echarts'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { useCacheMonitor } from './hooks/useCacheMonitor'

defineOptions({ name: 'MonitorCache' })
const { canView, loading, failed, overview, trends, refresh } = useCacheMonitor()
</script>

<template>
  <div class="cache-monitor">
    <n-card v-if="!canView"><n-empty description="暂无缓存监控查询权限" /></n-card>
    <template v-else>
      <n-card title="Redis 运行概况">
        <template #header-extra>
          <n-button secondary type="info" :loading="loading" @click="refresh">
            <template #icon><AppIcon name="refresh-cw" /></template>刷新
          </n-button>
        </template>
        <n-alert v-if="failed" type="warning" class="sampling-note">采集失败，自动刷新已暂停。点击刷新可重新采集。</n-alert>
        <p v-if="overview" class="sampling-note">
          <n-text depth="3">{{ overview.scope }} · 最近采样 {{ overview.time }} · 每 5 秒采样，趋势保留最近 5 分钟</n-text>
        </p>
        <div v-if="overview" class="metrics">
          <div v-for="metric in overview.metrics" :key="metric.label" class="metric">
            <n-text depth="3" class="metric-label"><AppIcon :name="metric.icon" />{{ metric.label }}</n-text>
            <div class="metric-reading">
              <n-text strong>{{ metric.value }}</n-text>
              <n-text v-if="metric.unit" depth="3" class="unit">{{ metric.unit }}</n-text>
            </div>
          </div>
        </div>
        <n-skeleton v-else-if="loading" text :repeat="2" />
      </n-card>

      <template v-if="overview">
        <div class="trends">
          <n-card v-for="trend in trends" :key="trend.title" :title="trend.title" size="small">
            <template #header-extra><n-text depth="3">{{ trend.unit }}</n-text></template>
            <VChart class="trend" :option="trend.option" autoresize role="img" :aria-label="`最近五分钟的 Redis ${trend.title}`" />
          </n-card>
        </div>
        <n-card title="键空间累计统计" size="small">
          <div class="metrics">
            <div v-for="counter in overview.counters" :key="counter.label" class="metric">
              <n-text depth="3">{{ counter.label }}</n-text>
              <n-text strong class="counter-value">{{ counter.value }}</n-text>
            </div>
          </div>
        </n-card>
      </template>
    </template>
  </div>
</template>

<style scoped>
.cache-monitor { display: grid; gap: 16px; }
.metric-label, .metric-reading { display: flex; align-items: center; gap: 8px; }
.sampling-note { margin: 0 0 12px; }
.metrics, .trends { display: grid; gap: 16px; }
.metrics { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.metric { display: grid; align-content: start; gap: 6px; min-width: 0; }
.metric-reading { align-items: baseline; flex-wrap: wrap; gap: 4px; font-size: clamp(20px, 2.3vw, 30px); font-variant-numeric: tabular-nums; }
.unit { font-size: 12px; }
.trends { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.trend { width: 100%; height: 230px; }
.counter-value { font-size: 18px; overflow-wrap: anywhere; }
@media (max-width: 850px) {
  .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .trends { grid-template-columns: minmax(0, 1fr); }
}
</style>
