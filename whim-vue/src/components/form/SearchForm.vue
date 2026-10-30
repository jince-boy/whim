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
  panelId,
  title,
  formProps,
  gridProps,
  filters,
  removeFilter,
  collapsed,
  canCollapse,
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
  <n-config-provider abstract :component-options="{ Button: { size: formProps?.size ?? 'medium' } }">
    <div class="search-form">
      <n-form
        :id="panelId"
        ref="formRef"
        :aria-label="title"
        v-bind="{ ...$attrs, ...formProps }"
        @submit.prevent="submit"
      >
        <n-grid ref="gridRef" v-bind="gridProps">
          <slot />
          <n-gi v-slot="{ overflow }" suffix :span="1" :style="{ gridColumn: 'auto / span 1' }">
            <n-flex align="center" :size="8" :wrap="false">
              <slot name="action" :submit="submit" :reset="reset">
                <n-button
                  attr-type="submit"
                  type="primary"
                  :loading="submitting"
                  :disabled="!binding"
                  :render-icon="createIcon('search')"
                >
                  {{ binding?.searchText.value ?? '查询' }}
                </n-button>
                <n-button
                  attr-type="button"
                  :disabled="!binding || submitting"
                  :render-icon="createIcon('rotate-ccw')"
                  @click="reset"
                >
                  {{ binding?.resetText.value ?? '重置' }}
                </n-button>
              </slot>
              <n-button
                v-if="overflow || (!collapsed && canCollapse)"
                attr-type="button"
                text
                :aria-expanded="!collapsed"
                :aria-controls="panelId"
                :aria-label="collapsed ? '展开更多筛选条件' : '收起更多筛选条件'"
                :render-icon="createIcon(collapsed ? 'chevron-down' : 'chevron-up')"
                icon-placement="right"
                @click="toggleCollapsed"
              >
                {{ collapsed ? '展开' : '收起' }}
              </n-button>
            </n-flex>
          </n-gi>
        </n-grid>
      </n-form>
      <n-flex v-if="filters.length" class="search-form__summary" align="center" :size="8" aria-label="已应用的筛选条件">
        <n-text depth="3">已选</n-text>
        <n-tag
          v-for="filter in filters"
          :key="filter.key"
          size="small"
          type="primary"
          :bordered="false"
          closable
          :disabled="submitting"
          @close="removeFilter(filter.key)"
        >
          <n-ellipsis :style="{ maxWidth: 'min(280px, 60vw)' }">
            {{ filter.label }}：{{ filter.value }}
          </n-ellipsis>
        </n-tag>
        <n-button size="small" text :disabled="submitting" @click="reset">清空</n-button>
      </n-flex>
      <span class="search-form__status" role="status">已应用 {{ filters.length }} 项筛选条件</span>
    </div>
  </n-config-provider>
</template>

<style scoped>
.search-form {
  min-width: 0;
}

.search-form :deep(.n-date-picker) {
  width: 100%;
}

.search-form__summary {
  margin-top: 16px;
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
