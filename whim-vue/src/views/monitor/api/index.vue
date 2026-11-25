<script setup lang="ts">
import { pageCardThemeOverrides } from '@/config/NaiveTheme'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import MonitorTrend from '@/components/monitorTrend/MonitorTrend.vue'
import { useApiMonitor } from './hooks/useApiMonitor'

defineOptions({ name: 'MonitorApi' })
const { canView, loading, failed, monitor, columns, rowKey, refresh } = useApiMonitor()
</script>

<template>
  <div class="api-monitor">
    <n-card :theme-overrides="pageCardThemeOverrides" v-if="!canView"><n-empty description="暂无接口监控查询权限" /></n-card>
    <template v-else>
      <n-card :theme-overrides="pageCardThemeOverrides" title="接口运行概况">
        <template #header-extra>
          <n-button secondary type="info" size="small" :loading="loading" @click="refresh">
            <template #icon><AppIcon name="refresh-cw" /></template>刷新
          </n-button>
        </template>
        <n-alert v-if="failed" type="warning" class="note">采集失败，自动刷新已暂停。点击刷新可重试。</n-alert>
        <template v-if="monitor">
          <p class="note"><n-text depth="3">当前实例 · /system 接口 · 每 5 秒自动刷新 · 数据更新至 {{ monitor.end }}</n-text></p>
          <div class="metrics">
            <n-statistic v-for="metric in monitor.metrics" :key="metric.field"
              :label="metric.label" :value="metric.value" tabular-nums>
              <template #suffix>{{ metric.unit }}</template>
            </n-statistic>
          </div>
        </template>
        <n-skeleton v-else-if="loading" text :repeat="2" />
      </n-card>
      <template v-if="monitor">
        <div v-if="monitor.hasTrend" class="trends">
          <MonitorTrend v-for="metric in monitor.metrics" :key="metric.field"
            :title="metric.title" :unit="metric.unit" :points="metric.points" :secondary="metric.secondary" />
        </div>
        <n-card :theme-overrides="pageCardThemeOverrides" v-else size="small"><n-empty description="正在建立采样基线" /></n-card>
        <n-card :theme-overrides="pageCardThemeOverrides" title="慢接口排行 · 最近 5 分钟" size="small">
          <p class="note"><n-text depth="3">{{ monitor.start }} 至 {{ monitor.end }} · 按平均耗时从高到低排列</n-text></p>
          <n-data-table v-if="monitor.endpoints.length" size="small" :bordered="false"
            :columns="columns" :data="monitor.endpoints" :row-key="rowKey" :scroll-x="640"
            :pagination="{ pageSize: 10, size: 'small' }" :paginate-single-page="false" />
          <n-empty v-else description="当前统计时段内暂无已完成的接口请求" />
        </n-card>
      </template>
    </template>
  </div>
</template>

<style scoped>
.api-monitor, .metrics, .trends { display: grid; gap: 16px; }
.api-monitor { grid-template-columns: minmax(0, 1fr); min-width: 0; }
.metrics, .trends { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.note { margin: 0 0 14px; }
@media (max-width: 960px) { .trends { grid-template-columns: minmax(0, 1fr); } }
@media (max-width: 600px) { .metrics { grid-template-columns: minmax(0, 1fr); } }
</style>
