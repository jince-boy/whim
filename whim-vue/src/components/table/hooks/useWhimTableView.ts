import { computed, onBeforeUnmount, onMounted, shallowRef, useTemplateRef } from 'vue'
import type { DataTableInst } from 'naive-ui'
import { useUserStore } from '@/stores/modules/user'
import type { TableButton, WhimTableBinding, WhimTableEmits, WhimTableRegistration } from '../types'

/** 表格视图只连接状态、呈现工具栏和发出业务事件。 */
export function useWhimTableView(
  emit: <Event extends keyof WhimTableEmits>(event: Event, ...args: WhimTableEmits[Event]) => void,
) {
  const binding = shallowRef<WhimTableBinding>()
  const tableRef = useTemplateRef<DataTableInst>('tableRef')
  const userStore = useUserStore()
  const tableProps = computed(() => binding.value?.tableProps.value)
  const toolbar = computed(() => binding.value?.toolbar.value ?? {})
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
  const buttonTypes = { add: 'primary', edit: 'info', delete: 'error', export: 'warning' } as const
  const buttonIcons = {
    add: 'zengjia',
    edit: 'xiugai',
    delete: 'shanchu',
    export: 'daochu',
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

  const registration: WhimTableRegistration = {
    /** 接入 hook 持有的响应式视图。 */
    connect: (value) => {
      binding.value = value
    },
    /** 注册接口允许在挂载前读取为空。 */
    getInstance: () => tableRef.value ?? undefined,
  }
  onMounted(() => emit('register', registration))
  onBeforeUnmount(() => emit('register', undefined))
  return {
    binding,
    tableProps,
    toolbar,
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
