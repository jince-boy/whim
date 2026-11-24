<script setup lang="ts">
import { pageCardThemeOverrides } from '@/config/NaiveTheme'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import MonitorTrend from '@/components/monitorTrend/MonitorTrend.vue'
import { useConnectionPoolMonitor } from '../hooks/useConnectionPoolMonitor'

const { loading, failed, refresh, monitor } = useConnectionPoolMonitor()
</script>

<template>
  <div class="connection-pools">
    <n-card :theme-overrides="pageCardThemeOverrides" title="HikariCP 连接池" size="small">
      <template #header-extra>
        <n-button secondary type="info" :loading="loading" @click="refresh">
          <template #icon><AppIcon name="refresh-cw" /></template>刷新
        </n-button>
      </template>
      <n-alert v-if="failed" type="warning">采集失败，自动刷新已暂停。点击刷新可重试。</n-alert>
      <n-text v-if="monitor" depth="3">当前实例 · 最近采样 {{ monitor.time }} · 每 5 秒刷新</n-text>
      <n-skeleton v-else-if="loading" text :repeat="2" />
    </n-card>
    <template v-if="monitor">
      <template v-for="pool in monitor.pools" :key="pool.name">
        <n-card :theme-overrides="pageCardThemeOverrides" :title="pool.name" size="small">
          <div class="metrics">
            <n-statistic label="获取连接次数（累计）" :value="pool.acquireCount ?? '—'" tabular-nums />
            <n-statistic label="活跃连接" :value="pool.active ?? '—'" tabular-nums />
            <n-statistic label="空闲连接" :value="pool.idle ?? '—'" tabular-nums />
            <n-statistic label="连接上限" :value="pool.maximum ?? '—'" tabular-nums />
            <n-statistic label="等待线程" :value="pool.pending ?? '—'" tabular-nums />
            <n-statistic label="获取连接超时（累计）" :value="pool.timeoutCount ?? '—'" tabular-nums />
          </div>
        </n-card>
        <MonitorTrend :title="`${pool.name} 等待线程趋势`" unit="个" :points="pool.points" :min-interval="1" />
      </template>
      <n-card :theme-overrides="pageCardThemeOverrides" v-if="!monitor.pools.length" size="small"><n-empty description="当前实例尚无 HikariCP 连接池指标" /></n-card>
    </template>
  </div>
</template>

<style scoped>
.connection-pools { display: grid; grid-template-columns: minmax(0, 1fr); gap: 16px; }
.metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(135px, 1fr)); gap: 16px; }
</style>
