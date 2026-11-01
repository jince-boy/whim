import { computed, h, onActivated, onDeactivated, onScopeDispose, ref, watch } from 'vue'
import { NButton, NSpace, NSwitch, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { message } from '@/plugins'
import {
  addDept,
  deleteDept,
  fetchDeptDetail,
  fetchDeptTree,
  updateDept,
  updateDeptStatus,
} from './api'
import { organizationStatusOptions, type DeptId, type DeptItem, type DeptQuery } from './types'
import DeptForm from '../form.vue'

/** 部门管理树、独立状态操作和新增修改流程。 */
export function useDept() {
  const user = useUserStore()
  const { openFormDialog, openConfirmDialog, openDeleteDialog } = useFormDialog()
  const can = (permission: string) =>
    user.permissionCode.includes('*') || user.permissionCode.includes(permission)
  const canView = computed(() => can('system:dept:list'))
  const recordLoading = ref(false)
  let recordVersion = 0
  let active = true

  const columns = computed<DataTableColumns<DeptItem>>(() => [
    { title: '部门名称', key: 'deptName', minWidth: 240, ellipsis: { tooltip: true } },
    { title: '部门编码', key: 'deptCode', minWidth: 180, ellipsis: { tooltip: true } },
    { title: '排序', key: 'sort', width: 80, align: 'center' },
    {
      title: '状态',
      key: 'status',
      width: 100,
      align: 'center',
      render: (row) =>
        h(
          NSwitch,
          {
            value: row.status,
            checkedValue: 0,
            uncheckedValue: 1,
            disabled: !can('system:dept:status'),
            'aria-label': `${row.deptName}状态`,
            onUpdateValue: () => changeStatus(row),
          },
          { checked: () => '正常', unchecked: () => '停用' },
        ),
    },
    { title: '创建时间', key: 'createTime', width: 180 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (row) =>
        h(NSpace, { size: 12, wrap: false }, () => [
          can('system:dept:create')
            ? h(
                NButton,
                {
                  text: true,
                  type: 'primary',
                  onClick: () => openAddDialog(row),
                },
                () => '新增',
              )
            : null,
          can('system:dept:update')
            ? h(
                NButton,
                {
                  text: true,
                  type: 'warning',
                  disabled: recordLoading.value,
                  onClick: () => openEditDialog(row.id),
                },
                () => '修改',
              )
            : null,
          can('system:dept:delete')
            ? h(
                NButton,
                { text: true, type: 'error', onClick: () => removeDept(row) },
                () => '删除',
              )
            : null,
        ]),
    },
  ])

  const [registerTable, table] = useWhimTable<DeptItem, DeptQuery>(() => ({
    title: '部门管理',
    columns,
    rowKey: (row) => String(row.id),
    scrollX: 960,
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
            organizationStatusOptions.find((item) => item.value === query.status)?.label ?? '',
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
    if (recordLoading.value) return
    const version = ++recordVersion
    recordLoading.value = true
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
      if (version === recordVersion) recordLoading.value = false
    }
  }

  function changeStatus(row: DeptItem) {
    const status = row.status === 0 ? 1 : 0
    openConfirmDialog({
      title: status === 1 ? '停用部门' : '启用部门',
      modalProps: { type: status === 0 ? 'success' : 'warning' },
      content:
        status === 1
          ? `确定停用“${row.deptName}”？其下级分支也将停止参与授权，相关用户需要重新登录。`
          : `确定启用“${row.deptName}”？上级部门必须正常，下级部门保留各自的启停设置。`,
      onSubmit: () => updateDeptStatus({ id: row.id, status }),
      onSuccess: table.reload,
    })
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
    recordLoading.value = false
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
    statusOptions: organizationStatusOptions,
    expandableKeys,
    allExpanded,
    openAddDialog,
    toggleExpand,
  }
}
