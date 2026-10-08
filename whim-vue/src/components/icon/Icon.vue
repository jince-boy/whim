<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'Icon',
})

interface Props {
  name: string | null // 唯一必需的图标名称属性
  size?: number
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 16,
  color: undefined,
})

// 动态生成 iconfont 类名和样式
const iconClass = computed(() => {
  return props.name ? `iconfont icon-${props.name}` : ''
})

const iconStyle = computed(() => ({
  fontSize: `${props.size}px`,
  color: props.color,
}))
</script>

<template>
  <!-- 如果 name 为空或 null，就不渲染 -->
  <i v-if="props.name" :class="iconClass" :style="iconStyle" />
</template>

<style scoped></style>
