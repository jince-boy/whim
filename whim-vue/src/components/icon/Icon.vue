<script setup lang="ts">
import '@/assets/iconfont/iconfont.css'
import { computed } from 'vue'

defineOptions({
  name: 'Icon',
})

interface Props {
  name?: string | null
  size?: number | string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: '1em',
})

const iconName = computed(() => props.name?.trim().replace(/^icon-/, ''))

const iconStyle = computed(() => ({
  fontSize: typeof props.size === 'number' ? `${props.size}px` : props.size,
  color: props.color,
}))
</script>

<template>
  <i
    v-if="iconName"
    class="whim-icon iconfont"
    :class="`icon-${iconName}`"
    :style="iconStyle"
    aria-hidden="true"
  />
</template>

<style scoped>
.whim-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 1em;
  height: 1em;
  line-height: 1;
}
</style>
