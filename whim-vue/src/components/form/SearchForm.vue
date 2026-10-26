<script setup lang="ts">
import type { VNode } from 'vue'
import { createIcon } from '@/components/icon/useIcon'
import type { SearchFormEmits } from './types'
import { useSearchFormView } from './hooks/useSearchFormView'

defineOptions({ name: 'SearchForm', inheritAttrs: false })
const emit = defineEmits<SearchFormEmits>()
defineSlots<{
  default(): VNode[]
  action(props: { submit: () => Promise<boolean>; reset: () => void }): VNode[]
}>()
const {
  binding,
  themeVars,
  panelId,
  title,
  formProps,
  gridProps,
  gridStyle,
  filters,
  removeFilter,
  collapsed,
  toggleCollapsed,
  submitting,
  submit,
  reset,
  validate,
  restoreValidation,
} = useSearchFormView(emit)
defineExpose({ submit, reset, validate, restoreValidation })
</script>

<template>
  <div
    class="search-form"
    :style="{
      '--search-border-color': themeVars.borderColor,
      '--search-border-radius': themeVars.borderRadius,
      '--search-label-color': themeVars.textColor3,
      '--search-focus-color': themeVars.primaryColor,
      '--search-error-color': themeVars.errorColor,
      '--search-warning-color': themeVars.warningColor,
    }"
  >
    <n-card class="search-form__card" size="small" :content-style="{ padding: '14px 16px' }">
      <n-form
        :id="panelId"
        ref="formRef"
        :aria-label="title"
        v-bind="{ ...$attrs, ...formProps }"
        @submit.prevent="submit"
      >
        <div ref="formLayout">
          <n-grid class="search-form__grid" v-bind="gridProps" :style="gridStyle">
            <slot />
            <n-gi suffix :span="1" style="grid-column: auto / span 1; align-self: start" v-slot="{ overflow }">
              <n-form-item :show-label="false" :show-feedback="false" class="search-form__actions">
                <n-flex align="center" :size="10">
                  <slot name="action" :submit="submit" :reset="reset">
                    <n-button
                      attr-type="submit"
                      type="primary"
                      :loading="submitting"
                      :disabled="!binding"
                      :render-icon="createIcon('search')"
                    >
                      {{ binding?.searchText.value ?? '搜索' }}
                    </n-button>
                    <n-button
                      attr-type="button"
                      text
                      :disabled="!binding || submitting"
                      :render-icon="createIcon('rotate-ccw')"
                      @click="reset"
                    >
                      {{ binding?.resetText.value ?? '重置' }}
                    </n-button>
                  </slot>
                  <n-button
                    v-if="overflow || (!collapsed && binding?.collapsed.value)"
                    attr-type="button"
                    text
                    type="primary"
                    :aria-expanded="!collapsed"
                    :aria-controls="panelId"
                    :aria-label="collapsed ? '展开更多筛选条件' : '收起更多筛选条件'"
                    :render-icon="createIcon(collapsed ? 'chevron-down' : 'chevron-up')"
                    icon-placement="right"
                    @click="toggleCollapsed"
                  >
                    {{ collapsed ? '更多' : '收起' }}
                  </n-button>
                </n-flex>
              </n-form-item>
            </n-gi>
          </n-grid>
        </div>
      </n-form>
      <div v-if="filters.length" class="search-form__summary" aria-label="已应用的筛选条件">
        <span class="search-form__summary-label">已选</span>
        <n-tag
          v-for="filter in filters"
          :key="filter.key"
          size="small"
          type="primary"
          :bordered="false"
          closable
          :disabled="submitting"
          class="search-form__tag"
          @close="removeFilter(filter.key)"
        >
          <span class="search-form__tag-text">{{ filter.label }}：{{ filter.value }}</span>
        </n-tag>
        <n-button text :disabled="submitting" @click="reset">
          清空
        </n-button>
      </div>
      <span class="search-form__status" role="status">已应用 {{ filters.length }} 项筛选条件</span>
    </n-card>
  </div>
</template>

<style scoped>
.search-form {
  min-width: 0;
}

.search-form__card {
  border-radius: 8px;
  overflow: hidden;
}

.search-form :deep(.n-form-item-label) {
  color: var(--search-label-color);
  font-size: 12px;
}

.search-form :deep(.n-form-item-blank),
.search-form :deep(.n-date-picker) {
  min-width: 0;
}

.search-form :deep(.n-date-picker) {
  width: 100%;
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions)) {
  position: relative;
  isolation: isolate;
  grid-template-areas: "label blank" "feedback feedback";
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions)::before) {
  content: '';
  grid-area: 1 / 1 / 2 / -1;
  align-self: stretch;
  /* 边框绘制在原生控件背景上方，避免右侧圆角被背景遮住。 */
  z-index: 1;
  border: 1px solid var(--search-border-color);
  border-radius: var(--search-border-radius);
  pointer-events: none;
  transition: border-color 0.2s;
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions):focus-within::before) {
  border-color: var(--search-focus-color);
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions):has(.n-input--warning, .n-base-selection--warning)::before) {
  border-color: var(--search-warning-color);
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions):has(.n-input--error, .n-base-selection--error)::before) {
  border-color: var(--search-error-color);
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions) > .n-form-item-label) {
  position: relative;
  align-self: start;
  min-height: 36px;
  padding: 0 12px;
  white-space: nowrap;
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions) > .n-form-item-label::after) {
  content: '';
  position: absolute;
  right: 0;
  top: 10px;
  bottom: 10px;
  width: 1px;
  background: var(--search-border-color);
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions) > .n-form-item-blank) {
  min-height: 36px;
  padding-right: 1px;
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions) :is(.n-input, .n-base-selection-label, .n-base-selection-tags)) {
  font-size: 13px;
}

.search-form__grid :deep(.n-form-item--left-labelled:not(.search-form__actions) :is(.n-input__border, .n-input__state-border, .n-base-selection__border, .n-base-selection__state-border)) {
  display: none;
}

.search-form__actions :deep(.n-form-item-blank) {
  min-height: 36px;
  justify-content: flex-start;
}

.search-form__summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--search-border-color);
}

.search-form__summary-label {
  color: var(--search-label-color);
  font-size: 12px;
}

.search-form__tag {
  max-width: 100%;
  height: auto;
  min-height: 24px;
}

.search-form__tag :deep(.n-tag__content) {
  min-width: 0;
}

.search-form__tag-text {
  padding: 2px 0;
  white-space: normal;
  overflow-wrap: anywhere;
}

.search-form__status {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
}
</style>
