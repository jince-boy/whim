<script setup lang="ts">
import { computed, useTemplateRef } from 'vue'
import { NButton, NTooltip } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'
import type { TableActionProps } from './types'

defineOptions({ name: 'TableAction', inheritAttrs: false })
const props = defineProps<TableActionProps>()
const emit = defineEmits<{ click: [event: MouseEvent] }>()
const trigger = useTemplateRef<HTMLElement>('trigger')
// 提示挂在所属表格中，全屏时仍能显示，同时避开单元格的溢出裁剪。
const tooltipTarget = computed(
  () => trigger.value?.closest<HTMLElement>('.whim-table') ?? undefined,
)
</script>

<template>
  <NTooltip :to="tooltipTarget" :delay="250">
    <template #trigger>
      <span ref="trigger" class="whim-table-action">
        <NButton
          v-bind="$attrs"
          class="whim-table-action__button whim-button--with-icon"
          size="small"
          tertiary
          :type="props.type"
          :loading="props.loading"
          :disabled="props.disabled"
          :aria-label="props.label"
          :aria-busy="!!props.loading"
          @click="emit('click', $event)"
        >
          <template #icon><Icon :name="props.icon" :size="16" :stroke-width="1.8" /></template>
        </NButton>
      </span>
    </template>
    {{ props.tooltip ?? props.label }}
  </NTooltip>
</template>

<style scoped>
.whim-table-action {
  display: inline-flex;
  vertical-align: middle;
}

/* 宽度跟随主题按钮高度形成正方形，背景和圆角由 Naive UI 维护。 */
.whim-table-action__button {
  width: var(--n-height);
  padding: 0;
}
</style>
