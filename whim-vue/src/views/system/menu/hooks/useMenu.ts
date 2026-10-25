import { useWhimTable } from '@/components/table'
import { computed, h, watch } from 'vue'
import { NButton, NSpace, NTag, useMessage, type DataTableColumn } from 'naive-ui'
import { fetchMenuList, fetchMenuDetail, insertMenu, updateMenu, deleteMenu } from './api'
import { menuTypeOptions, type MenuId, type MenuItem, type MenuQuery } from './types'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'
import { useFormDialog } from '@/components/dialog'
import MenuForm from '../form.vue'
import { useIcon } from '@/components/icon/useIcon'

/** 菜单管理使用完整目录作为编辑基准，搜索只改变表格展示，不截断父节点选项。 */
export function useMenu() {
  const { createIcon } = useIcon()
  const userStore = useUserStore()
  const navigation = usePermissionStore()
  const message = useMessage()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  /** 表格操作权限随当前授权变化，不依赖一次性的DOM删除。 */
  const can = (code: string) =>
    userStore.permissionCode.includes(code) || userStore.permissionCode.includes('*')
  const canCreate = computed(
    () => can('system:menu:create') && userStore.roleCode.includes('superadmin'),
  )
  /** 使用表格统一刷新流程和已提交的查询条件。 */
  async function getMenuList() {
    await table.reload()
  }
  /** 节点保存后同步服务端用户授权和导航；页面被移除时回到首页。 */
  async function refreshNavigation() {
    await userStore.getUserInfo()
    await navigation.reloadMenus()
    await getMenuList()
  }
  /** 打开新增或修改草稿；保存失败保留表单。 */
  function openEditor(initialModel?: MenuItem, parentId: MenuId = '0') {
    openFormDialog({
      title: initialModel ? '修改菜单' : '新建菜单',
      width: 800,
      formComponent: MenuForm,
      formProps: {
        initialModel,
        extendedData: { parentId: initialModel?.parentId ?? parentId, menuData: catalog.value },
      },
      onSubmit: (model) => (initialModel ? updateMenu(initialModel.id, model) : insertMenu(model)),
      onSuccess: refreshNavigation,
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
  /** 删除叶子节点，并由后端同步清理角色授权。 */
  function removeMenu(node: MenuItem) {
    openDeleteDialog({
      content: `确定删除“${node.menuName}”？关联的角色授权也会一并清理。`,
      onSubmit: () => deleteMenu(node.id),
      onSuccess: refreshNavigation,
    })
  }
  /** 展开整棵树或全部收起，不依赖第一页数据。 */
  function toggleExpand() {
    if (table.getExpandedRowKeys().length) {
      table.setExpandedRowKeys([])
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
    table.setExpandedRowKeys(keys)
  }
  const tableColumns: DataTableColumn<MenuItem>[] = [
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
        const homeMenu = node.builtIn === 1 && node.perms === 'system:home:view'
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
          can('system:menu:delete')
            ? h(
                NButton,
                {
                  text: true,
                  type: 'error',
                  disabled: homeMenu || node.protectedNode || !!node.children?.length,
                  title: homeMenu
                    ? '系统内置首页不能删除'
                    : node.protectedNode
                      ? '核心管理节点不能删除'
                      : node.children?.length
                        ? '请先删除所有子节点'
                        : '删除菜单及关联授权',
                  onClick: () => removeMenu(node),
                },
                () => '删除',
              )
            : null,
        ])
      },
    },
  ]
  const [registerTable, table] = useWhimTable<MenuItem, MenuQuery>(() => ({
    columns: tableColumns,
    rowKey: (row) => row.id,
    pagination: false,
    search: { defaultValues: { menuName: '' } },
    toolbar: {
      buttons: canCreate.value ? ['add'] : [],
      permissions: { add: ['system:menu:create'] },
      showStriped: false,
      showRefresh: true,
      showColumns: true,
      showFullscreen: true,
    },
    // 编辑需要完整父菜单目录，搜索条件只用于下面的展示过滤。
    request: () => fetchMenuList(),
    /** 仅转换展示树，搜索结果保留祖先和匹配目录的子树。 */
    transformData: (nodes, query) => {
      const keyword = query.menuName.trim()
      /** 递归筛选菜单树。 */
      const filter = (nodes: MenuItem[]): MenuItem[] =>
        nodes.flatMap((node) => {
          if (!keyword || node.menuName.includes(keyword)) return [node]
          const children = filter(node.children ?? [])
          return children.length ? [{ ...node, children }] : []
        })
      return filter(nodes)
    },
  }))
  // 编辑始终读取完整目录，搜索不会截断父节点候选项。
  const catalog = table.data
  watch(catalog, (nodes, previous) => {
    if (!previous.length) table.setExpandedRowKeys(nodes.map((node) => node.id))
  })
  return {
    registerTable,
    form: table.searchModel,
    getMenuList,
    openAddDialog,
    toggleExpand,
  }
}
