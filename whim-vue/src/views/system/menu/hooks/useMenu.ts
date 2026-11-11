import { computed, h, onDeactivated, onScopeDispose, reactive, ref, watch } from 'vue'
import { NButton, NEllipsis, NFlex, NSpace, NTag, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useIcon } from '@/components/icon/useIcon'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'
import { message } from '@/plugins'
import { useDictStore } from '@/stores/modules/dict'
import { fetchMenuList, fetchMenuDetail, insertMenu, updateMenu, deleteMenu } from './api'
import {
  MenuType,
  type MenuId,
  type MenuItem,
  type MenuQuery,
  type MenuDictionaryOptions,
} from './types'
import MenuForm from '../form.vue'

/** 管理完整菜单树，查询只过滤展示，编辑始终使用完整的父级目录。 */
export function useMenu() {
  const { createIcon } = useIcon()
  const userStore = useUserStore()
  const navigation = usePermissionStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const editingId = ref<MenuId>()
  const dictionary = useDictStore()
  const menuTypeOptions = computed(() => dictionary.getDictNumberOptions('sys_menu_type'))
  const menuStatusOptions = computed(() => dictionary.getDictNumberOptions('sys_common_status'))
  const dictionaryOptions: MenuDictionaryOptions = reactive({
    menuTypes: menuTypeOptions,
    statuses: menuStatusOptions,
    visibilities: computed(() => dictionary.getDictNumberOptions('sys_visible_status')),
    cacheModes: computed(() => dictionary.getDictNumberOptions('sys_menu_cache')),
  })
  let editVersion = 0

  /** 标签文字和颜色均取自字典，未知编码保留原值便于定位配置。 */
  function renderDictionaryTag(type: string, value: number) {
    const item = dictionary.getDictData(type, value)
    return h(NTag, { bordered: false, type: item.listClass }, () => item.label)
  }

  /** 按当前用户授权显示操作，新增同时要求超级管理员角色。 */
  const can = (permission: string) =>
    userStore.permissionCode.includes('*') || userStore.permissionCode.includes(permission)
  const canView = computed(() => can('system:menu:list'))
  const canAdd = computed(
    () => can('system:menu:create') && userStore.roleCode.includes('superadmin'),
  )
  const canEdit = computed(() => can('system:menu:update') && can('system:menu:detail'))
  const canDelete = computed(() => can('system:menu:delete'))

  const columns = computed(() => {
    const columns: DataTableColumns<MenuItem> = [
      {
        title: '菜单名称',
        key: 'menuName',
        render: (row) =>
          h(
            NFlex,
            {
              inline: true,
              align: 'center',
              wrap: false,
              size: 8,
              style: 'vertical-align: middle; max-width: 100%',
            },
            () => [
              row.icon ? createIcon(row.icon)() : null,
              h(NEllipsis, { style: 'min-width: 0' }, () => row.menuName),
            ],
          ),
      },
      {
        title: '菜单类型',
        key: 'menuType',
        align: 'center',
        render: (row) => renderDictionaryTag('sys_menu_type', row.menuType),
      },
      {
        title: '路由地址',
        key: 'path',
        ellipsis: { tooltip: true },
        render: (row) => row.path || '—',
      },
      {
        title: '路由名称',
        key: 'routeName',
        ellipsis: { tooltip: true },
        render: (row) => row.routeName || '—',
      },
      {
        title: '权限标识',
        key: 'perms',
        ellipsis: { tooltip: true },
        render: (row) => row.perms || '—',
      },
      { title: '排序', key: 'sort', align: 'center' },
      {
        title: '显示状态',
        key: 'visible',
        align: 'center',
        render: (row) =>
          row.menuType === MenuType.BUTTON
            ? '—'
            : renderDictionaryTag('sys_visible_status', row.visible),
      },
      {
        title: '菜单状态',
        key: 'status',
        align: 'center',
        render: (row) => renderDictionaryTag('sys_common_status', row.status),
      },
      { title: '创建时间', key: 'createTime', ellipsis: { tooltip: true } },
    ]
    if (canAdd.value || canEdit.value || canDelete.value) {
      columns.push({
        title: '操作',
        key: 'action',
        fixed: 'right',
        render: (row) => {
          const node = catalog.value.byId.get(String(row.id))!
          const deleteReason =
            row.builtIn === 1 && row.perms === 'system:home:view'
              ? '系统内置首页不能删除'
              : row.protectedNode
                ? '核心管理节点不能删除'
                : node.children?.length
                  ? '请先删除所有子节点'
                  : undefined
          return h(NSpace, { size: 12, wrap: false }, () => [
            canAdd.value && (row.menuType === MenuType.DIRECTORY || row.menuType === MenuType.MENU)
              ? h(
                  NButton,
                  { text: true, type: 'primary', onClick: () => openAddDialog(row) },
                  () => '新增',
                )
              : null,
            canEdit.value
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'warning',
                    loading: editingId.value === row.id,
                    disabled: editingId.value !== undefined,
                    onClick: () => openEditDialog(row.id),
                  },
                  () => '修改',
                )
              : null,
            canDelete.value
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'error',
                    disabled: !!deleteReason,
                    title: deleteReason,
                    onClick: () => removeMenu(row),
                  },
                  () => '删除',
                )
              : null,
          ])
        },
      })
    }
    return columns
  })

  const [registerTable, table] = useWhimTable<MenuItem, MenuQuery>(() => ({
    title: '菜单管理',
    columns,
    rowKey: (row) => row.id,
    scrollX: 1460,
    pagination: false,
    immediate: canView.value,
    request: () => fetchMenuList(),
    search: {
      defaultValues: { menuName: '', menuType: null, status: null },
      fields: [
        { key: 'menuName', label: '菜单名称' },
        {
          key: 'menuType',
          label: '菜单类型',
          format: (query) =>
            query.menuType === null ? '' : dictionary.getDictData('sys_menu_type', query.menuType).label,
        },
        {
          key: 'status',
          label: '菜单状态',
          format: (query) =>
            query.status === null ? '' : dictionary.getDictData('sys_common_status', query.status).label,
        },
      ],
    },
    toolbar: {
      buttons: canAdd.value ? ['add'] : [],
      permissions: { add: ['system:menu:create'] },
    },
    /** 保留匹配节点的祖先；名称命中目录时，其子树继续参与类型和状态筛选。 */
    transformData: (nodes, query) => {
      const keyword = query.menuName.trim().toLocaleLowerCase()
      if (!keyword && query.menuType === null && query.status === null) return nodes
      /** 逐层筛选节点，祖先只承担展示层级的作用。 */
      function filter(nodes: MenuItem[], ancestorMatches = false): MenuItem[] {
        return nodes.flatMap((node) => {
          const matchesName = ancestorMatches || node.menuName.toLocaleLowerCase().includes(keyword)
          const children = filter(node.children ?? [], matchesName)
          const matches =
            matchesName &&
            (query.menuType === null || node.menuType === query.menuType) &&
            (query.status === null || node.status === query.status)
          return matches || children.length ? [{ ...node, children }] : []
        })
      }
      return filter(nodes)
    },
  }))

  const catalog = computed(() => {
    const byId = new Map<string, MenuItem>()
    const expandableKeys: MenuId[] = []
    /** 索引完整菜单及可展开节点，搜索后的操作限制仍按完整目录判断。 */
    function collect(nodes: MenuItem[]) {
      for (const node of nodes) {
        byId.set(String(node.id), node)
        if (!node.children?.length) continue
        expandableKeys.push(node.id)
        collect(node.children)
      }
    }
    collect(table.data.value)
    return { byId, expandableKeys }
  })
  const expandableKeys = computed(() => catalog.value.expandableKeys)
  const allExpanded = computed(() => {
    const expanded = new Set(table.getExpandedRowKeys())
    return expandableKeys.value.length > 0 && expandableKeys.value.every((key) => expanded.has(key))
  })

  /** 保存成功后更新当前授权、导航和管理树。 */
  async function refreshNavigation() {
    await userStore.getUserInfo()
    await navigation.reloadMenus()
    await table.reload()
  }

  /** 新增和修改共用表单，行内新增时预选父菜单。 */
  function openEditor(initialModel?: MenuItem, parentMenu?: MenuItem) {
    openFormDialog({
      title: initialModel ? '修改菜单' : '新增菜单',
      width: 760,
      formComponent: MenuForm,
      formProps: { initialModel, parentMenu, menus: table.data.value, dictionaryOptions },
      onSubmit: (model) => (initialModel ? updateMenu(initialModel.id, model) : insertMenu(model)),
      onSuccess: refreshNavigation,
    })
  }

  /** 在顶级或指定父菜单下新增节点。 */
  function openAddDialog(parentMenu?: MenuItem) {
    openEditor(undefined, parentMenu)
  }

  /** 读取最新详情，离开页面或重复点击时不再打开旧弹窗。 */
  async function openEditDialog(id: MenuId) {
    if (editingId.value !== undefined) return
    const version = ++editVersion
    editingId.value = id
    try {
      const response = await fetchMenuDetail(id)
      if (version !== editVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      // 详情接口只返回当前节点，子节点约束取自完整管理树。
      openEditor({ ...response.data, children: catalog.value.byId.get(String(id))?.children })
    } catch {
      // 请求错误由公共拦截器提示。
    } finally {
      if (version === editVersion) editingId.value = undefined
    }
  }

  /** 删除叶子节点，关联授权由后端同步清理。 */
  function removeMenu(node: MenuItem) {
    openDeleteDialog({
      content: `确定删除“${node.menuName}”？关联的角色授权也会一并清理。`,
      onSubmit: () => deleteMenu(node.id),
      onSuccess: refreshNavigation,
    })
  }

  /** 部分展开时继续展开全部，全部展开后再统一收起。 */
  function toggleExpand() {
    table.setExpandedRowKeys(allExpanded.value ? [] : expandableKeys.value)
  }

  watch([table.data, table.submittedQuery], ([nodes, query], [previous]) => {
    if (query.menuName.trim() || query.menuType !== null || query.status !== null) {
      table.setExpandedRowKeys(expandableKeys.value)
    } else if (!previous.length) {
      table.setExpandedRowKeys(nodes.filter((node) => node.children?.length).map((node) => node.id))
    }
  })
  onDeactivated(() => {
    editVersion++
    editingId.value = undefined
  })
  onScopeDispose(() => editVersion++)

  return {
    registerTable,
    form: table.searchModel,
    canView,
    allExpanded,
    expandableKeys,
    menuTypeOptions,
    menuStatusOptions,
    openAddDialog,
    toggleExpand,
  }
}
