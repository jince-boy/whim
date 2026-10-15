<script setup lang="ts">
import { computed, ref, useId, useTemplateRef, watch } from 'vue'
import { useRouter, withBase } from 'vitepress'
import { NEmpty, NInput, NModal, type InputInst } from 'naive-ui'
import { data as pages } from '../pages.data'

const show = defineModel<boolean>({ required: true })
const router = useRouter()
const inputRef = useTemplateRef<InputInst>('inputRef')
const resultsRef = useTemplateRef<HTMLElement>('resultsRef')
const query = ref('')
const activeIndex = ref(0)
const listId = `docs-search-${useId()}`
const terms = computed(() => query.value.trim().toLowerCase().split(/\s+/).filter(Boolean))
const results = computed(() => {
  if (!terms.value.length) return []
  return pages
    .filter((page) =>
      terms.value.every((term) => `${page.title} ${page.text}`.toLowerCase().includes(term)),
    )
    .sort(
      (first, second) =>
        Number(second.title.toLowerCase().includes(terms.value[0]!)) -
        Number(first.title.toLowerCase().includes(terms.value[0]!)),
    )
    .slice(0, 20)
    .map((page) => {
      const start = Math.max(0, page.text.toLowerCase().indexOf(terms.value[0]!) - 24)
      return { ...page, excerpt: `${start ? '…' : ''}${page.text.slice(start, start + 120)}…` }
    })
})
watch(show, (visible) => {
  if (visible) query.value = ''
})
watch(
  results,
  () => {
    activeIndex.value = 0
  },
  { flush: 'sync' },
)
watch(
  activeIndex,
  () => {
    resultsRef.value?.querySelector('[aria-selected="true"]')?.scrollIntoView({ block: 'nearest' })
  },
  { flush: 'post' },
)

/** 入场完成后聚焦输入框。 */
function focusSearch() {
  inputRef.value?.focus()
}

/** 搜索框内切换或打开结果；输入法组合键保留默认行为。 */
function handleKeyDown(event: KeyboardEvent) {
  if (
    event.isComposing ||
    event.defaultPrevented ||
    event.ctrlKey ||
    event.metaKey ||
    event.altKey ||
    event.shiftKey ||
    !results.value.length
  )
    return
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault()
    activeIndex.value =
      (activeIndex.value + (event.key === 'ArrowDown' ? 1 : -1) + results.value.length) %
      results.value.length
  } else if (event.key === 'Enter') {
    const selected = results.value[activeIndex.value]
    if (selected) {
      event.preventDefault()
      openResult(selected.url)
    }
  }
}

/** 使用 VitePress 路由，保留子路径部署能力。 */
function openResult(url: string) {
  show.value = false
  void router.go(withBase(url))
}
</script>

<template>
  <NModal
    v-model:show="show"
    preset="card"
    title="搜索文档"
    class="docs-search-modal"
    :bordered="false"
    @after-enter="focusSearch"
  >
    <NInput
      ref="inputRef"
      v-model:value="query"
      clearable
      placeholder="搜索组件、注解或权限规则…"
      :input-props="{
        role: 'combobox',
        'aria-label': '搜索文档',
        'aria-controls': listId,
        'aria-expanded': results.length > 0,
        'aria-autocomplete': 'list',
        'aria-activedescendant': results.length ? `${listId}-${activeIndex}` : undefined,
      }"
      @keydown="handleKeyDown"
    />
    <div ref="resultsRef" class="docs-search-results">
      <ul :id="listId" role="listbox" aria-label="文档搜索结果">
        <li
          v-for="(result, index) in results"
          :id="`${listId}-${index}`"
          :key="result.url"
          role="option"
          :aria-selected="index === activeIndex"
          @pointermove="activeIndex = index"
          @mousedown.prevent
          @click="openResult(result.url)"
        >
          <strong>{{ result.title }}</strong>
          <p>{{ result.excerpt }}</p>
        </li>
      </ul>
      <NEmpty
        v-if="!results.length"
        :description="
          terms.length ? '没有找到相关内容，试试更简短的关键词' : '输入关键词，搜索全部文档正文'
        "
      />
    </div>
    <p class="search-help" role="status" aria-live="polite">
      {{ terms.length ? `找到 ${results.length} 篇相关文档` : '支持多个关键词，以空格分隔' }} · ↑ ↓
      切换 · Enter 打开
    </p>
  </NModal>
</template>
