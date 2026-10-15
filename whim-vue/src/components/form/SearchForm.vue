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
const { binding, formProps, gridProps, submitting, submit, reset, validate, restoreValidation } =
  useSearchFormView(emit)
defineExpose({ submit, reset, validate, restoreValidation })
</script>

<template>
  <n-form ref="formRef" v-bind="{ ...$attrs, ...formProps }" @submit.prevent="submit">
    <n-grid v-bind="gridProps">
      <slot />
      <n-form-item-gi>
        <slot name="action" :submit="submit" :reset="reset">
          <n-space>
            <n-button attr-type="submit" type="primary" :loading="submitting" :disabled="!binding">
              {{ binding?.searchText.value ?? '搜索' }}
            </n-button>
            <n-button attr-type="button" :disabled="!binding || submitting" @click="reset">
              {{ binding?.resetText.value ?? '重置' }}
            </n-button>
          </n-space>
        </slot>
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>
