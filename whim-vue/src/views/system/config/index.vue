<script setup lang="ts">
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { pageTabThemeOverrides } from '@/config/NaiveTheme'
import { useUserStore } from '@/stores/modules/user'
import ConfigTable from './components/ConfigTable.vue'

defineOptions({ name: 'SysConfig' })
const user = useUserStore()
</script>

<template>
  <div>
    <n-tabs v-if="user.hasPermission('system:config:page')" type="segment" size="small" animated
      default-value="system" class="whim-page-tabs" :theme-overrides="pageTabThemeOverrides">
      <n-tab-pane name="system" display-directive="show:lazy">
        <template #tab><AppIcon name="settings" />系统配置</template>
        <ConfigTable :built-in="true" />
      </n-tab-pane>
      <n-tab-pane name="custom" display-directive="show:lazy">
        <template #tab><AppIcon name="sliders-horizontal" />自定义配置</template>
        <ConfigTable :built-in="false" />
      </n-tab-pane>
    </n-tabs>
    <n-card v-else><n-empty description="暂无配置查询权限" /></n-card>
  </div>
</template>
