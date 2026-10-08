<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

interface ResultItem {
  text: string
  path?: string
}

const props = defineProps<{
  results: ResultItem[]
}>()
const emit = defineEmits<{
  close: []
}>()
const router = useRouter()
const activeIndex = ref(0)

// 处理点击事件
const handleClick = (path?: string) => {
  if (path) {
    router.push(path)
    emit('close')
  }
}

// 处理键盘事件
const handleKeyDown = (e: KeyboardEvent) => {
  switch (e.key) {
    case 'ArrowUp':
      e.preventDefault()
      activeIndex.value = Math.max(0, activeIndex.value - 1)
      break
    case 'ArrowDown':
      e.preventDefault()
      activeIndex.value = Math.min(props.results.length - 1, activeIndex.value + 1)
      break
    case 'Enter':
      e.preventDefault()
      if (props.results[activeIndex.value]?.path) {
        handleClick(props.results[activeIndex.value].path)
      }
      break
  }
}

// 添加和移除键盘事件监听
onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<template>
  <n-el>
    <n-list :show-divider="false">
      <n-list-item
        v-for="(item, index) in props.results"
        :key="item.path || item.text"
        :class="{ 'active-item': index === activeIndex }"
        :bordered="false"
        @click="handleClick(item.path)"
      >
        <n-flex justify="space-between" align="center">
          <span>{{ item.text }}</span>
          <i class="iconfont icon-c181huiche"></i>
        </n-flex>
      </n-list-item>
    </n-list>
  </n-el>
</template>

<style scoped lang="scss">
.n-list-item {
  cursor: pointer;
  padding: 8px 12px;
  transition:
    color 0.3s var(--n-bezier),
    background-color 0.3s var(--n-bezier),
    box-shadow 0.3s var(--n-bezier),
    border-color 0.3s var(--n-bezier);
  border-radius: var(--border-radius);
  margin: 12px 0;
  background-color: var(--n-color-hover);
  &:hover {
    background-color: var(--primary-color);
    color: var(--n-color);
  }
  &.active-item {
    background-color: var(--primary-color);
    color: var(--n-color);
  }
}
</style>
