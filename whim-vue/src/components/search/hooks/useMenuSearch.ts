import {
  computed,
  ref,
  useId,
  useTemplateRef,
  watch,
  type InputHTMLAttributes,
  type Ref,
} from 'vue'
import { isNavigationFailure, NavigationFailureType, useRouter } from 'vue-router'
import type { InputInst } from 'naive-ui'
import { usePermissionStore } from '@/stores/modules/permission'
import { getVisibleMenus, type SafeMenuOption } from '@/utils/menu'
import type { MenuSearchItem } from './types'

/** 管理可访问菜单的搜索、选中和导航，键盘操作仅由搜索输入框触发。 */
export function useMenuSearch(show: Ref<boolean>) {
  const permissionStore = usePermissionStore()
  const router = useRouter()
  const inputRef = useTemplateRef<InputInst>('inputRef')
  const resultsContainer = useTemplateRef<HTMLElement>('resultsContainer')
  const listId = `menu-search-${useId()}`
  const statusId = `${listId}-status`
  const value = ref('')
  const activeIndex = ref(-1)
  const navigating = ref(false)
  let composing = false
  const keyword = computed(() => value.value.trim().toLowerCase())

  // 菜单变化时重建索引，输入时只过滤，避免反复遍历菜单和解析路由。
  const menuItems = computed(() => {
    const items: MenuSearchItem[] = []

    /** 收集可访问的内部页面，并保留所属目录供展示和搜索。 */
    function collectMenus(menus: SafeMenuOption[], ancestors: string[]) {
      for (const menu of menus) {
        if (menu.disabled || menu.isExternal) continue
        const name = menu.name || ''
        if (menu.target && !menu.directory) {
          items.push({
            key: String(menu.key),
            name,
            breadcrumb: ancestors.join(' / '),
            path: router.resolve(menu.target).fullPath,
          })
        }
        if (menu.children?.length) collectMenus(menu.children, [...ancestors, name])
      }
    }

    collectMenus(getVisibleMenus(permissionStore.menus), [])
    return items
  })

  const searchResults = computed(() =>
    keyword.value
      ? menuItems.value.filter((item) =>
          `${item.breadcrumb} / ${item.name}`.toLowerCase().includes(keyword.value),
        )
      : [],
  )
  const statusText = computed(() => {
    if (!keyword.value) return '输入菜单名称或所属目录进行搜索'
    return searchResults.value.length
      ? `找到 ${searchResults.value.length} 个菜单`
      : '未找到匹配的菜单'
  })
  const inputProps = computed<InputHTMLAttributes>(() => ({
    role: 'combobox',
    'aria-label': '搜索菜单',
    'aria-autocomplete': 'list',
    'aria-haspopup': 'listbox',
    'aria-expanded': show.value && searchResults.value.length > 0,
    'aria-controls': listId,
    'aria-describedby': statusId,
    'aria-activedescendant': activeIndex.value >= 0 ? `${listId}-${activeIndex.value}` : undefined,
    autocomplete: 'off',
    spellcheck: false,
    onCompositionstart: handleCompositionStart,
    onCompositionend: handleCompositionEnd,
  }))

  watch(show, (visible) => {
    if (!visible) return
    value.value = ''
    composing = false
  })

  watch(
    searchResults,
    (results) => {
      activeIndex.value = results.length ? 0 : -1
    },
    { flush: 'sync' },
  )

  // 只滚动结果容器，避免选中项变化带动弹窗或页面滚动。
  watch(
    [activeIndex, searchResults, resultsContainer],
    () => {
      const container = resultsContainer.value
      const selected = container?.querySelector<HTMLElement>('[aria-selected="true"]')
      if (!container || !selected) return
      const containerBounds = container.getBoundingClientRect()
      const selectedBounds = selected.getBoundingClientRect()
      if (selectedBounds.top < containerBounds.top) {
        container.scrollTop -= containerBounds.top - selectedBounds.top
      } else if (selectedBounds.bottom > containerBounds.bottom) {
        container.scrollTop += selectedBounds.bottom - containerBounds.bottom
      }
    },
    { flush: 'post' },
  )

  /** 打开弹窗后将输入焦点放到搜索框。 */
  function focusInput() {
    if (show.value) inputRef.value?.focus()
  }

  /** 记录输入法组合状态，避免确认候选词时触发菜单导航。 */
  function handleCompositionStart() {
    composing = true
  }

  /** 候选词确认后恢复搜索框快捷键。 */
  function handleCompositionEnd() {
    composing = false
  }

  /** 鼠标和键盘共用同一个选中索引。 */
  function setActiveIndex(index: number) {
    if (index >= 0 && index < searchResults.value.length) activeIndex.value = index
  }

  /** 导航完成后关闭弹窗；失败时保留结果，并阻止重复提交。 */
  async function selectResult(item: MenuSearchItem) {
    if (!show.value || navigating.value) return
    navigating.value = true
    try {
      const failure = await router.push(item.path)
      if (!failure || isNavigationFailure(failure, NavigationFailureType.duplicated)) {
        show.value = false
      }
    } catch {
      window.$message?.error('页面打开失败，请稍后重试')
    } finally {
      navigating.value = false
    }
  }

  /** 输入框内循环切换结果或确认选中项，保留输入法和组合键的默认行为。 */
  function handleKeyDown(event: KeyboardEvent) {
    if (
      !show.value ||
      event.target !== inputRef.value?.inputElRef ||
      composing ||
      event.isComposing ||
      event.defaultPrevented ||
      event.ctrlKey ||
      event.altKey ||
      event.metaKey ||
      event.shiftKey ||
      navigating.value ||
      !searchResults.value.length
    ) {
      return
    }
    if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
      event.preventDefault()
      event.stopPropagation()
      const direction = event.key === 'ArrowDown' ? 1 : -1
      activeIndex.value =
        (activeIndex.value + direction + searchResults.value.length) % searchResults.value.length
    } else if (event.key === 'Enter') {
      const selected = searchResults.value[activeIndex.value]
      if (!selected) return
      event.preventDefault()
      event.stopPropagation()
      void selectResult(selected)
    }
  }

  return {
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
  }
}
