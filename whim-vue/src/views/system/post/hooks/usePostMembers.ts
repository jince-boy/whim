import { computed, h, ref, watch, type ComputedRef, type Ref } from 'vue'
import { NButton, NTag, type DataTableColumns } from 'naive-ui'
import type { DataTableRowKey } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { addPostMembers, deletePostMembers, fetchPostMemberPage } from './api'
import { useDictStore } from '@/stores/modules/dict'
import type { PostMember, PostMemberQuery, PostMembersProps } from './types'

/** 成员与候选用户独立分页，批量操作只使用当前明确选择的用户。 */
export function usePostMembers(props: PostMembersProps, busy: Ref<boolean>) {
  const dictionary = useDictStore()
  const user = useUserStore()
  const { openConfirmDialog } = useFormDialog()
  const assigned = ref(true)
  const canAssign = computed(
    () =>
      user.permissionCode.includes('*') ||
      user.permissionCode.includes('system:post:member:assign'),
  )
  const canAdd = computed(() => canAssign.value && props.post.status === 0)
  const departmentName = (id: string | number) =>
    props.departmentNames.get(String(id)) ?? `部门 ${id}`

  const columns: ComputedRef<DataTableColumns<PostMember>> = computed(() => [
    ...(canAssign.value
      ? [
          {
            type: 'selection' as const,
            width: 48,
            disabled: () => busy.value || table.loading.value,
          },
        ]
      : []),
    { title: '用户名', key: 'username', minWidth: 130, ellipsis: { tooltip: true } },
    { title: '姓名', key: 'name', minWidth: 110, ellipsis: { tooltip: true } },
    { title: '主部门', key: 'deptId', minWidth: 140, render: (row) => departmentName(row.deptId) },
    {
      title: '用户状态',
      key: 'status',
      width: 100,
      render: (row) => {
        const status = dictionary.getDictData('sys_common_status', row.status)
        return h(NTag, { bordered: false, type: status.listClass }, () => status.label)
      },
    },
    ...(canAssign.value
      ? [
          {
            title: '操作',
            key: 'action',
            width: 90,
            fixed: 'right' as const,
            render: (row: PostMember) =>
              h(
                NButton,
                {
                  text: true,
                  type: assigned.value ? 'error' : 'primary',
                  disabled: busy.value || table.loading.value || (!assigned.value && !canAdd.value),
                  onClick: () => changeMembers([row.userId]),
                },
                () => (assigned.value ? '移除' : '添加'),
              ),
          },
        ]
      : []),
  ])

  const [registerTable, table] = useWhimTable<PostMember, PostMemberQuery>(() => ({
    title: assigned.value ? '已分配成员' : '可添加用户',
    columns,
    rowKey: (row) => String(row.userId),
    scrollX: 650,
    request: (query) =>
      fetchPostMemberPage(props.post.id, {
        ...query,
        username: query.username.trim(),
        name: query.name.trim(),
        assigned: assigned.value,
        status: assigned.value ? query.status : null,
      }),
    onUpdatePage: () => clearSelection(),
    onUpdatePageSize: () => clearSelection(),
    search: {
      defaultValues: { username: '', name: '', deptId: null, status: null },
      fields: [
        { key: 'username', label: '用户名' },
        { key: 'name', label: '姓名' },
        {
          key: 'deptId',
          label: '主部门',
          format: (query) => (query.deptId ? departmentName(query.deptId) : ''),
        },
        {
          key: 'status',
          label: '用户状态',
          format: (query) =>
            query.status === null ? '' : dictionary.getDictData('sys_common_status', query.status).label,
        },
      ],
    },
    toolbar: { showFullscreen: false },
  }))
  const selectedCount = computed(() => table.getCheckedRowKeys().length)

  function clearSelection() {
    table.setCheckedRowKeys([])
  }

  function changeMembers(ids: DataTableRowKey[]) {
    if (
      !ids.length ||
      busy.value ||
      table.loading.value ||
      !canAssign.value ||
      (!assigned.value && !canAdd.value)
    )
      return
    const removing = assigned.value
    const snapshot = [...ids]
    openConfirmDialog({
      title: removing ? '移除岗位成员' : '添加岗位成员',
      content: `确定${removing ? '从' : '向'}“${props.post.postName}”${removing ? '移除' : '添加'}选中的 ${snapshot.length} 名用户？其他成员保持不变，变更用户需要重新登录。`,
      modalProps: { type: removing ? 'warning' : 'info' },
      onSubmit: async () => {
        busy.value = true
        try {
          return await (removing
            ? deletePostMembers(props.post.id, snapshot)
            : addPostMembers(props.post.id, snapshot))
        } finally {
          busy.value = false
        }
      },
      onSuccess: async () => {
        clearSelection()
        await table.reload()
      },
    })
  }

  function changeSelectedMembers() {
    changeMembers(table.getCheckedRowKeys())
  }
  watch(assigned, () => {
    clearSelection()
    void table.resetSearch()
  })
  watch([table.data, table.submittedQuery], () => {
    clearSelection()
  })

  return {
    registerTable,
    form: table.searchModel,
    assigned,
    canAssign,
    canAdd,
    selectedCount,
    loading: table.loading,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    changeSelectedMembers,
  }
}
