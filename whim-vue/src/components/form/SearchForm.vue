<script setup lang="ts">
import type { VNode } from 'vue'
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
  <div ref="formContainer">
    <n-form ref="formRef" v-bind="{ ...$attrs, ...formProps }" @submit.prevent="submit">
      <n-grid v-bind="gridProps">
        <slot />
        <!-- suffix 为折叠预留一格，自动排布让按钮紧跟查询项居左显示。 -->
        <n-gi suffix :span="1" style="grid-column: span 1 / span 1" v-slot="{ overflow }">
          <n-form-item :show-label="false">
            <n-space align="center">
              <slot name="action" :submit="submit" :reset="reset">
                <n-button
                  attr-type="submit"
                  type="primary"
                  :loading="submitting"
                  :disabled="!binding"
                >
                  {{ binding?.searchText.value ?? '搜索' }}
                </n-button>
                <n-button attr-type="button" :disabled="!binding || submitting" @click="reset">
                  {{ binding?.resetText.value ?? '重置' }}
                </n-button>
              </slot>
              <n-button
                v-if="overflow || !collapsed"
                attr-type="button"
                text
                type="primary"
                :aria-expanded="!collapsed"
                @click="toggleCollapsed"
              >
                {{ collapsed ? '展开' : '收起' }}
              </n-button>
            </n-space>
          </n-form-item>
        </n-gi>
      </n-grid>
    </n-form>
  </div>
</template>
