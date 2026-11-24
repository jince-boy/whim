import { computed, h, onActivated, onDeactivated, onScopeDispose, ref, watch } from 'vue'
import { NSpace, NTag, type DataTableColumns } from 'naive-ui'
import { TableAction, useDataTable } from '@/components/dataTable'
import { useFormDialog } from '@/components/formDialog'
import { useUserStore } from '@/stores/modules/user'
import { message } from '@/plugins'
import { addDept, deleteDept, fetchDeptDetail, fetchDeptTree, updateDept } from './api'
import { useDictStore } from '@/stores/modules/dict'
import type { DeptId, DeptItem, DeptQuery } from './types'
import DeptForm from '../form.vue'

/** 部门管理树、状态展示和新增修改流程。 */
export function useDept() {
  const dictionary = useDictStore()
  const user = useUserStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const can = (permission: string) => user.hasPermission(permission)
  const canView = computed(() => can('system:dept:list'))
  const editingId = ref<DeptId>()
  let recordVersion = 0
  let active = true

  const columns = computed<DataTableColumns<DeptItem>>(() => [
    { title: '部门名称', key: 'deptName', ellipsis: { tooltip: true } },
    { title: '部门编码', key: 'deptCode', ellipsis: { tooltip: true } },
    { title: '排序', key: 'sort', align: 'center' },
    {
      title: '状态',
      key: 'status',
      align: 'center',
      render: (row) => {
        const status = dictionary.getDictData('sys_common_status', row.status)
        return h(NTag, { bordered: false, type: status.listClass }, () => status.label)
      },
    },
    { title: '创建时间', key: 'createTime' },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      render: (row) =>
        h(NSpace, { size: 6, wrap: false }, () => [
          can('system:dept:create')
            ? h(TableAction, {
                label: '新增',
                icon: 'plus',
                type: 'primary',
                onClick: () => openAddDialog(row),
              })
            : null,
          can('system:dept:update')
            ? h(TableAction, {
                label: '修改',
                icon: 'pencil',
                type: 'warning',
                loading: editingId.value === row.id,
                disabled: editingId.value !== undefined,
                onClick: () => openEditDialog(row.id),
              })
            : null,
          can('system:dept:delete')
            ? h(TableAction, {
                label: '删除',
                icon: 'trash-2',
                type: 'error',
                onClick: () => removeDept(row),
              })
            : null,
        ]),
    },
  ])

  const [registerTable, table] = useDataTable<DeptItem, DeptQuery>(() => ({
    title: '部门管理',
    columns,
    rowKey: (row) => String(row.id),
    scrollX: 1080,
    pagination: false,
    immediate: canView.value,
    request: ({ deptName, status }) => fetchDeptTree({ deptName: deptName.trim(), status }),
    search: {
      defaultValues: { deptName: '', status: null },
      fields: [
        { key: 'deptName', label: '部门名称' },
        {
          key: 'status',
          label: '状态',
          format: (query) =>
            query.status === null
              ? ''
              : dictionary.getDictData('sys_common_status', query.status).label,
        },
      ],
    },
    toolbar: { buttons: ['add'], permissions: { add: ['system:dept:create'] } },
  }))
  const expandableKeys = computed(() => {
    const keys: string[] = []
    function collect(nodes: DeptItem[]) {
      for (const node of nodes) {
        if (node.children.length) {
          keys.push(String(node.id))
          collect(node.children)
        }
      }
    }
    collect(table.data.value)
    return keys
  })
  const allExpanded = computed(() => {
    const expanded = new Set(table.getExpandedRowKeys())
    return expandableKeys.value.length > 0 && expandableKeys.value.every((key) => expanded.has(key))
  })

  function openAddDialog(parentDept?: DeptItem) {
    openFormDialog({
      title: parentDept ? `新增下级部门 · ${parentDept.deptName}` : '新增部门',
      width: 640,
      formComponent: DeptForm,
      formProps: { parentDept },
      onSubmit: addDept,
      onSuccess: table.reload,
    })
  }

  /** 编辑前读取最新数据，离开页面后丢弃异步结果。 */
  async function openEditDialog(id: DeptId) {
    if (editingId.value !== undefined) return
    const version = ++recordVersion
    editingId.value = id
    try {
      const response = await fetchDeptDetail(id)
      if (version !== recordVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: '修改部门',
        width: 640,
        formComponent: DeptForm,
        formProps: { initialModel: response.data },
        onSubmit: updateDept,
        onSuccess: table.reload,
      })
    } catch {
      // 公共请求层提示错误。
    } finally {
      if (version === recordVersion) editingId.value = undefined
    }
  }

  function removeDept(row: DeptItem) {
    openDeleteDialog({
      content: `确定删除“${row.deptName}”？存在下级部门、用户、岗位或角色数据范围引用时无法删除。`,
      onSubmit: () => deleteDept(row.id),
      onSuccess: table.reload,
    })
  }

  function toggleExpand() {
    table.setExpandedRowKeys(allExpanded.value ? [] : expandableKeys.value)
  }
  watch(table.data, () => table.setExpandedRowKeys(expandableKeys.value))
  onDeactivated(() => {
    active = false
    recordVersion++
    editingId.value = undefined
  })
  onActivated(() => {
    if (!active) {
      active = true
      void table.reload()
    }
  })
  onScopeDispose(() => recordVersion++)

  return {
    registerTable,
    form: table.searchModel,
    canView,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    expandableKeys,
    allExpanded,
    openAddDialog,
    toggleExpand,
  }
}
