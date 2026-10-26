<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import SearchResults from './result.vue'
import {useMenuSearch} from './hooks/useMenuSearch'

defineOptions({name: 'SearchComponent'})

const show = defineModel<boolean>({required: true})
const {
  inputRef,
  resultsContainer,
  listId,
  statusId,
  value,
  keyword,
  searchResults,
  activeIndex,
  navigating,
  statusText,
  inputProps,
  focusInput,
  setActiveIndex,
  selectResult,
  handleKeyDown,
} = useMenuSearch(show)
</script>

<template>
  <n-modal v-model:show="show" transform-origin="center" @after-enter="focusInput">
    <n-card class="menu-search" :bordered="false" content-style="padding: 0; display: flex; flex-direction: column; min-height: 0; overflow: hidden" role="dialog" aria-modal="true" aria-label="菜单搜索">
      <div class="search-header">
        <n-input ref="inputRef" v-model:value="value" class="search-input" size="large" clearable placeholder="搜索菜单名称或所属目录" :input-props="inputProps" :loading="navigating" @keydown="handleKeyDown">
          <template #prefix>
            <Icon name="search" :size="20"/>
          </template>
        </n-input>
        <n-button quaternary size="small" aria-label="关闭菜单搜索" @click="show = false">
          关闭
        </n-button>
      </div>

      <div ref="resultsContainer" class="results-container">
        <SearchResults :results="searchResults" :active-index="activeIndex" :list-id="listId" :navigating="navigating" @active-change="setActiveIndex" @select="selectResult"/>
        <n-empty v-if="!searchResults.length" class="search-empty" :description="keyword ? '未找到匹配的菜单' : '输入关键词，快速前往菜单'"/>
      </div>

      <p :id="statusId" class="search-status" role="status" aria-live="polite" aria-atomic="true">
        {{ statusText }} </p>
      <div class="search-footer" aria-hidden="true">
        <span class="key-hint"><kbd>↑</kbd><kbd>↓</kbd> 切换</span>
        <span class="key-hint"><kbd>Enter</kbd> 打开</span>
        <span class="key-hint"><kbd>Esc</kbd> 关闭</span>
        <span v-if="searchResults.length" class="result-count">{{
            searchResults.length
          }} 个结果</span>
      </div>
    </n-card>
  </n-modal>
</template>

<style scoped lang="scss">
.menu-search {
  --search-top: clamp(16px, 10dvh, 100px);

  width: min(600px, calc(100vw - 32px));
  max-height: calc(100dvh - var(--search-top) - 16px);
  margin: var(--search-top) auto 16px;
  overflow: hidden;
}

.search-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.search-input {
  flex: 1;
  min-width: 0;
}

.results-container {
  min-height: 0;
  max-height: 420px;
  padding: 2px 12px;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.search-empty {
  padding: 36px 12px;
}

.search-status {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
}

.search-footer {
  display: flex;
  flex-shrink: 0;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding: 12px 20px;
  border-top: 1px solid var(--n-border-color);
  font-size: 12px;
}

.key-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  kbd {
    min-width: 20px;
    padding: 1px 5px;
    border: 1px solid var(--n-border-color);
    border-radius: 4px;
    line-height: 18px;
    text-align: center;
  }
}

.result-count {
  margin-left: auto;
}

@media (max-width: 480px) {
  .search-header {
    padding: 12px;
  }

  .search-footer {
    gap: 8px;
    padding: 12px;
  }
}
</style>
