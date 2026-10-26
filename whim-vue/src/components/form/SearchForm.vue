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
  panelCollapsed,
  togglePanelCollapsed,
  formProps,
  gridProps,
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
    ref="formContainer"
    class="search-form"
    :style="{ '--search-header-background': themeVars.tableHeaderColor }"
  >
    <n-card
      class="search-form__card"
      size="small"
      :segmented="{ content: !panelCollapsed }"
      :content-style="{ padding: panelCollapsed ? '0' : '16px' }"
    >
      <template #header>
        <span class="search-form__title">{{ title }}</span>
      </template>
      <template #header-extra>
        <n-button
          text
          :aria-label="panelCollapsed ? '展开筛选条件' : '收起筛选条件'"
          :aria-expanded="!panelCollapsed"
          :aria-controls="panelId"
          :render-icon="createIcon(panelCollapsed ? 'chevron-down' : 'chevron-up')"
          icon-placement="right"
          @click="togglePanelCollapsed"
        >
          {{ panelCollapsed ? '展开' : '收起' }}
        </n-button>
      </template>
      <n-form
        v-show="!panelCollapsed"
        :id="panelId"
        ref="formRef"
        v-bind="{ ...$attrs, ...formProps }"
        @submit.prevent="submit"
      >
        <n-grid v-bind="gridProps">
          <slot />
          <n-gi suffix :span="1" v-slot="{ overflow }">
            <n-form-item :show-label="false" class="search-form__actions">
              <n-flex align="center" justify="end" :size="8">
                <slot name="action" :submit="submit" :reset="reset">
                  <n-button
                    attr-type="submit"
                    type="primary"
                    :render-icon="createIcon('search')"
                    :loading="submitting"
                    :disabled="!binding"
                  >
                    {{ binding?.searchText.value ?? '搜索' }}
                  </n-button>
                  <n-button
                    attr-type="button"
                    :render-icon="createIcon('rotate-ccw')"
                    :disabled="!binding || submitting"
                    @click="reset"
                  >
                    {{ binding?.resetText.value ?? '重置' }}
                  </n-button>
                </slot>
                <n-button
                  v-if="overflow || !collapsed"
                  attr-type="button"
                  text
                  type="primary"
                  :aria-expanded="!collapsed"
                  :render-icon="createIcon(collapsed ? 'chevron-down' : 'chevron-up')"
                  icon-placement="right"
                  @click="toggleCollapsed"
                >
                  {{ collapsed ? '展开更多' : '收起条件' }}
                </n-button>
              </n-flex>
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
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

.search-form__card > :deep(.n-card-header) {
  padding: 12px 16px;
  background: var(--search-header-background);
}

.search-form__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.search-form__actions :deep(.n-form-item-blank) {
  justify-content: flex-end;
}
</style>
