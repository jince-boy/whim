<script setup lang="ts">
import { computed } from 'vue'
import { getIconComponent } from './catalog'

defineOptions({
  name: 'Icon',
  inheritAttrs: false,
})

interface Props {
  name?: string | null
  size?: number | string
  color?: string
  strokeWidth?: number
}

const props = withDefaults(defineProps<Props>(), {
  size: '1em',
  strokeWidth: 2,
})

const iconComponent = computed(() => getIconComponent(props.name))

const iconStyle = computed(() => ({
  fontSize: typeof props.size === 'number' ? `${props.size}px` : props.size,
  color: props.color,
}))
</script>

<template>
  <component
    :is="iconComponent"
    v-if="iconComponent"
    class="whim-icon"
    :stroke-width="props.strokeWidth"
    :style="iconStyle"
    aria-hidden="true"
    focusable="false"
    v-bind="$attrs"
  />
</template>

<style scoped>
.whim-icon {
  display: inline-block;
  flex-shrink: 0;
  width: 1em;
  height: 1em;
  line-height: 1;
  vertical-align: middle;
}
</style>
