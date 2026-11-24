<script setup lang="ts">
import { ref } from 'vue'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { pageTabThemeOverrides } from '@/config/NaiveTheme'
import { useUserStore } from '@/stores/modules/user'
import ConnectionPools from './components/ConnectionPools.vue'
import SlowSql from './components/SlowSql.vue'

defineOptions({ name: 'MonitorDatabase' })
const user = useUserStore()
const tab = ref<'slowSql' | 'connectionPool'>('slowSql')
</script>

<template>
  <div>
    <template v-if="user.hasPermission('monitor:database:query')">
      <n-tabs v-model:value="tab" type="segment" size="small" class="whim-page-tabs"
        :theme-overrides="pageTabThemeOverrides" aria-label="数据库监控栏目">
        <n-tab name="slowSql"><AppIcon name="timer" />慢 SQL</n-tab>
        <n-tab name="connectionPool"><AppIcon name="database" />连接池</n-tab>
      </n-tabs>
      <KeepAlive>
        <SlowSql v-if="tab === 'slowSql'" />
        <ConnectionPools v-else />
      </KeepAlive>
    </template>
    <n-card v-else><n-empty description="暂无数据库监控查询权限" /></n-card>
  </div>
</template>

<style scoped>
.whim-page-tabs { margin-bottom: 12px; }
</style>
