import { deleteMenu, fetchMenuDetail, fetchMenuList, insertMenu, updateMenu } from './api'
import { type DataTableColumn, NButton, NSpace, NTag, useMessage } from 'naive-ui'
import type { Menu, MenuItem } from './types'
import { useIcon } from '@/components/icon/useIcon.ts'
import { useDict } from '@/components/dict/useDict.ts'
import { useFormDialog } from '@/components/dialog/useFormDialog.ts'
import editForm from '@/views/system/menu/form.vue'

const { dictData, getDictData } = await useDict('sys_menu_type')
const { dictData: sysShowStatusDict, getDictData: getSysShowStatusDict } =
  await useDict('sys_show_status')
const { dictData: sysRunStatusDict } = await useDict('sys_run_status')
const { dictData: sysMenuKeepAliveStatus } = await useDict('sys_menu_keepAlive_status')

export function useMenu() {
  const { createIcon } = useIcon()
  const message = useMessage()
  const { openFormDialog, openDeleteDialog } = useFormDialog()

  const form = reactive({
    title: '',
  })

  const tableData = ref<MenuItem[]>([])

  const tableColumns: DataTableColumn[] = [
    {
      title: '菜单名称',
      key: 'title',
      align: 'center',
    },
    {
      title: '菜单类型',
      key: 'type',
      align: 'center',
      render(row: object) {
        const menu = row as MenuItem
        const dictData = getDictData(String(menu.type))
        return h(
          NTag,
          {
            bordered: false,
            type: dictData!.listClass,
          },
          { default: () => dictData!.label },
        )
      },
    },
    {
      title: '图标',
      key: 'icon',
      align: 'center',
      render(row: object) {
        const menu = row as MenuItem
        if (!menu.icon) {
          return '-'
        }
        return createIcon(menu.icon)()
      },
    },
    {
      title: '路由名称',
      key: 'name',
      align: 'center',
      render(row: object) {
        const menu = row as MenuItem
        return menu.name || '-'
      },
    },
    {
      title: '权限标识',
      key: 'code',
      align: 'center',
      render(row: object) {
        const menu = row as MenuItem
        return menu.code || '-'
      },
    },
    {
      title: '显示状态',
      key: 'visible',
      align: 'center',
      render(row: object) {
        const menu = row as MenuItem
        const dictData = getSysShowStatusDict(String(menu.visible))
        return h(
          NTag,
          {
            bordered: false,
            type: dictData!.listClass,
          },
          { default: () => dictData!.label },
        )
      },
    },
    {
      title: '排序',
      key: 'sort',
      align: 'center',
    },
    {
      title: '创建时间',
      key: 'createTime',
      align: 'center',
    },
    {
      title: '操作',
      key: 'action',
      align: 'center',
      render(row: object) {
        const menu = row as MenuItem
        return h(NSpace, { justify: 'center' }, () => [
          h(
            NButton,
            {
              text: true,
              type: 'primary',
              onClick: async () => {
                openAddDialog(menu.id)
              },
            },
            { default: () => '新增' },
          ),
          h(
            NButton,
            {
              text: true,
              type: 'info',
              onClick: async () => {
                await openEditDialog(menu.id)
              },
            },
            { default: () => '修改' },
          ),
          h(
            NButton,
            {
              text: true,
              type: 'error',
              onClick: () => {
                removeMenu(menu)
              },
            },
            { default: () => '删除' },
          ),
        ])
      },
    },
  ]

  const loading = ref(false)

  // 展开菜单的键
  const expandedRowKeys = ref<Array<number | string>>([])
  /**
   * 获取菜单详情
   * @param id
   */
  const getMenuDetail = async (id: string): Promise<Menu> => {
    const res = await fetchMenuDetail({ id })
    return res.data as Menu
  }

  /**
   * 新增菜单
   * @param parentId
   */
  const openAddDialog = (parentId?: string) => {
    openFormDialog<Menu>({
      title: '新建菜单',
      width: 800,
      formComponent: editForm,
      extendedData: {
        parentId: parentId ?? '0',
        menuTypeDict: dictData,
        sysShowStatusDict: sysShowStatusDict,
        sysRunStatusDict: sysRunStatusDict,
        sysMenuKeepAliveStatus: sysMenuKeepAliveStatus,
        menuData: tableData.value,
      },
      onSubmit: async (formModel, dialog) => {
        return insertMenu(formModel).then((res) => {
          if (res.code == 200) {
            dialog.destroy()
            message.success(`${dialog.title}成功`)
            getMenuList()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }
  /**
   * 修改菜单
   * @param id
   */
  const openEditDialog = async (id: string) => {
    const data = await getMenuDetail(id)
    console.log(data)
    openFormDialog<Menu>({
      title: '修改菜单',
      formComponent: editForm,
      model: data,
      extendedData: {
        menuTypeDict: dictData,
        sysShowStatusDict: sysShowStatusDict,
        sysRunStatusDict: sysRunStatusDict,
        sysMenuKeepAliveStatus: sysMenuKeepAliveStatus,
        menuData: tableData.value,
      },
      onSubmit: async (formModel, dialog) => {
        return updateMenu(formModel).then((res) => {
          if (res.code == 200) {
            dialog.destroy()
            message.success(`${dialog.title}成功`)
            getMenuList()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }
  /**
   * 删除菜单
   * @param row 菜单
   */
  const removeMenu = (row: MenuItem) => {
    openDeleteDialog({
      content: `确定要删除${row.title}吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteMenu({ menuIds: [row.id] }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getMenuList()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }
  /**
   * 获取菜单列表
   */
  const getMenuList = () => {
    loading.value = true
    fetchMenuList(form).then((res) => {
      tableData.value = res.data
      loading.value = false
      expandedRowKeys.value = res.data.map((item) => item.id)
    })
  }

  /** 接收搜索表单的重置事件，由页面恢复查询条件。 */
  const handleReset = (values: Record<string, unknown>) => {
    Object.assign(form, values)
  }

  onMounted(() => {
    getMenuList()
  })
  return {
    form,
    handleReset,
    tableData,
    loading,
    tableColumns,
    getMenuList,
    expandedRowKeys,
    openAddDialog,
  }
}
