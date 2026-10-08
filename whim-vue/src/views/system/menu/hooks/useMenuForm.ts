import type { FormInst } from 'naive-ui'
import { MenuType, type Menu, type MenuFormProps, type MenuItem } from './types'

/** 管理独立的菜单编辑草稿、父节点选项和类型切换。 */
export function useMenuForm(props: MenuFormProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const defaultFormModel: Menu = {
    id: '',
    name: '',
    title: '',
    parentId: '0',
    type: MenuType.DIRECTORY,
    path: '',
    queryParam: '',
    component: '',
    keepAlive: 0,
    sort: 0,
    code: '',
    visible: 0,
    status: 0,
    icon: '',
    redirect: '',
    remark: '',
  }
  const formModel = ref<Menu>({ ...defaultFormModel, ...props.initialModel })
  const extendedData = computed(
    () =>
      props.extendedData ?? {
        menuTypeDict: [],
        sysShowStatusDict: [],
        sysRunStatusDict: [],
        sysMenuKeepAliveStatus: [],
        menuData: [],
      },
  )
  const fieldClearConfig: Record<MenuType, Partial<Menu>> = {
    [MenuType.DIRECTORY]: { code: '', redirect: '', queryParam: '', component: '', keepAlive: 0 },
    [MenuType.MENU]: { code: '', redirect: '' },
    [MenuType.BUTTON]: {
      name: '',
      path: '',
      queryParam: '',
      component: '',
      keepAlive: 0,
      icon: '',
      redirect: '',
      visible: 0,
    },
    [MenuType.EXTERNAL]: { code: '', queryParam: '', component: '', keepAlive: 0 },
  }

  /** 递归保留允许作为父节点的目录和菜单。 */
  const filterMenuByType = (menus: MenuItem[]): MenuItem[] =>
    menus
      .filter((menu) => menu.type === MenuType.DIRECTORY || menu.type === MenuType.MENU)
      .map((menu) => {
        const children = menu.children?.length ? filterMenuByType(menu.children) : undefined
        return { ...menu, children: children?.length ? children : undefined }
      })

  const menuData = computed(() => [
    {
      title: '顶级菜单',
      id: '0',
      type: MenuType.DIRECTORY,
      children: filterMenuByType(extendedData.value.menuData),
    } as MenuItem,
  ])

  /** 类型切换时清理无关字段，保留当前草稿内已编辑的其他字段。 */
  const clearUnrelatedFields = (newType: MenuType) => {
    formModel.value = { ...formModel.value, ...fieldClearConfig[newType], type: newType }
  }

  // 当前模型字段均为标量；初始值与父节点变化时创建新的草稿。
  watch(
    [() => props.initialModel, () => props.extendedData?.parentId],
    ([model, parentId]) => {
      formModel.value = { ...defaultFormModel, ...model }
      if (parentId !== undefined) formModel.value.parentId = parentId
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )

  return { formRef, formModel, extendedData, menuData, clearUnrelatedFields }
}
