import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst, TreeSelectOption } from 'naive-ui'
import { MenuType, type MenuFormProps, type MenuSave, type MenuItem } from './types'
import { pageComponentPaths } from '@/router/dynamicRoutes'
import { createMenuRules } from './rule'

/** 维护独立草稿，父级选择与类型联动遵循后端的层级和内置菜单约束。 */
export function useMenuForm(props: MenuFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const defaults: MenuSave = {
    parentId: '0',
    menuName: '',
    menuType: MenuType.DIRECTORY,
    routeName: '',
    path: '',
    component: null,
    param: null,
    perms: null,
    isCache: 0,
    sort: 0,
    icon: null,
    visible: 0,
    status: 0,
    remark: null,
  }
  const formModel = ref<MenuSave>({ ...defaults })
  const isPage = computed(() => formModel.value.menuType === MenuType.MENU)
  const isButton = computed(() => formModel.value.menuType === MenuType.BUTTON)
  const isExternal = computed(() => formModel.value.menuType === MenuType.EXTERNAL)
  const semanticsLocked = computed(() => props.initialModel?.builtIn === 1)
  const typeLockReason = computed(() => {
    if (props.initialModel?.builtIn === 1) return '内置菜单的类型和权限标识不可修改'
    return props.initialModel?.children?.length ? '存在子节点的菜单不可修改类型' : undefined
  })
  const statusLockReason = computed(() => {
    if (props.initialModel?.protectedNode) return '核心菜单管理节点不能停用'
    return props.initialModel?.children?.some((node) => node.status === 0)
      ? '请先停用所有启用的子节点'
      : undefined
  })
  const parentOptions = computed<TreeSelectOption[]>(() => {
    const disabledLabel =
      props.dictionaryOptions.statuses.find((item) => item.value === 1)?.label ?? '1'
    /** 排除自身和后代，启用节点不能挂在停用分支下。 */
    function filter(nodes: MenuItem[], disabledAncestor = false): TreeSelectOption[] {
      return nodes.flatMap((node) => {
        if (String(node.id) === String(props.initialModel?.id)) return []
        const allowed = isButton.value
          ? node.menuType === MenuType.MENU
          : node.menuType === MenuType.DIRECTORY
        const disabledBranch = disabledAncestor || node.status === 1
        const children = filter(node.children ?? [], disabledBranch)
        if (!allowed && !children.length) return []
        return [
          {
            label: node.status === 1 ? `${node.menuName}（${disabledLabel}）` : node.menuName,
            key: String(node.id),
            disabled: !allowed || (formModel.value.status === 0 && disabledBranch),
            children: children.length ? children : undefined,
          },
        ]
      })
    }
    const nodes = filter(props.menus)
    return isButton.value ? nodes : [{ label: '顶级菜单', key: '0', children: nodes }]
  })
  const parentValid = computed(() => {
    /** 判断当前父级是否仍符合类型、层级及启用状态要求。 */
    function includes(options: TreeSelectOption[]): boolean {
      return options.some((option) =>
        String(option.key) === String(formModel.value.parentId)
          ? !option.disabled
          : includes(option.children ?? []),
      )
    }
    return includes(parentOptions.value)
  })
  const componentOptions = computed(() => {
    const options = pageComponentPaths.map((path) => ({ label: path, value: path }))
    const current = formModel.value.component
    if (current && !pageComponentPaths.includes(current)) {
      options.unshift({ label: `${current}（当前配置）`, value: current })
    }
    return options
  })

  /** 切换类型时保留仍然适用的路由和父级，只清理不适用字段。 */
  function changeType(type: MenuType) {
    const previous = formModel.value.menuType
    formModel.value.menuType = type
    const internal = type === MenuType.DIRECTORY || type === MenuType.MENU
    const wasInternal = previous === MenuType.DIRECTORY || previous === MenuType.MENU
    if (!internal) formModel.value.routeName = null
    if (!internal || !wasInternal) formModel.value.path = type === MenuType.BUTTON ? null : ''
    if (type === MenuType.DIRECTORY || type === MenuType.EXTERNAL) formModel.value.perms = null
    if (type !== MenuType.MENU) {
      formModel.value.component = null
      formModel.value.param = null
      formModel.value.isCache = 0
    }
    if (type === MenuType.BUTTON) formModel.value.icon = null
    if (!parentValid.value) formModel.value.parentId = type === MenuType.BUTTON ? '' : '0'
    formRef.value?.restoreValidation()
  }

  watch(
    [() => props.initialModel, () => props.parentMenu],
    ([model, parent]) => {
      // 按请求字段复制，避免把子节点、内置标志和审计字段带入提交体。
      const values = model
        ? Object.fromEntries(
            Object.keys(defaults).map((key) => [key, model[key as keyof MenuSave]]),
          )
        : {}
      formModel.value = {
        ...defaults,
        menuType: parent
          ? parent.menuType === MenuType.MENU
            ? MenuType.BUTTON
            : MenuType.MENU
          : MenuType.DIRECTORY,
        status: parent?.status ?? 0,
        ...values,
        parentId: String(model?.parentId ?? parent?.id ?? '0'),
      }
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )

  return {
    formRef,
    formModel,
    rules: createMenuRules(formModel, parentValid),
    parentOptions,
    componentOptions,
    semanticsLocked,
    typeLockReason,
    statusLockReason,
    isPage,
    isButton,
    isExternal,
    changeType,
  }
}
