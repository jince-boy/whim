<script setup lang="ts">
import { computed } from 'vue'
import { useThemeVars, type GlobalThemeOverrides } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'
import { useUserStore } from '@/stores/modules/user'
import ConfigTable from './components/ConfigTable.vue'

defineOptions({ name: 'SysConfig' })
const user = useUserStore()
const theme = useThemeVars()
const tabThemeOverrides = computed<GlobalThemeOverrides['Tabs']>(() => ({
  tabBorderRadius: '6px',
  tabTextColorActiveSegment: theme.value.primaryColor,
  tabTextColorHoverSegment: theme.value.primaryColor,
}))
</script>

<template>
  <div>
    <n-tabs v-if="user.hasPermission('system:config:page')" type="segment" size="small" animated
      default-value="system" class="config-tabs" :theme-overrides="tabThemeOverrides">
      <n-tab-pane name="system" display-directive="show:lazy">
        <template #tab><span class="tab-label"><Icon name="settings" />系统配置</span></template>
        <ConfigTable :built-in="true" />
      </n-tab-pane>
      <n-tab-pane name="custom" display-directive="show:lazy">
        <template #tab><span class="tab-label"><Icon name="sliders-horizontal" />自定义配置</span></template>
        <ConfigTable :built-in="false" />
      </n-tab-pane>
    </n-tabs>
    <n-card v-else><n-empty description="暂无配置查询权限" /></n-card>
  </div>
</template>

<style scoped>
.config-tabs :deep(> .n-tabs-nav) {
  padding: 8px;
  background: v-bind('theme.cardColor');
  border: 1px solid v-bind('theme.dividerColor');
  border-radius: 8px;
}
.config-tabs :deep(> .n-tabs-nav > .n-tabs-rail) {
  width: 288px;
  max-width: 100%;
  box-sizing: border-box;
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
</style>
