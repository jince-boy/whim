<script setup lang="ts">
import { pageCardThemeOverrides } from '@/config/NaiveTheme'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import MonitorTrend from '@/components/monitorTrend/MonitorTrend.vue'
import { useSlowSqlMonitor } from '../hooks/useSlowSqlMonitor'

const { minutes, periods, loading, failed, refresh, monitor, columns, rowKey } = useSlowSqlMonitor()
</script>

<template>
  <div class="slow-sql">
    <n-card :theme-overrides="pageCardThemeOverrides" title="慢 SQL 统计" size="small">
      <template #header-extra>
        <n-button secondary type="info" :loading="loading" @click="refresh">
          <template #icon><AppIcon name="refresh-cw" /></template>刷新
        </n-button>
      </template>
      <n-flex align="center">
        <n-select v-model:value="minutes" aria-label="统计时段" :options="periods" class="period" />
        <n-text v-if="monitor" depth="3">
          当前实例 · {{ monitor.start }} 至 {{ monitor.end }} · 耗时达到 {{ monitor.threshold }} ms 计为慢 SQL
        </n-text>
      </n-flex>
      <n-alert v-if="failed" class="status" type="warning">采集失败，自动刷新已暂停。点击刷新可重试。</n-alert>
      <n-flex v-if="monitor" class="status" :size="32">
        <n-statistic label="SQL 执行总次数" :value="monitor.executionCount" tabular-nums />
        <n-statistic label="其中慢 SQL 次数" :value="monitor.slowCount" tabular-nums />
      </n-flex>
      <n-skeleton v-else-if="loading" class="status" text :repeat="2" />
    </n-card>
    <template v-if="monitor">
      <MonitorTrend title="每分钟慢 SQL 次数" unit="次" :points="monitor.points" :sample-interval="60000" :min-interval="1" />
      <n-card :theme-overrides="pageCardThemeOverrides" title="慢 SQL 排行" size="small">
        <p><n-text depth="3">按 SQL 模板和执行来源分别统计，按累计耗时展示前 100 项</n-text></p>
        <n-data-table size="small" :bordered="false" :columns="columns" aria-label="慢 SQL 排行"
          :data="monitor.statements" :row-key="rowKey" :scroll-x="1185" min-height="clamp(220px, 32vh, 340px)"
          :pagination="{ pageSize: 10, size: 'small' }" :paginate-single-page="false">
          <template #empty><n-empty description="所选时段内暂无达到阈值的 SQL" /></template>
        </n-data-table>
      </n-card>
    </template>
  </div>
</template>

<style scoped>
.slow-sql { display: grid; grid-template-columns: minmax(0, 1fr); gap: 16px; }
.period { width: 145px; }
.status { margin-top: 12px; }
p { margin: 0 0 12px; }
</style>
