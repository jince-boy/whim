<script setup lang="ts">
import { pageCardThemeOverrides } from '@/config/NaiveTheme'
import MonitorTrend from '@/components/monitorTrend/MonitorTrend.vue'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { useCacheMonitor } from './hooks/useCacheMonitor'

defineOptions({ name: 'MonitorCache' })
const { canView, loading, failed, overview, trends, refresh } = useCacheMonitor()
</script>

<template>
  <div class="cache-monitor">
    <n-card :theme-overrides="pageCardThemeOverrides" v-if="!canView"><n-empty description="暂无缓存监控查询权限" /></n-card>
    <template v-else>
      <n-card :theme-overrides="pageCardThemeOverrides" title="Redis 运行概况">
        <template #header-extra>
          <n-button secondary type="info" size="small" :loading="loading" @click="refresh">
            <template #icon><AppIcon name="refresh-cw" /></template>刷新
          </n-button>
        </template>
        <n-alert v-if="failed" type="warning" class="sampling-note">采集失败，自动刷新已暂停。点击刷新可重新采集。</n-alert>
        <template v-if="overview">
          <p class="sampling-note">
            <n-text depth="3">{{ overview.scope }} · 最近采样 {{ overview.time }} · 每 5 秒采样，趋势保留最近 5 分钟</n-text>
          </p>
          <div class="metrics">
            <n-statistic :value="overview.memory" tabular-nums>
              <template #label><AppIcon name="database" /> 已用内存</template>
              <template #suffix>{{ overview.memoryUnit }}</template>
            </n-statistic>
            <n-statistic :value="overview.commandsPerSecond" tabular-nums>
              <template #label><AppIcon name="activity" /> 每秒命令数</template>
              <template #suffix>次/秒</template>
            </n-statistic>
            <n-statistic :value="overview.connectedClients" tabular-nums>
              <template #label><AppIcon name="cable" /> 客户端连接</template>
              <template #suffix>个</template>
            </n-statistic>
            <n-statistic :value="overview.hitRate" tabular-nums>
              <template #label><AppIcon name="crosshair" /> 累计命中率</template>
            </n-statistic>
          </div>
        </template>
        <n-skeleton v-else-if="loading" text :repeat="2" />
      </n-card>

      <template v-if="overview">
        <div class="trends">
          <MonitorTrend title="内存趋势" unit="MiB" :points="trends.memory" scale />
          <MonitorTrend title="命令吞吐趋势" unit="次/秒" :points="trends.commands" :min-interval="1" secondary />
        </div>
        <n-card :theme-overrides="pageCardThemeOverrides" title="键空间累计统计" size="small">
          <div class="metrics">
            <n-statistic label="键查找命中" :value="overview.hits" tabular-nums />
            <n-statistic label="键查找未命中" :value="overview.misses" tabular-nums />
            <n-statistic label="过期键" :value="overview.expiredKeys" tabular-nums />
            <n-statistic label="内存淘汰键" :value="overview.evictedKeys" tabular-nums />
          </div>
        </n-card>
      </template>
    </template>
  </div>
</template>

<style scoped>
.cache-monitor { display: grid; gap: 16px; }
.sampling-note { margin: 0 0 12px; }
.metrics, .trends { display: grid; gap: 16px; }
.metrics { grid-template-columns: repeat(4, minmax(0, 1fr)); overflow-wrap: anywhere; }
.trends { grid-template-columns: repeat(2, minmax(0, 1fr)); }
@media (max-width: 850px) {
  .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .trends { grid-template-columns: minmax(0, 1fr); }
}
</style>
