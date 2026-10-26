<script setup lang="ts">
import type { VNode } from 'vue'
import { SearchForm } from '../form'
import { useIcon } from '@/components/icon/useIcon'
import type { WhimTableEmits } from './types'
import { useWhimTableView } from './hooks/useWhimTableView'

defineOptions({ name: 'WhimTable', inheritAttrs: false })
const emit = defineEmits<WhimTableEmits>()
defineSlots<{
  search(): VNode[]
  searchAction(props: { submit: () => Promise<boolean>; reset: () => void }): VNode[]
  toolbarBefore(): VNode[]
  toolbarExtra(): VNode[]
  default(): VNode[]
  empty(): VNode[]
  loading(): VNode[]
}>()
const { createIcon } = useIcon()
const {
  tableContainer,
  themeVars,
  fullscreenEnabled,
  isFullscreen,
  toggleFullscreen,
  binding,
  title,
  tableProps,
  toolbar,
  showSettings,
  showTools,
  settingsVisible,
  buttons,
  buttonLabels,
  buttonTypes,
  buttonIcons,
  isDisabled,
  handleAction,
  refresh,
  reload,
  getTableInstance,
} = useWhimTableView(emit)
defineExpose({ reload, getTableInstance })
</script>

<template>
  <div
    ref="tableContainer"
    class="whim-table"
    :style="{
      '--table-background': themeVars.bodyColor,
      '--table-header-background': themeVars.tableHeaderColor,
      '--table-border-color': themeVars.borderColor,
    }"
  >
    <SearchForm
      v-if="binding?.registerSearch"
      @register="binding.registerSearch"
      @submit="binding.applySearch"
      @reset="binding.applySearch"
      @filter-change="binding.applySearch"
    >
      <slot name="search" />
      <template v-if="$slots.searchAction" #action="actions">
        <slot name="searchAction" v-bind="actions" />
      </template>
    </SearchForm>
    <n-card class="whim-table__card" size="small" :segmented="{ content: true }">
      <template #header>
        <span class="whim-table__title">{{ title }}</span>
      </template>
      <template #header-extra>
        <div class="whim-table__toolbar">
          <n-flex
            v-if="buttons.length || $slots.toolbarBefore || $slots.toolbarExtra"
            class="whim-table__actions"
            align="center"
            justify="end"
            :size="8"
          >
            <slot name="toolbarBefore" />
            <n-button
              v-for="button in buttons"
              :key="button"
              :type="buttonTypes[button]"
              :secondary="button !== 'add'"
              :render-icon="createIcon(buttonIcons[button])"
              :disabled="isDisabled(button)"
              @click="handleAction(button)"
            >
              {{ buttonLabels[button] }}
            </n-button>
            <slot name="toolbarExtra" />
          </n-flex>
          <div v-if="binding && showTools" class="whim-table__tools">
            <n-button-group aria-label="表格工具">
              <n-tooltip v-if="toolbar.showRefresh !== false" :to="tableContainer ?? undefined">
                <template #trigger>
                  <n-button
                    class="whim-table__tool"
                    aria-label="刷新表格"
                    :loading="binding.loading.value"
                    :render-icon="createIcon('refresh-cw')"
                    @click="refresh"
                  />
                </template>
                刷新
              </n-tooltip>
              <n-popover
                v-if="showSettings"
                v-model:show="settingsVisible"
                trigger="click"
                placement="bottom-end"
                :to="tableContainer ?? undefined"
              >
                <template #trigger>
                  <n-button
                    class="whim-table__tool"
                    aria-label="表格设置"
                    :aria-expanded="settingsVisible"
                    :render-icon="createIcon('sliders-horizontal')"
                  />
                </template>
                <template #header>表格设置</template>
                <div class="whim-table__settings">
                  <div v-if="toolbar.showStriped !== false" class="whim-table__setting-row">
                    <n-text>斑马纹</n-text>
                    <n-switch
                      v-model:value="binding.striped.value"
                      size="small"
                      aria-label="显示斑马纹"
                    />
                  </div>
                  <div v-if="toolbar.showColumns !== false" class="whim-table__columns">
                    <n-text depth="3">显示列</n-text>
                    <n-checkbox-group
                      :value="binding.visibleColumnKeys.value"
                      @update:value="binding.setVisibleColumns"
                    >
                      <n-space vertical :size="10">
                        <n-checkbox
                          v-for="column in binding.columnOptions.value"
                          :key="column.key"
                          :value="column.key"
                          :label="column.label"
                        />
                      </n-space>
                    </n-checkbox-group>
                  </div>
                </div>
              </n-popover>
              <n-tooltip v-if="toolbar.showFullscreen !== false" :to="tableContainer ?? undefined">
                <template #trigger>
                  <n-button
                    class="whim-table__tool"
                    :disabled="!fullscreenEnabled"
                    :aria-label="isFullscreen ? '退出表格全屏' : '表格全屏'"
                    :render-icon="createIcon(isFullscreen ? 'minimize' : 'maximize')"
                    @click="toggleFullscreen"
                  />
                </template>
                {{ isFullscreen ? '退出全屏' : '全屏' }}
              </n-tooltip>
            </n-button-group>
          </div>
        </div>
      </template>
      <n-space vertical :size="12">
        <n-alert v-if="binding?.error.value" type="error">{{ binding.error.value }}</n-alert>
        <n-data-table ref="tableRef" v-bind="{ ...$attrs, ...tableProps }">
          <template v-if="$slots.default" #default><slot /></template>
          <template v-if="$slots.empty" #empty><slot name="empty" /></template>
          <template v-if="$slots.loading" #loading><slot name="loading" /></template>
        </n-data-table>
      </n-space>
    </n-card>
  </div>
</template>

<style scoped>
.whim-table {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.whim-table__card {
  border-radius: 8px;
  overflow: hidden;
}

.whim-table__card > :deep(.n-card-header) {
  gap: 12px;
  flex-wrap: wrap;
  padding: 12px 16px;
  background: var(--table-header-background);
}

.whim-table__card > :deep(.n-card-header .n-card-header__extra) {
  flex-shrink: 0;
  min-width: 0;
  max-width: 100%;
  margin-left: auto;
}

.whim-table__card > :deep(.n-card-header .n-card-header__main) {
  flex: 1 0 auto;
  max-width: 100%;
}

.whim-table__card > :deep(.n-card__content) {
  padding: 16px;
}

.whim-table__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.whim-table__toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.whim-table__tools {
  flex-shrink: 0;
}

.whim-table__actions + .whim-table__tools {
  padding-left: 12px;
  border-left: 1px solid var(--table-border-color);
}

.whim-table__tool {
  width: 34px;
  padding: 0;
}

.whim-table__settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 200px;
}

.whim-table__setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.whim-table__columns {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 280px;
  overflow-y: auto;
}

/* 保留透明边框作为过渡起点，避免从文字颜色闪变，也保持切换前后的尺寸。 */
.whim-table :deep(.n-data-table:not(.n-data-table--bordered) .n-data-table-wrapper) {
  border: 1px solid transparent;
  border-radius: var(--n-border-radius);
}

.whim-table :deep(.n-data-table--single-line .n-data-table-th:not(.n-data-table-th--last)),
.whim-table :deep(.n-data-table--single-line .n-data-table-td:not(.n-data-table-td--last-col)) {
  border-right: 1px solid transparent;
}

.whim-table:fullscreen {
  box-sizing: border-box;
  padding: 16px;
  overflow: auto;
  background: var(--table-background);
}
</style>
