import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NSpace, NTag, useMessage, type DataTableColumn } from 'naive-ui'
import { fetchMenuList, fetchMenuDetail, insertMenu, updateMenu, deleteMenu } from './api'
import { menuTypeOptions, type MenuId, type MenuItem, type MenuSave } from './types'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'
import { useFormDialog } from '@/components/dialog/useFormDialog'
import MenuForm from '../form.vue'
import { useIcon } from '@/components/icon/useIcon'

/** 菜单管理使用完整目录作为编辑基准，搜索只改变表格展示，不截断父节点选项。 */
export function useMenu() {
  const { createIcon } = useIcon()
  const userStore = useUserStore()
  const navigation = usePermissionStore()
  const message = useMessage()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const form = reactive({ menuName: '' })
  const catalog = ref<MenuItem[]>([])
  const loading = ref(false)
  const expandedRowKeys = ref<MenuId[]>([])
  /** 表格操作权限随当前授权变化，不依赖一次性的DOM删除。 */
  const can = (code: string) =>
    userStore.permissionCode.includes(code) || userStore.permissionCode.includes('*')
  const canCreate = computed(
    () => can('system:menu:create') && userStore.roleCode.includes('superadmin'),
  )
  const tableData = computed(() => {
    const keyword = form.menuName.trim()
    /** 搜索保留匹配节点祖先，匹配目录时展示其完整子树。 */
    const filter = (nodes: MenuItem[]): MenuItem[] =>
      nodes.flatMap((node) => {
        if (!keyword || node.menuName.includes(keyword)) return [node]
        const children = filter(node.children ?? [])
        return children.length ? [{ ...node, children }] : []
      })
    return filter(catalog.value)
  })
  /** 完整刷新目录，始终结束加载状态。 */
  async function getMenuList() {
    if (loading.value) return
    loading.value = true
    try {
      const res = await fetchMenuList()
      if (res.code !== 200) {
        message.error(res.message)
        return
      }
      if (!catalog.value.length) expandedRowKeys.value = res.data.map((node) => node.id)
      catalog.value = res.data
    } catch {
      // 请求错误由公共拦截器提示，保留旧表格。
    } finally {
      loading.value = false
    }
  }
  /** 节点保存后同步服务端用户授权和导航；页面被移除时回到首页。 */
  async function refreshNavigation() {
    await userStore.getUserInfo()
    await navigation.reloadMenus()
    await getMenuList()
  }
  /** 打开新增或修改草稿；保存失败保留表单。 */
  function openEditor(initialModel?: MenuItem, parentId: MenuId = '0') {
    openFormDialog<MenuSave>({
      title: initialModel ? '修改菜单' : '新建菜单',
      width: initialModel ? 600 : 800,
      formComponent: MenuForm,
      model: initialModel,
      extendedData: { parentId: initialModel?.parentId ?? parentId, menuData: catalog.value },
      onSubmit: async (model, dialog) => {
        const res = initialModel
          ? await updateMenu(initialModel.id, model)
          : await insertMenu(model)
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        dialog.destroy()
        message.success(res.message)
        await refreshNavigation()
      },
    })
  }
  /** 新增子节点默认使用被点击节点作为父级。 */
  function openAddDialog(parentId: MenuId = '0') {
    openEditor(undefined, parentId)
  }
  /** 修改前查询详情，避免使用过期列表数据提交。 */
  async function openEditDialog(id: MenuId) {
    const res = await fetchMenuDetail(id)
    if (res.code !== 200) {
      message.error(res.message)
      return
    }
    openEditor(res.data)
  }
  /** 删除单个无子节点的自定义节点，后端再次核验授权引用。 */
  function removeMenu(node: MenuItem) {
    openDeleteDialog({
      content: `确定删除“${node.menuName}”？`,
      onSubmit: async () => {
        const res = await deleteMenu(node.id)
        if (res.code !== 200) {
          message.error(res.message)
          throw new Error(res.message)
        }
        message.success(res.message)
        await refreshNavigation()
      },
    })
  }
  /** 展开整棵树或全部收起，不依赖第一页数据。 */
  function toggleExpand() {
    if (expandedRowKeys.value.length) {
      expandedRowKeys.value = []
      return
    }
    const keys: MenuId[] = []
    /** 收集有子节点的树节点。 */
    const collect = (nodes: MenuItem[]) => {
      for (const node of nodes)
        if (node.children?.length) {
          keys.push(node.id)
          collect(node.children)
        }
    }
    collect(catalog.value)
    expandedRowKeys.value = keys
  }
  const tableColumns: DataTableColumn[] = [
    { title: '菜单名称', key: 'menuName', align: 'center' },
    {
      title: '菜单类型',
      key: 'menuType',
      align: 'center',
      render(row: object) {
        const node = row as MenuItem
        const tagType = (['info', 'success', 'warning', 'primary'] as const)[node.menuType - 1]
        return h(
          NTag,
          { bordered: false, type: tagType },
          () => menuTypeOptions.find((option) => option.value === node.menuType)?.label,
        )
      },
    },
    {
      title: '图标',
      key: 'icon',
      align: 'center',
      render(row: object) {
        const node = row as MenuItem
        return node.icon ? createIcon(node.icon)() : '-'
      },
    },
    {
      title: '路由名称',
      key: 'routeName',
      align: 'center',
      render: (row: object) => (row as MenuItem).routeName || '-',
    },
    {
      title: '权限标识',
      key: 'perms',
      align: 'center',
      render: (row: object) => (row as MenuItem).perms || '-',
    },
    {
      title: '显示状态',
      key: 'visible',
      align: 'center',
      render(row: object) {
        const node = row as MenuItem
        return h(NTag, { bordered: false, type: node.visible === 0 ? 'success' : 'warning' }, () =>
          node.visible === 0 ? '显示' : '隐藏',
        )
      },
    },
    { title: '排序', key: 'sort', align: 'center' },
    { title: '创建时间', key: 'createTime', align: 'center' },
    {
      title: '操作',
      key: 'action',
      align: 'center',
      render(row: object) {
        const node = row as MenuItem
        return h(NSpace, { justify: 'center' }, () => [
          canCreate.value && [1, 2].includes(node.menuType)
            ? h(
                NButton,
                { text: true, type: 'primary', onClick: () => openAddDialog(node.id) },
                () => '新增',
              )
            : null,
          can('system:menu:update') && can('system:menu:detail')
            ? h(
                NButton,
                { text: true, type: 'info', onClick: () => openEditDialog(node.id) },
                () => '修改',
              )
            : null,
          can('system:menu:delete') && userStore.roleCode.includes('superadmin') && !node.builtIn
            ? h(
                NButton,
                {
                  text: true,
                  type: 'error',
                  disabled: !!node.children?.length,
                  onClick: () => removeMenu(node),
                },
                () => '删除',
              )
            : null,
        ])
      },
    },
  ]
  onMounted(getMenuList)
  /** 保持安全整数之外的主键精度。 */
  const menuRowKey = (row: MenuItem) => row.id
  /** 重置由页面维护的搜索条件，不改变原搜索栏交互。 */
  function handleReset() {
    form.menuName = ''
  }
  return {
    form,
    handleReset,
    tableData,
    tableColumns,
    loading,
    expandedRowKeys,
    canCreate,
    getMenuList,
    openAddDialog,
    toggleExpand,
    menuRowKey,
  }
}
