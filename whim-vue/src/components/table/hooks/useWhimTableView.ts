import {
  computed,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
  shallowRef,
  useTemplateRef,
} from 'vue'
import { useThemeVars, type DataTableInst } from 'naive-ui'
import { message } from '@/plugins'
import screenfull from 'screenfull'
import { useUserStore } from '@/stores/modules/user'
import type { TableButton, WhimTableBinding, WhimTableEmits, WhimTableRegistration } from '../types'

/** 表格视图只连接状态、呈现工具栏和发出业务事件。 */
export function useWhimTableView(
  emit: <Event extends keyof WhimTableEmits>(event: Event, ...args: WhimTableEmits[Event]) => void,
) {
  const binding = shallowRef<WhimTableBinding>()
  const tableRef = useTemplateRef<DataTableInst>('tableRef')
  const tableContainer = useTemplateRef<HTMLElement>('tableContainer')
  const themeVars = useThemeVars()
  const fullscreenEnabled = screenfull.isEnabled
  const isFullscreen = ref(false)
  const settingsVisible = ref(false)
  const userStore = useUserStore()
  const title = computed(() => binding.value?.title.value ?? '数据列表')
  const tableProps = computed(() => binding.value?.tableProps.value)
  const toolbar = computed(() => binding.value?.toolbar.value ?? {})
  const showSettings = computed(
    () => toolbar.value.showColumns !== false || toolbar.value.showStriped !== false,
  )
  const showTools = computed(
    () =>
      toolbar.value.showRefresh !== false ||
      showSettings.value ||
      toolbar.value.showFullscreen !== false,
  )
  const buttons = computed(() => {
    const config = toolbar.value
    return (config.buttons ?? []).filter((button) => {
      const permissions = config.permissions?.[button]
      const roles = config.roles?.[button]
      const permitted =
        !permissions ||
        userStore.permissionCode.includes('*') ||
        permissions.some((code) => userStore.permissionCode.includes(code))
      const hasRole =
        !roles ||
        userStore.roleCode.includes('*') ||
        roles.some((role) => userStore.roleCode.includes(role))
      return permitted && hasRole
    })
  })
  const buttonLabels = { add: '新增', edit: '修改', delete: '删除', export: '导出' } as const
  // 新增跟随主题主色，修改和删除使用各自的语义色。
  const buttonTypes = {
    add: 'primary',
    edit: 'warning',
    delete: 'error',
    export: 'warning',
  } as const
  const buttonIcons = {
    add: 'plus',
    edit: 'pencil',
    delete: 'trash-2',
    export: 'download',
  } as const

  /** 编辑要求唯一选择，删除要求至少选择一行。 */
  function isDisabled(button: TableButton) {
    const count = binding.value?.checkedRowKeys.value.length ?? 0
    return (
      !!binding.value?.loading.value ||
      (button === 'edit' && count !== 1) ||
      (button === 'delete' && count === 0)
    )
  }

  /** 事件使用独立的主键快照，业务逻辑留给页面。 */
  function handleAction(button: TableButton) {
    if (isDisabled(button)) return
    const keys = [...(binding.value?.checkedRowKeys.value ?? [])]
    if (button === 'add') emit('add')
    else if (button === 'edit' && keys[0] !== undefined) emit('edit', keys[0])
    else if (button === 'delete') emit('delete', keys)
    else if (button === 'export') emit('export', keys)
  }

  /** 刷新由 hook 完成，事件只作为操作通知。 */
  function refresh() {
    void binding.value?.reload()
    emit('refresh')
  }

  /** 命令式刷新返回请求是否成功。 */
  function reload() {
    if (!binding.value) throw new Error('表格尚未注册')
    return binding.value.reload()
  }

  /** 返回挂载后的原生表格实例。 */
  function getTableInstance() {
    if (!tableRef.value) throw new Error('表格尚未挂载')
    return tableRef.value
  }

  /** 同步当前表格的全屏状态，包括 Esc 退出。 */
  function syncFullscreen() {
    isFullscreen.value = screenfull.isFullscreen && screenfull.element === tableContainer.value
  }

  /** 全屏展示当前查询表单和表格，保留原有数据与选择状态。 */
  async function toggleFullscreen() {
    if (!fullscreenEnabled || !tableContainer.value) return
    settingsVisible.value = false
    try {
      if (isFullscreen.value) await screenfull.exit()
      else await screenfull.request(tableContainer.value)
    } catch {
      message.error('切换表格全屏失败')
    }
  }

  /** 离开缓存页面或卸载时释放当前表格占用的全屏。 */
  function exitFullscreen() {
    settingsVisible.value = false
    if (fullscreenEnabled && screenfull.element === tableContainer.value) {
      void screenfull.exit().catch(() => message.error('退出表格全屏失败'))
    }
  }

  const registration: WhimTableRegistration = {
    /** 接入 hook 持有的响应式视图。 */
    connect: (value) => {
      binding.value = value
    },
    /** 注册接口允许在挂载前读取为空。 */
    getInstance: () => tableRef.value ?? undefined,
  }
  onMounted(() => {
    emit('register', registration)
    if (fullscreenEnabled) screenfull.on('change', syncFullscreen)
  })
  onDeactivated(exitFullscreen)
  onBeforeUnmount(() => {
    exitFullscreen()
    if (fullscreenEnabled) screenfull.off('change', syncFullscreen)
    emit('register', undefined)
  })
  return {
    tableContainer,
    themeVars,
    fullscreenEnabled,
    isFullscreen,
    toggleFullscreen,
    binding,
    title,
    tableProps,
    toolbar,
    showSettings,
    showTools,
    settingsVisible,
    buttons,
    buttonLabels,
    buttonTypes,
    buttonIcons,
    isDisabled,
    handleAction,
    refresh,
    reload,
    getTableInstance,
  }
}
