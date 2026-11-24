import { computed, h } from 'vue'
import { NSpace, NTag, type DataTableColumns } from 'naive-ui'
import { TableAction, useDataTable } from '@/components/dataTable'
import { useFormDialog } from '@/components/formDialog'
import { useDictStore } from '@/stores/modules/dict'
import { usePermissionStore } from '@/stores/modules/permission'
import { useUserStore } from '@/stores/modules/user'
import HomePageForm from '../form.vue'
import { addHomePage, deleteHomePage, fetchHomePageConfigs, updateHomePage } from './api'
import type { HomePageQuery, HomePageRow } from './types'

export function useHomePage() {
  const dictionary = useDictStore()
  const navigation = usePermissionStore()
  const user = useUserStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const can = (action: string) => user.hasPermission(`system:homePage:${action}`)
  const label = (type: string, value: number | null) =>
    value == null ? '' : dictionary.getDictData(type, value).label
  const columns = computed<DataTableColumns<HomePageRow>>(() => {
    const result: DataTableColumns<HomePageRow> = [
      {
        title: '关联类型',
        key: 'targetType',
        render: (row) => label('sys_home_target_type', row.targetType),
      },
      {
        title: '关联对象',
        key: 'targetName',
        render: (row) =>
          row.targetType === 4 ? '—' : (row.targetName ?? `已删除（${row.targetId}）`),
      },
      { title: '首页路由', key: 'path', ellipsis: { tooltip: true } },
      { title: '页面组件', key: 'component', ellipsis: { tooltip: true } },
      {
        title: '角色优先级',
        key: 'priority',
        render: (row) => (row.targetType === 2 ? row.priority : '—'),
      },
      {
        title: '状态',
        key: 'status',
        render: (row) => {
          const status = dictionary.getDictData('sys_common_status', row.status)
          return h(NTag, { type: status.listClass, bordered: false }, () => status.label)
        },
      },
    ]
    if (can('update') || can('delete')) {
      result.push({
        title: '操作',
        key: 'action',
        fixed: 'right',
        width: 100,
        render: (row) =>
          h(NSpace, { size: 6, wrap: false }, () => [
            can('update')
              ? h(TableAction, {
                  label: '修改',
                  icon: 'pencil',
                  type: 'warning',
                  onClick: () => editHome(row),
                })
              : null,
            can('delete')
              ? h(TableAction, {
                  label: '删除',
                  icon: 'trash-2',
                  type: 'error',
                  onClick: () => removeHome(row),
                })
              : null,
          ]),
      })
    }
    return result
  })
  const [registerTable, table] = useDataTable<HomePageRow, HomePageQuery>(() => ({
    title: '首页配置',
    columns,
    rowKey: (row) => String(row.id),
    scrollX: 930,
    immediate: can('page'),
    request: fetchHomePageConfigs,
    search: {
      defaultValues: { targetType: null, status: null },
      fields: [
        {
          key: 'targetType',
          label: '关联类型',
          format: (query) => label('sys_home_target_type', query.targetType),
        },
        {
          key: 'status',
          label: '状态',
          format: (query) => label('sys_common_status', query.status),
        },
      ],
    },
    toolbar: { buttons: ['add'], permissions: { add: ['system:homePage:add'] } },
  }))

  async function refresh() {
    await Promise.all([table.reload(), navigation.reloadMenus()])
  }

  function editHome(initialModel?: HomePageRow) {
    openFormDialog({
      title: initialModel ? '修改首页配置' : '新增首页配置',
      width: 600,
      formComponent: HomePageForm,
      formProps: { initialModel },
      onSubmit: initialModel ? updateHomePage : addHomePage,
      onSuccess: refresh,
    })
  }

  function removeHome(row: HomePageRow) {
    openDeleteDialog({
      content: `确定删除指向“${row.path}”的首页配置？受影响的账号将使用下一条匹配配置。`,
      onSubmit: () => deleteHomePage(row.id),
      onSuccess: refresh,
    })
  }

  return {
    registerTable,
    form: table.searchModel,
    editHome,
    canView: computed(() => can('page')),
    typeOptions: computed(() => dictionary.getDictNumberOptions('sys_home_target_type')),
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
  }
}
