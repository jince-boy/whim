<script setup lang="ts">
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { useIcon } from '@/components/appIcon/hooks/useIcon'
import { DataTable } from '@/components/dataTable'
import { pageTabThemeOverrides } from '@/config/NaiveTheme'
import { useConfig } from './hooks/useConfig'

defineOptions({ name: 'SysConfig' })
const { createIcon } = useIcon()
const { tabs, searchFields, canViewConfigs, canRefreshCache, refreshingCache, handleRefreshCache } = useConfig()
</script>

<template>
  <div>
    <n-tabs v-if="canViewConfigs" type="segment" size="small" animated
      default-value="system" class="whim-page-tabs" :theme-overrides="pageTabThemeOverrides">
      <template v-if="canRefreshCache" #suffix>
        <n-button
          class="whim-button--with-icon"
          secondary
          :loading="refreshingCache"
          :render-icon="createIcon('database')"
          @click="handleRefreshCache"
        >刷新缓存</n-button>
      </template>
      <n-tab-pane v-for="tab in tabs" :key="tab.name" :name="tab.name" display-directive="show:lazy">
        <template #tab><AppIcon :name="tab.icon" />{{ tab.title }}</template>
        <DataTable ref="configTables" @register="tab.registerTable" @add="tab.openAddDialog">
          <template #search>
            <n-form-item-gi v-for="field in searchFields" :key="field.key" :label="field.label" :path="field.key">
              <n-input v-model:value="tab.form.value[field.key]" :placeholder="'请输入' + field.label" :maxlength="field.maxLength" clearable />
            </n-form-item-gi>
          </template>
          <template v-if="tab.builtIn" #toolbarBefore>
            <n-flex align="center" :size="6"><AppIcon name="lock-keyhole" /><n-text depth="3">系统内置配置，仅可修改配置值</n-text></n-flex>
          </template>
          <template #empty><n-empty description="暂无符合条件的配置" /></template>
        </DataTable>
      </n-tab-pane>
    </n-tabs>
    <n-card v-else><n-empty description="暂无配置查询权限" /></n-card>
  </div>
</template>

<style scoped>
:deep(.n-tabs-nav) {
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.n-tabs-nav__suffix) {
  margin-inline-start: auto;
}
</style>
