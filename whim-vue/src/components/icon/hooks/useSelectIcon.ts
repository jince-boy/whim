import { computed, nextTick, ref, shallowRef, useId, useTemplateRef, watch } from 'vue'
import Fuse from 'fuse.js'
import type { Ref } from 'vue'
import type { InputInst, VirtualListInst } from 'naive-ui'
import { useFormItem } from 'naive-ui/es/_mixins'
import { getIconName, loadIconCatalog, type IconOption } from '../catalog'

const columns = 8
const rowHeight = 48

export interface SelectIconProps {
  iconData?: readonly IconOption[]
  placeholder?: string
  searchInputPlaceholder?: string
  disabled?: boolean
  clearable?: boolean
  size?: 'small' | 'medium' | 'large'
  status?: 'success' | 'warning' | 'error'
}

/** 管理图标网格、搜索和焦点，并接入 Naive UI 表单状态与校验。 */
export function useSelectIcon(props: Readonly<SelectIconProps>, model: Ref<string | null>) {
  const show = ref(false)
  const keyword = ref('')
  const searchQuery = ref('')
  const activeIndex = ref(0)
  const catalog = shallowRef<readonly IconOption[]>([])
  const loading = ref(false)
  const loadFailed = ref(false)
  const panelId = useId()
  const triggerRef = useTemplateRef<HTMLButtonElement>('triggerRef')
  const searchRef = useTemplateRef<InputInst>('searchRef')
  const gridRef = useTemplateRef<HTMLDivElement>('gridRef')
  const virtualListRef = useTemplateRef<VirtualListInst>('virtualListRef')
  const {
    mergedSizeRef: size,
    mergedDisabledRef: disabled,
    mergedStatusRef: status,
    nTriggerFormChange,
    nTriggerFormInput,
    nTriggerFormFocus,
    nTriggerFormBlur,
  } = useFormItem(props)
  const selectedName = computed(() => getIconName(model.value))
  const searchableIcons = computed(() =>
    (props.iconData ?? catalog.value).map((icon) => ({
      icon,
      name: icon.name.replace(/[-_]/g, ' '),
      aliases: icon.aliases?.map((alias) => alias.replace(/[-_]/g, ' ')),
      tags: icon.tags,
      categories: icon.categories,
      label: icon.label,
      keywords: icon.keywords,
    })),
  )
  // 沿用 Lucide 官网的搜索阈值及英文元数据权重，额外支持项目中文标签。
  const searchIndex = computed(
    () =>
      new Fuse(searchableIcons.value, {
        threshold: 0.2,
        useExtendedSearch: true,
        keys: [
          { name: 'name', weight: 3 },
          { name: 'aliases', weight: 8 },
          { name: 'tags', weight: 2 },
          { name: 'categories', weight: 1 },
          { name: 'label', weight: 3 },
          { name: 'keywords', weight: 2 },
        ],
      }),
  )
  const filteredIcons = computed(() => {
    const tokens = searchQuery.value
      .toLowerCase()
      .replace(/[-_]/g, ' ')
      .split(/\s+/)
      .filter(Boolean)
    if (!tokens.length) return props.iconData ?? catalog.value
    return searchIndex.value.search({ $and: tokens }).map(({ item }) => item.icon)
  })
  const iconRows = computed(() => {
    const rows = []
    for (let start = 0; start < filteredIcons.value.length; start += columns) {
      const icons = filteredIcons.value.slice(start, start + columns)
      rows.push({ key: icons[0]!.name, start, icons })
    }
    return rows
  })

  /** 仅在需要完整选择器时加载目录，失败后允许用户重新尝试。 */
  async function loadCatalog() {
    if (loading.value || catalog.value.length || props.iconData) return
    loading.value = true
    loadFailed.value = false
    try {
      catalog.value = await loadIconCatalog()
    } catch {
      loadFailed.value = true
    } finally {
      loading.value = false
    }
  }

  /** 首次展开才下载全量索引，传入自定义选项时无需加载目录。 */
  watch([show, () => props.iconData], ([visible]) => {
    if (visible) void loadCatalog()
  })

  /** 连续输入时仅搜索最后一次内容，卸载时自动取消等待。 */
  watch(keyword, (value, _previous, onCleanup) => {
    const timer = setTimeout(() => {
      searchQuery.value = value.trim()
    }, 150)
    onCleanup(() => clearTimeout(timer))
  })

  /** 切换结果或重新打开时定位选中项，避免沿用旧列表的滚动位置。 */
  watch(
    [filteredIcons, virtualListRef],
    () => {
      activeIndex.value = Math.max(
        0,
        filteredIcons.value.findIndex((icon) => getIconName(icon.name) === selectedName.value),
      )
      virtualListRef.value?.scrollTo({
        index: Math.floor(activeIndex.value / columns),
        debounce: false,
      })
    },
    { flush: 'post' },
  )

  /** 禁用后立即关闭选择面板。 */
  watch(disabled, (value) => {
    if (value) show.value = false
  })

  /** 弹层挂载搜索输入框后聚焦，兼容延迟挂载的弹出内容。 */
  watch(
    searchRef,
    (input) => {
      if (show.value) input?.focus()
    },
    { flush: 'post' },
  )

  /** 每次打开重置搜索，关闭时触发表单失焦校验。 */
  function updateShow(value: boolean) {
    if (disabled.value && value) return
    show.value = value
    if (value) {
      keyword.value = ''
      searchQuery.value = ''
    } else nTriggerFormBlur()
  }

  /** 关闭面板并将键盘焦点交还触发按钮。 */
  function closePanel() {
    updateShow(false)
    triggerRef.value?.focus()
  }

  /** 更新选中值，等待父模型同步后触发表单校验。 */
  async function selectIcon(value: string | null) {
    if (disabled.value) return
    const changed = model.value !== value
    model.value = value
    show.value = false
    triggerRef.value?.focus()
    await nextTick()
    if (changed) {
      nTriggerFormInput()
      nTriggerFormChange()
    }
    nTriggerFormBlur()
  }

  /** 从搜索框进入网格，优先聚焦当前选中的图标。 */
  function focusGrid() {
    void focusIcon(activeIndex.value)
  }

  /** 滚动移出当前按钮后，为可见区域保留一个 Tab 焦点入口。 */
  function syncVisibleFocus(event: Event) {
    const list = event.target as HTMLElement
    const first = Math.floor(list.scrollTop / rowHeight) * columns
    const end = Math.ceil((list.scrollTop + list.clientHeight) / rowHeight) * columns
    if (activeIndex.value < first || activeIndex.value >= end) {
      activeIndex.value = Math.max(0, Math.min(first, filteredIcons.value.length - 1))
    }
  }

  /** 先滚动挂载目标行，再聚焦按钮，支持跨虚拟窗口的键盘导航。 */
  async function focusIcon(index: number) {
    if (!filteredIcons.value.length) return
    const target = Math.max(0, Math.min(index, filteredIcons.value.length - 1))
    activeIndex.value = target
    virtualListRef.value?.scrollTo({ index: Math.floor(target / columns) })
    // 原生滚动事件先更新虚拟窗口，下一帧再等待 Vue 挂载目标按钮。
    await new Promise<void>((resolve) => requestAnimationFrame(() => resolve()))
    await nextTick()
    if (!show.value || activeIndex.value !== target) return
    gridRef.value
      ?.querySelector<HTMLButtonElement>(`button[data-icon-index="${target}"]`)
      ?.focus({ preventScroll: true })
  }

  /** 按网格列数移动焦点，保留 Tab 和 Enter 的原生按钮行为。 */
  function navigateGrid(event: KeyboardEvent) {
    const button = (event.target as HTMLElement).closest<HTMLButtonElement>(
      'button[data-icon-index]',
    )
    if (!button) return
    let target = Number(button.dataset.iconIndex)
    switch (event.key) {
      case 'ArrowRight':
        target += 1
        break
      case 'ArrowLeft':
        target -= 1
        break
      case 'ArrowDown':
        target += columns
        break
      case 'ArrowUp':
        target -= columns
        break
      case 'Home':
        target = 0
        break
      case 'End':
        target = filteredIcons.value.length - 1
        break
      default:
        return
    }
    event.preventDefault()
    void focusIcon(target)
  }

  return {
    show,
    keyword,
    panelId,
    size,
    disabled,
    status,
    selectedName,
    activeIndex,
    loading,
    loadFailed,
    loadCatalog,
    rowHeight,
    filteredIcons,
    iconRows,
    updateShow,
    closePanel,
    selectIcon,
    focusGrid,
    syncVisibleFocus,
    navigateGrid,
    nTriggerFormFocus,
    nTriggerFormBlur,
  }
}
