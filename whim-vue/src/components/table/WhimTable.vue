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
  tableProps,
  toolbar,
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
    :style="{ '--table-background': themeVars.bodyColor }"
  >
    <n-space vertical>
      <n-card v-if="binding?.registerSearch" :bordered="false" padding="12px">
        <SearchForm
          @register="binding.registerSearch"
          @submit="binding.search"
          @reset="binding.resetSearch"
        >
          <slot name="search" />
          <template v-if="$slots.searchAction" #action="actions">
            <slot name="searchAction" v-bind="actions" />
          </template>
        </SearchForm>
      </n-card>
      <n-card :bordered="false">
        <n-space vertical>
          <n-flex justify="space-between" align="center">
            <n-space>
              <slot name="toolbarBefore" />
              <n-button
                v-for="button in buttons"
                :key="button"
                :type="buttonTypes[button]"
                :render-icon="createIcon(buttonIcons[button])"
                :disabled="isDisabled(button)"
                @click="handleAction(button)"
              >
                {{ buttonLabels[button] }}
              </n-button>
              <slot name="toolbarExtra" />
            </n-space>
            <n-space v-if="binding" align="center">
              <n-flex v-if="toolbar.showStriped !== false" align="center">
                <n-text>斑马线</n-text>
                <n-switch v-model:value="binding.striped.value" size="small" />
              </n-flex>
              <n-tooltip v-if="toolbar.showRefresh !== false" :to="tableContainer ?? undefined">
                <template #trigger>
                  <n-button
                    quaternary
                    circle
                    aria-label="刷新表格"
                    :loading="binding.loading.value"
                    :render-icon="createIcon('refresh-2')"
                    @click="refresh"
                  />
                </template>
                刷新
              </n-tooltip>
              <n-popover
                v-if="toolbar.showColumns !== false"
                trigger="click"
                placement="bottom-end"
                :to="tableContainer ?? undefined"
              >
                <template #trigger>
                  <n-button
                    quaternary
                    circle
                    aria-label="列设置"
                    :render-icon="createIcon('setting')"
                  />
                </template>
                <template #header>显示/隐藏列</template>
                <n-checkbox-group
                  :value="binding.visibleColumnKeys.value"
                  @update:value="binding.setVisibleColumns"
                >
                  <n-space vertical>
                    <n-checkbox
                      v-for="column in binding.columnOptions.value"
                      :key="column.key"
                      :value="column.key"
                      :label="column.label"
                    />
                  </n-space>
                </n-checkbox-group>
              </n-popover>
              <n-tooltip v-if="toolbar.showFullscreen !== false" :to="tableContainer ?? undefined">
                <template #trigger>
                  <n-button
                    quaternary
                    circle
                    :disabled="!fullscreenEnabled"
                    :aria-label="isFullscreen ? '退出表格全屏' : '表格全屏'"
                    :render-icon="createIcon(isFullscreen ? 'quanpingsuoxiao' : 'full-screen')"
                    @click="toggleFullscreen"
                  />
                </template>
                {{ isFullscreen ? '退出全屏' : '全屏' }}
              </n-tooltip>
            </n-space>
          </n-flex>
          <n-alert v-if="binding?.error.value" type="error">{{ binding.error.value }}</n-alert>
          <n-data-table ref="tableRef" v-bind="{ ...$attrs, ...tableProps }">
            <template v-if="$slots.default" #default><slot /></template>
            <template v-if="$slots.empty" #empty><slot name="empty" /></template>
            <template v-if="$slots.loading" #loading><slot name="loading" /></template>
          </n-data-table>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<style scoped>
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
