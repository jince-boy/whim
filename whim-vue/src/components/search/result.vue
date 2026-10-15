<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import type { MenuSearchItem } from './hooks/types'

defineOptions({ name: 'MenuSearchResults' })

const props = defineProps<{
  results: readonly MenuSearchItem[]
  activeIndex: number
  listId: string
  navigating: boolean
}>()
const emit = defineEmits<{
  select: [item: MenuSearchItem]
  activeChange: [index: number]
}>()
</script>

<template>
  <n-el>
    <ul
      :id="props.listId"
      class="search-results"
      role="listbox"
      aria-label="菜单搜索结果"
      :aria-busy="props.navigating"
    >
      <li
        v-for="(item, index) in props.results"
        :id="`${props.listId}-${index}`"
        :key="item.key"
        class="search-option"
        :class="{ 'is-active': index === props.activeIndex }"
        role="option"
        :aria-selected="index === props.activeIndex"
        :aria-disabled="props.navigating"
        @pointermove="emit('activeChange', index)"
        @mousedown.prevent
        @click="emit('select', item)"
      >
        <span class="option-content">
          <span class="option-name">{{ item.name }}</span>
          <span v-if="item.breadcrumb" class="option-breadcrumb">{{ item.breadcrumb }}</span>
        </span>
        <Icon class="option-enter" name="c181huiche" :size="18" />
      </li>
    </ul>
  </n-el>
</template>

<style scoped lang="scss">
.search-results {
  display: flex;
  flex-direction: column;
  gap: 6px;
  list-style: none;
}

.search-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px;
  border-radius: var(--border-radius);
  color: var(--text-color-1);
  cursor: pointer;

  &.is-active {
    background-color: var(--hover-color);
    box-shadow: inset 0 0 0 1px var(--primary-color);

    .option-name,
    .option-enter {
      color: var(--primary-color);
    }
  }

  &[aria-disabled='true'] {
    cursor: progress;
  }
}

.option-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  overflow-wrap: anywhere;
}

.option-name {
  font-weight: 500;
}

.option-breadcrumb {
  color: var(--text-color-3);
  font-size: 12px;
}

.option-enter {
  color: var(--text-color-3);
}
</style>
