<script setup lang="ts">
import { pageCardThemeOverrides } from '@/config/NaiveTheme'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import MonitorTrend from '@/components/monitorTrend/MonitorTrend.vue'
import { useServerMonitor } from './hooks/useServerMonitor'

defineOptions({ name: 'MonitorServer' })
const { canView, overview, trends, loading, failed, refresh } = useServerMonitor()
</script>

<template>
  <div class="server-monitor">
    <n-card :theme-overrides="pageCardThemeOverrides" v-if="!canView"><n-empty description="暂无服务器监控查询权限" /></n-card>
    <template v-else>
      <n-card :theme-overrides="pageCardThemeOverrides" title="服务器概况">
        <template #header-extra>
          <n-button secondary type="info" size="small" :loading="loading" @click="refresh">
            <template #icon><AppIcon name="refresh-cw" /></template>刷新
          </n-button>
        </template>
        <n-alert v-if="failed" type="warning" class="note">采集失败，自动刷新已暂停。点击刷新可重新采集。</n-alert>
        <template v-if="overview">
          <p class="note"><n-text depth="3">{{ overview.host }} · 最近采样 {{ overview.time }} · 每 5 秒采样，趋势保留最近 5 分钟</n-text></p>
          <div class="resources">
            <div><n-text depth="3">CPU 使用率</n-text><strong>{{ overview.cpu }}</strong><n-text depth="3">{{ overview.processors }} 个可用逻辑处理器</n-text></div>
            <div><n-text depth="3">内存使用率</n-text><strong>{{ overview.memory }}</strong><n-text depth="3">空闲 {{ overview.memoryFree }} / 总计 {{ overview.memoryTotal }}</n-text></div>
            <div><n-text depth="3">操作系统</n-text><n-text strong>{{ overview.system }}</n-text><n-text depth="3">系统已运行 {{ overview.uptime }}</n-text></div>
          </div>
        </template>
        <n-skeleton v-else-if="loading" text :repeat="2" />
      </n-card>
      <template v-if="overview">
        <div class="trends">
          <MonitorTrend title="CPU 使用率" unit="%" :points="trends.cpu" :maximum="100" />
          <MonitorTrend title="内存使用率" unit="%" :points="trends.memory" :maximum="100" secondary />
        </div>
        <n-card :theme-overrides="pageCardThemeOverrides" title="本地磁盘分区" size="small">
          <div v-if="overview.disks.length" class="table-scroll">
            <n-table size="small" :bordered="false" :single-line="true">
              <thead><tr><th>挂载点</th><th>文件系统</th><th>总容量</th><th>可用空间</th><th>使用率</th></tr></thead>
              <tbody><tr v-for="disk in overview.disks" :key="disk.mount">
                <td>{{ disk.mount }}</td><td>{{ disk.type }}</td><td>{{ disk.total }}</td><td>{{ disk.usable }}</td>
                <td class="usage">
                  <n-progress v-if="disk.used !== null" type="line" :percentage="disk.used">
                    {{ Number(disk.used.toFixed(1)) }}%
                  </n-progress>
                  <span v-else>—</span>
                </td>
              </tr></tbody>
            </n-table>
          </div>
          <n-empty v-else description="当前环境未提供本地磁盘分区" />
        </n-card>
        <n-card :theme-overrides="pageCardThemeOverrides" title="网卡流量" size="small">
          <div v-if="overview.networks.length" class="table-scroll">
            <n-table size="small" :bordered="false">
              <thead><tr><th>网卡</th><th>接收速率</th><th>发送速率</th><th>累计接收</th><th>累计发送</th></tr></thead>
              <tbody><tr v-for="network in overview.networks" :key="network.index">
                <td>{{ network.name }}</td><td>{{ network.receiveRate }}</td><td>{{ network.sendRate }}</td>
                <td>{{ network.received }}</td><td>{{ network.sent }}</td>
              </tr></tbody>
            </n-table>
          </div>
          <n-empty v-else description="当前环境未提供可监控的网卡" />
        </n-card>
      </template>
    </template>
  </div>
</template>

<style scoped>
.server-monitor, .resources, .trends { display: grid; gap: 16px; }
.server-monitor { min-width: 0; grid-template-columns: minmax(0, 1fr); }
.note { margin: 0 0 14px; }
.resources { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.resources > div { display: grid; align-content: start; gap: 5px; overflow-wrap: anywhere; }
.resources strong { font-size: 24px; font-variant-numeric: tabular-nums; }
.trends { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.table-scroll { overflow-x: auto; }
.table-scroll table { min-width: 540px; }
.usage { min-width: 110px; white-space: nowrap; }
@media (max-width: 800px) { .trends { grid-template-columns: minmax(0, 1fr); } }
@media (max-width: 600px) { .resources { grid-template-columns: minmax(0, 1fr); } }
</style>
