import { h } from 'vue'
import { NTag, type DataTableColumns } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import type { RoleUserItem } from './types'

/** 成员列表与选择用户表格共用列宽、空值回显和字典标签。 */
export function useRoleUserColumns(): DataTableColumns<RoleUserItem> {
  const dictionary = useDictStore()
  return [
    { title: '用户名', key: 'username', minWidth: 120, ellipsis: { tooltip: true } },
    { title: '姓名', key: 'name', minWidth: 100, ellipsis: { tooltip: true } },
    {
      title: '所属部门',
      key: 'deptName',
      minWidth: 140,
      ellipsis: { tooltip: true },
      render: (row) => row.deptName || '未分配部门',
    },
    {
      title: '联系电话',
      key: 'phone',
      minWidth: 140,
      ellipsis: { tooltip: true },
      render: (row) => row.phone || '—',
    },
    {
      title: '状态',
      key: 'status',
      width: 84,
      align: 'center',
      render: (row) => {
        const status = dictionary.getDictData('sys_common_status', row.status)
        return h(NTag, { bordered: false, type: status.listClass }, () => status.label)
      },
    },
  ]
}
