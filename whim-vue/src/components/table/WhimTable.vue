<script setup lang="ts">
import type { VNode } from 'vue'
import { SearchForm } from '../form'
import Icon from '@/components/icon/Icon.vue'
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
  tableComponentOptions,
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
  buttonOptions,
  isDisabled,
  handleAction,
  searchAction,
  applySearch,
  refreshing,
  finishRefreshRotation,
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
    }"
  >
    <n-card
      v-if="binding?.registerSearch"
      class="whim-table__search-card"
      size="medium"
      content-style="padding: 10px"
      :theme-overrides="{ borderRadius: '8px' }"
    >
      <SearchForm
        :loading="searchAction === 'submit'"
        :resetting="searchAction === 'reset'"
        @register="binding.registerSearch"
        @submit="applySearch('submit')"
        @reset="applySearch('reset')"
        @filter-change="applySearch('filter')"
      >
        <slot name="search" />
        <template v-if="$slots.searchAction" #action="actions">
          <slot name="searchAction" v-bind="actions" />
        </template>
      </SearchForm>
    </n-card>
    <n-card
      class="whim-table__card"
      size="medium"
      content-style="padding: 10px"
      :theme-overrides="{ borderRadius: '8px' }"
    >
      <n-flex
        v-if="buttons.length || $slots.toolbarBefore || $slots.toolbarExtra || (binding && showTools)"
        class="whim-table__toolbar"
        align="center"
        :size="8"
      >
        <n-flex
          v-if="buttons.length || $slots.toolbarBefore || $slots.toolbarExtra"
          align="center"
          :size="8"
        >
          <slot name="toolbarBefore" />
          <n-button
            v-for="button in buttons"
            :key="button"
            class="whim-button--with-icon"
            :loading="toolbar.loading?.[button]"
            :aria-busy="!!toolbar.loading?.[button]"
            :type="buttonOptions[button].type"
            :secondary="button !== 'add'"
            :render-icon="createIcon(buttonOptions[button].icon)"
            :disabled="isDisabled(button)"
            @click="handleAction(button)"
          >
            {{ buttonOptions[button].label }}
          </n-button>
          <slot name="toolbarExtra" />
        </n-flex>
        <n-flex
          v-if="binding && showTools"
          class="whim-table__tools"
          align="center"
          :size="4"
          aria-label="表格工具"
        >
          <n-tooltip v-if="toolbar.showRefresh !== false" :to="tableContainer ?? undefined">
            <template #trigger>
              <n-button
                quaternary
                native-focus-behavior
                size="small"
                aria-label="刷新表格"
                :aria-busy="refreshing || binding.loading.value"
                :disabled="refreshing || binding.loading.value"
                @mousedown.prevent
                @click="refresh"
              >
                <template #icon>
                  <Icon
                    name="refresh-cw"
                    :class="{ 'refresh-icon--spinning': refreshing }"
                    @animationiteration="finishRefreshRotation"
                  />
                </template>
              </n-button>
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
                quaternary
                native-focus-behavior
                size="small"
                aria-label="表格设置"
                :aria-expanded="settingsVisible"
                :render-icon="createIcon('sliders-horizontal')"
                @mousedown.prevent
              />
            </template>
            <template #header>表格设置</template>
            <div class="whim-table__settings">
              <div v-if="toolbar.showStriped !== false" class="whim-table__setting-row">
                <n-text>斑马线</n-text>
                <n-switch
                  v-model:value="binding.striped.value"
                  size="small"
                  aria-label="显示斑马线"
                />
              </div>
              <div v-if="toolbar.showColumns !== false" class="whim-table__columns">
                <n-text depth="3">显示列</n-text>
                <n-checkbox-group
                  size="small"
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
                quaternary
                native-focus-behavior
                size="small"
                :disabled="!fullscreenEnabled"
                :aria-label="isFullscreen ? '退出表格全屏' : '表格全屏'"
                :render-icon="createIcon(isFullscreen ? 'minimize' : 'maximize')"
                @mousedown.prevent
                @click="toggleFullscreen"
              />
            </template>
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </n-tooltip>
        </n-flex>
      </n-flex>
      <n-config-provider abstract :component-options="tableComponentOptions">
        <n-space vertical :size="12">
          <n-alert v-if="binding?.error.value" type="error">{{ binding.error.value }}</n-alert>
          <n-data-table
            ref="tableRef"
            v-bind="{ tableLayout: 'fixed', 'aria-label': title, ...$attrs, ...tableProps }"
          >
            <template v-if="$slots.default" #default><slot /></template>
            <template v-if="$slots.empty" #empty><slot name="empty" /></template>
            <template v-if="$slots.loading" #loading><slot name="loading" /></template>
          </n-data-table>
        </n-space>
      </n-config-provider>
    </n-card>
  </div>
</template>

<style scoped>
.whim-table {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.whim-table__toolbar {
  min-width: 0;
  margin-bottom: 10px;
}

.whim-table__tools {
  flex-shrink: 0;
  margin-left: auto;
}

.refresh-icon--spinning {
  animation: refresh-rotate 0.6s linear infinite;
}

@keyframes refresh-rotate {
  to {
    transform: rotate(360deg);
  }
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

.whim-table:fullscreen {
  box-sizing: border-box;
  padding: 10px;
  overflow: auto;
  background: var(--table-background);
}
</style>
