<script setup lang="ts">
import AppIcon from '@/components/appIcon/AppIcon.vue'
import MonitorTrend from '@/components/monitorTrend/MonitorTrend.vue'
import { useSlowSqlMonitor } from '../hooks/useSlowSqlMonitor'

const { minutes, periods, loading, failed, refresh, monitor, columns, rowKey } = useSlowSqlMonitor()
</script>

<template>
  <div class="slow-sql">
    <n-card title="慢 SQL 统计" size="small">
      <template #header-extra>
        <n-button secondary type="info" :loading="loading" @click="refresh">
          <template #icon><AppIcon name="refresh-cw" /></template>刷新
        </n-button>
      </template>
      <n-flex align="center">
        <n-select v-model:value="minutes" aria-label="统计时段" :options="periods" class="period" />
        <n-text v-if="monitor" depth="3">
          当前实例 · {{ monitor.start }} 至 {{ monitor.end }} · 执行耗时 ≥ {{ monitor.threshold }} ms
        </n-text>
      </n-flex>
      <n-alert v-if="failed" class="status" type="warning">采集失败，自动刷新已暂停。点击刷新可重试。</n-alert>
      <n-statistic v-if="monitor" class="status" label="慢 SQL 执行次数" :value="monitor.count" tabular-nums />
      <n-skeleton v-else-if="loading" class="status" text :repeat="2" />
    </n-card>
    <template v-if="monitor">
      <MonitorTrend title="每分钟慢 SQL 次数" unit="次" :points="monitor.points" :sample-interval="60000" :min-interval="1" />
      <n-card title="慢 SQL 排行" size="small">
        <p><n-text depth="3">按累计耗时降序，最多展示 100 项</n-text></p>
        <n-data-table v-if="monitor.statements.length" size="small" :bordered="false" :columns="columns"
          :data="monitor.statements" :row-key="rowKey" :scroll-x="825"
          :pagination="{ pageSize: 10, size: 'small' }" :paginate-single-page="false" />
        <n-empty v-else description="所选时段内暂无达到阈值的 SQL" />
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
