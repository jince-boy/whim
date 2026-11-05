import { computed, h } from 'vue'
import { NTag, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { fetchPostMemberPage } from './api'
import { useDictStore } from '@/stores/modules/dict'
import type { PostMember, PostMemberQuery, PostMembersProps } from './types'

/** 只读查询岗位成员，岗位分配由用户管理维护。 */
export function usePostMembers(props: PostMembersProps) {
  const dictionary = useDictStore()
  const departmentName = (id: string | number | null) =>
    id == null ? '未分配部门' : (props.departmentNames.get(String(id)) ?? `部门 ${id}`)

  const columns: DataTableColumns<PostMember> = [
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
  ]

  const [registerTable, table] = useWhimTable<PostMember, PostMemberQuery>(() => ({
    title: '岗位成员',
    columns,
    rowKey: (row) => String(row.userId),
    scrollX: 520,
    request: (query) =>
      fetchPostMemberPage({
        ...query,
        postId: props.post.id,
        username: query.username.trim(),
        name: query.name.trim(),
      }),
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

  return {
    registerTable,
    form: table.searchModel,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
  }
}
