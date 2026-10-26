import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref,
  shallowRef,
  useId,
  useTemplateRef,
  watch,
} from 'vue'
import { useThemeVars, type FormInst, type GridProps } from 'naive-ui'
import type { SearchFormBinding, SearchFormEmits, SearchFormRegistration } from '../types'

/** 连接查询表单 hook，并将用户提交、重置作为事件通知调用方。 */
export function useSearchFormView(
  emit: <Event extends keyof SearchFormEmits>(
    event: Event,
    ...args: SearchFormEmits[Event]
  ) => void,
) {
  const binding = shallowRef<SearchFormBinding>()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formLayout = useTemplateRef<HTMLElement>('formLayout')
  const themeVars = useThemeVars()
  const panelId = useId()
  const title = computed(() => binding.value?.title.value ?? '筛选条件')
  const columnCount = ref(1)
  const collapsed = ref(true)
  let resizeObserver: ResizeObserver | undefined
  const submitting = ref(false)
  const formProps = computed(() => binding.value?.formProps.value)
  const filters = computed(() => binding.value?.filters.value ?? [])
  const gridProps = computed<GridProps>(() => {
    const config = binding.value?.gridProps.value
    const cols = config?.cols ?? columnCount.value
    return {
      ...config,
      cols,
      // 单列时保留一个查询项，操作区占用下一行。
      collapsedRows: config?.collapsedRows ?? (cols === 1 || cols === '1' ? 2 : 1),
      collapsed: collapsed.value,
    }
  })
  const gridStyle = computed(() => ({
    // 显式配置列数时保留原生栅格；默认字段不随宽屏拉伸。
    gridTemplateColumns: binding.value?.gridProps.value.cols
      ? undefined
      : `repeat(${columnCount.value}, minmax(0, 278px))`,
  }))

  watch(
    () => binding.value?.collapsed.value,
    (value) => {
      collapsed.value = value ?? true
    },
  )

  /** 展开和收起只影响布局，保留查询条件与字段实例。 */
  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  /** 使用原生表单校验，未挂载时明确失败。 */
  const validate: FormInst['validate'] = (...args) => {
    if (!formRef.value) throw new Error('查询表单尚未挂载')
    return formRef.value.validate(...args)
  }

  /** 清理字段校验。 */
  function restoreValidation() {
    formRef.value?.restoreValidation()
  }

  /** 阻止重复校验，无效字段留在表单中展示。 */
  async function submit() {
    if (!binding.value || submitting.value) return false
    submitting.value = true
    try {
      await validate()
      binding.value.commitValues()
      emit('submit')
      return true
    } catch {
      collapsed.value = false
      return false
    } finally {
      submitting.value = false
    }
  }

  /** 通过 hook 恢复模型，再通知调用方使用新条件。 */
  function reset() {
    if (!binding.value || submitting.value) return
    binding.value.resetFields()
    binding.value.commitValues()
    emit('reset')
  }

  /** 撤销一个已应用条件后通知调用方重新查询。 */
  function removeFilter(key: string) {
    if (!binding.value || submitting.value) return
    if (binding.value.removeFilter(key)) emit('filterChange')
  }

  const registration: SearchFormRegistration = {
    /** 使用 hook 持有的同一份响应式状态。 */
    connect: (value) => {
      binding.value = value
    },
    validate,
    restoreValidation,
    submit,
    reset,
  }
  onMounted(() => {
    emit('register', registration)
    resizeObserver = new ResizeObserver(([entry]) => {
      if (!entry) return
      const gap = binding.value?.gridProps.value.xGap
      const spacing = typeof gap === 'number' ? gap : 10
      // 每格至少预留 220px，宽裕时增长到 278px，避免双栏表单过早换行。
      columnCount.value = Math.max(
        1,
        Math.min(4, Math.floor((entry.contentRect.width + spacing) / (220 + spacing))),
      )
    })
    if (formLayout.value) resizeObserver.observe(formLayout.value)
  })
  onBeforeUnmount(() => {
    resizeObserver?.disconnect()
    emit('register', undefined)
  })
  return {
    binding,
    themeVars,
    panelId,
    title,
    formProps,
    gridProps,
    gridStyle,
    filters,
    removeFilter,
    collapsed,
    toggleCollapsed,
    submitting,
    submit,
    reset,
    validate,
    restoreValidation,
  }
}
