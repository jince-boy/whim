import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import {
  MenuType,
  menuTypeOptions,
  type MenuFormProps,
  type MenuSave,
  type MenuItem,
} from './types'
import { componentOptions } from '@/utils/navigation'
import { useUserStore } from '@/stores/modules/user'
import { createMenuRules } from './rule'

/** 编辑草稿与列表隔离，父节点选项排除自身、后代及不匹配的节点类型。 */
export function useMenuForm(props: MenuFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const user = useUserStore()
  const defaults: MenuSave = {
    parentId: '0',
    menuName: '',
    menuType: MenuType.DIRECTORY,
    routeName: '',
    path: '',
    component: null,
    param: null,
    perms: null,
    dataPermission: 0,
    isCache: 0,
    sort: 0,
    icon: null,
    visible: 0,
    status: 0,
    remark: null,
  }
  const formModel = ref<MenuSave>({ ...defaults })
  const builtIn = computed(() => props.initialModel?.builtIn === 1)
  const semanticsLocked = computed(() => builtIn.value || !user.roleCode.includes('superadmin'))
  const parentOptions = computed(() => {
    /** 排除整个当前子树，仅目录可容纳导航，页面可容纳按钮。 */
    const filter = (nodes: MenuItem[]): object[] =>
      nodes.flatMap((node) => {
        if (String(node.id) === String(props.initialModel?.id)) return []
        const allowed = formModel.value.menuType === 3 ? node.menuType === 2 : node.menuType === 1
        const children = filter(node.children ?? [])
        if (!allowed && !children.length) return []
        return [
          {
            label: node.menuName,
            key: String(node.id),
            disabled: !allowed,
            children: children.length ? children : undefined,
          },
        ]
      })
    const nodes = filter(props.extendedData?.menuData ?? [])
    return formModel.value.menuType === 3
      ? nodes
      : [{ label: '顶级菜单', key: '0', children: nodes }]
  })
  /** 类型切换只清理不适用字段，不重置已经填写的标题和排序。 */
  function changeType(type: MenuType) {
    if (type === 1 || type === 4) {
      formModel.value.perms = null
      formModel.value.dataPermission = 0
    }
    if (type !== 2) {
      formModel.value.component = null
      formModel.value.param = null
      formModel.value.isCache = 0
    }
    if (type === 3 || type === 4) formModel.value.routeName = null
    formModel.value.path = ''
    formModel.value.parentId = '0'
    formRef.value?.restoreValidation()
  }
  watch(
    () => props.initialModel,
    (model) => {
      // 只复制请求字段，id、children、builtIn等服务端元数据不进入提交体。
      const values = model
        ? Object.fromEntries(
            Object.keys(defaults).map((key) => [key, model[key as keyof MenuSave]]),
          )
        : {}
      formModel.value = {
        ...defaults,
        ...values,
        parentId: String(model?.parentId ?? props.extendedData?.parentId ?? '0'),
      }
      const parent = props.extendedData?.menuData
      if (!model && parent) {
        /** 查找预选父节点，页面下新增默认创建按钮权限。 */
        const find = (nodes: MenuItem[]): MenuItem | undefined => {
          for (const node of nodes) {
            if (String(node.id) === String(formModel.value.parentId)) return node
            const child = find(node.children ?? [])
            if (child) return child
          }
        }
        if (find(parent)?.menuType === 2) formModel.value.menuType = MenuType.BUTTON
      }
    },
    { immediate: true },
  )
  const rules = createMenuRules(formModel)
  const showStatusOptions = [
    { label: '显示', value: 0 },
    { label: '隐藏', value: 1 },
  ]
  const runStatusOptions = [
    { label: '启用', value: 0 },
    { label: '停用', value: 1 },
  ]
  const cacheStatusOptions = [
    { label: '不缓存', value: 0 },
    { label: '缓存', value: 1 },
  ]
  return {
    formRef,
    formModel,
    builtIn,
    semanticsLocked,
    parentOptions,
    componentOptions,
    menuTypeOptions,
    rules,
    showStatusOptions,
    runStatusOptions,
    cacheStatusOptions,
    changeType,
  }
}
