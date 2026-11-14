import { computed, h, onActivated, onDeactivated, onScopeDispose, ref } from 'vue'
import { NButton, NSpace, NSwitch, NTag, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useAuthStore } from '@/stores/modules/auth'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import {
  addRole,
  assignRoleDataScope,
  assignRolePermissions,
  deleteRole,
  fetchRoleDataScope,
  fetchRoleDetail,
  fetchRolePage,
  fetchRolePermissions,
  updateRole,
  updateRoleStatus,
} from './api'
import type { RoleId, RoleItem, RoleQuery, RoleStatus } from './types'
import RoleForm from '../form.vue'
import PermissionForm from '../PermissionForm.vue'
import DataScopeForm from '../DataScopeForm.vue'

/** 角色管理按菜单权限码显示操作，所有角色采用相同的维护规则。 */
export function useRole() {
  const user = useUserStore()
  const auth = useAuthStore()
  const dictionary = useDictStore()
  const { openFormDialog, openDeleteDialog, openConfirmDialog } = useFormDialog()
  const can = (permission: string) => user.hasPermission(permission)
  const canView = computed(() => can('system:role:list'))
  const loadingRecord = ref(false)
  const statusRoleId = ref<RoleId>()
  const statusSubmitting = ref(false)
  let recordVersion = 0
  let active = true

  const columns = computed<DataTableColumns<RoleItem>>(() => {
    const columns: DataTableColumns<RoleItem> = [
      { title: '角色名称', key: 'roleName', ellipsis: { tooltip: true } },
      { title: '角色编码', key: 'roleCode', ellipsis: { tooltip: true } },
      {
        title: '数据范围',
        key: 'dataScope',
        align: 'center',
        render: (row) => {
          const scope = dictionary.getDictData('sys_role_data_scope', row.dataScope)
          return h(
            NTag,
            { size: 'small', bordered: false, type: scope.listClass },
            () => scope.label,
          )
        },
      },
      { title: '排序', key: 'sort', align: 'center' },
      {
        title: '状态',
        key: 'status',
        align: 'center',
        render: (row) => {
          const status = dictionary.getDictData('sys_common_status', row.status)
          if (!can('system:role:status'))
            return h(
              NTag,
              { size: 'small', bordered: false, type: status.listClass },
              () => status.label,
            )
          return h(
            NSwitch,
            {
              value: row.status,
              checkedValue: 0,
              uncheckedValue: 1,
              size: 'small',
              disabled: statusRoleId.value !== undefined,
              loading: statusSubmitting.value && String(statusRoleId.value) === String(row.id),
              'aria-label': `${row.roleName}的状态`,
              onUpdateValue: (value) => {
                if (value === 0 || value === 1) confirmStatus(row, value)
              },
            },
            {
              checked: () => dictionary.getDictData('sys_common_status', 0).label,
              unchecked: () => dictionary.getDictData('sys_common_status', 1).label,
            },
          )
        },
      },
      { title: '创建时间', key: 'createTime', ellipsis: { tooltip: true } },
    ]
    const canEdit = can('system:role:detail') && can('system:role:update')
    const canDelete = can('system:role:delete')
    const canPermissions = can('system:rolePermission:list')
    const canScope = can('system:role:dataScope:list')
    if (canEdit || canDelete || canPermissions || canScope)
      columns.push({
        title: '操作',
        key: 'action',
        align: 'center',
        cellProps: () => ({ style: { verticalAlign: 'middle' } }),
        // 列宽均分，四个操作按可用空间换行，不为操作列固定宽度。
        render: (row) =>
          h(NSpace, { size: [12, 4], justify: 'center', align: 'center' }, () => [
            canEdit
              ? h(
                  NButton,
                  {
                    text: true,
                    size: 'small',
                    type: 'warning',
                    disabled: loadingRecord.value,
                    onClick: () => openEditDialog(row.id),
                  },
                  () => '编辑',
                )
              : null,
            canDelete
              ? h(
                  NButton,
                  {
                    text: true,
                    size: 'small',
                    type: 'error',
                    onClick: () => removeRole(row),
                  },
                  () => '删除',
                )
              : null,
            canPermissions
              ? h(
                  NButton,
                  {
                    text: true,
                    size: 'small',
                    type: !can('system:rolePermission:assign') ? 'info' : 'warning',
                    disabled: loadingRecord.value,
                    onClick: () => openPermissionDialog(row),
                  },
                  () => (!can('system:rolePermission:assign') ? '查看权限' : '分配权限'),
                )
              : null,
            canScope
              ? h(
                  NButton,
                  {
                    text: true,
                    size: 'small',
                    type: !can('system:role:dataScope:assign') ? 'info' : 'warning',
                    disabled: loadingRecord.value,
                    onClick: () => openScopeDialog(row),
                  },
                  () => (!can('system:role:dataScope:assign') ? '查看范围' : '数据范围'),
                )
              : null,
          ]),
      })
    return columns
  })

  const [registerTable, table] = useWhimTable<RoleItem, Omit<RoleQuery, 'pageNum' | 'pageSize'>>(
    () => ({
      title: '角色管理',
      columns,
      rowKey: (row) => String(row.id),
      scrollX: 1100,
      immediate: canView.value,
      request: (query) =>
        fetchRolePage({
          ...query,
          roleName: query.roleName.trim(),
          roleCode: query.roleCode.trim(),
        }),
      search: {
        defaultValues: { roleName: '', roleCode: '', status: null },
        fields: [
          { key: 'roleName', label: '角色名称' },
          { key: 'roleCode', label: '角色编码' },
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
      toolbar: { buttons: ['add'], permissions: { add: ['system:role:create'] } },
    }),
  )

  function openAddDialog() {
    const token = auth.getToken
    openFormDialog({
      title: '新增角色',
      width: 600,
      formComponent: RoleForm,
      onSubmit: (model) => (auth.getToken === token ? addRole(model) : false),
      onSuccess: table.reload,
    })
  }

  async function openEditDialog(id: RoleId) {
    if (loadingRecord.value) return
    const version = ++recordVersion
    const token = auth.getToken
    loadingRecord.value = true
    try {
      const response = await fetchRoleDetail(id)
      if (version !== recordVersion || auth.getToken !== token) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: '修改角色',
        width: 600,
        formComponent: RoleForm,
        formProps: { initialModel: response.data },
        onSubmit: (model) => (auth.getToken === token ? updateRole(model) : false),
        onSuccess: table.reload,
      })
    } catch {
      // 请求失败由公共请求层提示。
    } finally {
      if (version === recordVersion) loadingRecord.value = false
    }
  }

  async function openPermissionDialog(row: RoleItem) {
    if (loadingRecord.value) return
    const version = ++recordVersion
    const token = auth.getToken
    loadingRecord.value = true
    try {
      const response = await fetchRolePermissions(row.id)
      if (version !== recordVersion || auth.getToken !== token) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      const readonly = !can('system:rolePermission:assign')
      openFormDialog({
        title: `${readonly ? '查看权限' : '分配权限'} · ${row.roleName}`,
        width: 660,
        formComponent: PermissionForm,
        formProps: { role: row, initialModel: response.data, readonly },
        modalProps: {
          positiveText: readonly ? '关闭' : '保存权限',
          negativeText: readonly ? '' : '取消',
          positiveButtonProps: { type: readonly ? 'info' : 'warning' },
        },
        successMessage: readonly ? false : undefined,
        onSubmit: (model) =>
          auth.getToken !== token ? false : readonly ? true : assignRolePermissions(model),
        onSuccess: readonly ? undefined : table.reload,
      })
    } catch {
      // 请求失败由公共请求层提示。
    } finally {
      if (version === recordVersion) loadingRecord.value = false
    }
  }

  async function openScopeDialog(row: RoleItem) {
    if (loadingRecord.value) return
    const version = ++recordVersion
    const token = auth.getToken
    loadingRecord.value = true
    try {
      const response = await fetchRoleDataScope(row.id)
      if (version !== recordVersion || auth.getToken !== token) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      const readonly = !can('system:role:dataScope:assign')
      openFormDialog({
        title: `${readonly ? '查看范围' : '数据范围'} · ${row.roleName}`,
        width: 600,
        formComponent: DataScopeForm,
        formProps: { initialModel: response.data, readonly },
        modalProps: {
          positiveText: readonly ? '关闭' : '保存范围',
          negativeText: readonly ? '' : '取消',
          positiveButtonProps: { type: readonly ? 'info' : 'warning' },
        },
        successMessage: readonly ? false : undefined,
        onSubmit: (model) =>
          auth.getToken !== token ? false : readonly ? true : assignRoleDataScope(model),
        onSuccess: readonly ? undefined : table.reload,
      })
    } catch {
      // 请求失败由公共请求层提示。
    } finally {
      if (version === recordVersion) loadingRecord.value = false
    }
  }

  function confirmStatus(row: RoleItem, status: RoleStatus) {
    if (statusRoleId.value !== undefined || status === row.status) return
    const version = recordVersion
    const token = auth.getToken
    const label = dictionary.getDictData('sys_common_status', status).label
    const type = status === 0 ? 'success' : 'warning'
    statusRoleId.value = row.id
    openConfirmDialog({
      title: `${label}角色`,
      content: `确定${label}“${row.roleName}”？角色状态变更后，关联用户需要重新登录。`,
      modalProps: {
        type,
        positiveButtonProps: { type },
        onAfterLeave: () => {
          if (version === recordVersion) statusRoleId.value = undefined
        },
      },
      onSubmit: async () => {
        if (auth.getToken !== token || version !== recordVersion) return false
        statusSubmitting.value = true
        try {
          return await updateRoleStatus({ id: row.id, status })
        } finally {
          if (version === recordVersion) statusSubmitting.value = false
        }
      },
      onSuccess: table.reload,
    })
  }

  function removeRole(row: RoleItem) {
    const token = auth.getToken
    openDeleteDialog({
      content: `确定删除“${row.roleName}”？仍被用户使用的角色无法删除，请先在用户管理中调整角色。`,
      onSubmit: () => (auth.getToken === token ? deleteRole(row.id) : false),
      onSuccess: table.reload,
    })
  }

  onDeactivated(() => {
    active = false
    recordVersion++
    loadingRecord.value = false
    statusRoleId.value = undefined
    statusSubmitting.value = false
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
    openAddDialog,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
  }
}
