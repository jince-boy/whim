import { computed, nextTick, ref, useId, useTemplateRef, watch } from 'vue'
import type { Ref } from 'vue'
import type { InputInst } from 'naive-ui'
import { useFormItem } from 'naive-ui/es/_mixins'
import iconfont from '@/assets/iconfont/iconfont.json'

export interface SelectIconProps {
  iconData?: readonly { name: string; font_class: string }[]
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
  const panelId = useId()
  const triggerRef = useTemplateRef<HTMLButtonElement>('triggerRef')
  const searchRef = useTemplateRef<InputInst>('searchRef')
  const gridRef = useTemplateRef<HTMLDivElement>('gridRef')
  const {
    mergedSizeRef: size,
    mergedDisabledRef: disabled,
    mergedStatusRef: status,
    nTriggerFormChange,
    nTriggerFormInput,
    nTriggerFormFocus,
    nTriggerFormBlur,
  } = useFormItem(props)
  const filteredIcons = computed(() => {
    const query = keyword.value.trim().toLowerCase()
    const icons = props.iconData ?? iconfont.glyphs
    return query
      ? icons.filter((icon) => `${icon.name} ${icon.font_class}`.toLowerCase().includes(query))
      : icons
  })

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
    if (value) keyword.value = ''
    else nTriggerFormBlur()
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
    const grid = gridRef.value
    const button =
      grid?.querySelector<HTMLButtonElement>('[aria-pressed="true"]') ??
      grid?.querySelector<HTMLButtonElement>('button')
    button?.focus()
  }

  /** 按网格列数移动焦点，保留 Tab 和 Enter 的原生按钮行为。 */
  function navigateGrid(event: KeyboardEvent) {
    const buttons = Array.from(gridRef.value?.querySelectorAll<HTMLButtonElement>('button') ?? [])
    const index = buttons.indexOf(event.target as HTMLButtonElement)
    if (index < 0) return
    let target = index
    switch (event.key) {
      case 'ArrowRight':
        target += 1
        break
      case 'ArrowLeft':
        target -= 1
        break
      case 'ArrowDown':
        target += 8
        break
      case 'ArrowUp':
        target -= 8
        break
      case 'Home':
        target = 0
        break
      case 'End':
        target = buttons.length - 1
        break
      default:
        return
    }
    event.preventDefault()
    buttons[Math.max(0, Math.min(target, buttons.length - 1))]?.focus()
  }

  return {
    show,
    keyword,
    panelId,
    size,
    disabled,
    status,
    filteredIcons,
    updateShow,
    closePanel,
    selectIcon,
    focusGrid,
    navigateGrid,
    nTriggerFormFocus,
    nTriggerFormBlur,
  }
}
