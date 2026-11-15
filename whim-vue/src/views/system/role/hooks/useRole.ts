import { computed, h, onActivated, onDeactivated, onScopeDispose, ref, shallowRef } from 'vue'
import { NSpace, NSwitch, NTag, type DataTableColumns } from 'naive-ui'
import { TableAction, useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import type { DialogController } from '@/components/dialog/types'
import { useUserStore } from '@/stores/modules/user'
import { useAuthStore } from '@/stores/modules/auth'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import {
  addRole,
  deleteRole,
  fetchRoleDetail,
  fetchRolePage,
  updateRole,
  updateRoleStatus,
} from './api'
import type { RoleId, RoleItem, RoleQuery, RoleStatus } from './types'
import RoleForm from '../form.vue'

/** 角色管理按菜单权限码显示操作，所有角色采用相同的维护规则。 */
export function useRole() {
  const user = useUserStore()
  const auth = useAuthStore()
  const dictionary = useDictStore()
  const { openFormDialog, openDeleteDialog, openConfirmDialog } = useFormDialog()
  const can = (permission: string) => user.hasPermission(permission)
  const canView = computed(() => can('system:role:list'))
  const loadingRecord = ref(false)
  const authorizingRole = shallowRef<RoleItem>()
  const showAuthorization = ref(false)
  const usersRole = shallowRef<RoleItem>()
  const showUsers = ref(false)
  const statusRoleId = ref<RoleId>()
  const statusDialog = shallowRef<DialogController>()
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
          return h(NTag, { bordered: false, type: scope.listClass }, () => scope.label)
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
            return h(NTag, { bordered: false, type: status.listClass }, () => status.label)
          return h(
            NSwitch,
            {
              value: row.status,
              checkedValue: 0,
              uncheckedValue: 1,
              disabled: statusRoleId.value !== undefined,
              loading:
                !!statusDialog.value?.submitting.value &&
                String(statusRoleId.value) === String(row.id),
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
    const canUsers = can('system:roleUser:list')
    if (canEdit || canDelete || canPermissions || canScope || canUsers)
      columns.push({
        title: '操作',
        key: 'action',
        align: 'center',
        cellProps: () => ({ style: { verticalAlign: 'middle' } }),
        // 列宽均分，四个操作按可用空间换行，不为操作列固定宽度。
        render: (row) =>
          h(NSpace, { size: 6, justify: 'center', align: 'center' }, () => [
            canEdit
              ? h(TableAction, {
                  label: '编辑',
                  icon: 'pencil',
                  type: 'warning',
                  disabled: loadingRecord.value,
                  onClick: () => openEditDialog(row.id),
                })
              : null,
            canDelete
              ? h(TableAction, {
                  label: '删除',
                  icon: 'trash-2',
                  type: 'error',
                  onClick: () => removeRole(row),
                })
              : null,
            canPermissions || canScope
              ? h(TableAction, {
                  label: '角色授权',
                  icon: 'shield-check',
                  type:
                    (canPermissions && can('system:rolePermission:assign')) ||
                    (canScope && can('system:role:dataScope:assign'))
                      ? 'warning'
                      : 'info',
                  disabled: loadingRecord.value,
                  onClick: () => openAuthorizationDialog(row),
                })
              : null,
            canUsers
              ? h(TableAction, {
                  label: can('system:roleUser:assign') ? '分配用户' : '查看用户',
                  icon: 'users',
                  type: can('system:roleUser:assign') ? 'warning' : 'info',
                  disabled: loadingRecord.value,
                  onClick: () => openUsersDrawer(row),
                })
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

  /** 功能权限和数据范围在同一弹窗中分开保存，各自遵守原操作权限。 */
  function openAuthorizationDialog(row: RoleItem) {
    authorizingRole.value = row
    showAuthorization.value = true
  }

  /** 在保留角色列表的同时管理关联用户。 */
  function openUsersDrawer(row: RoleItem) {
    usersRole.value = row
    showUsers.value = true
  }

  function confirmStatus(row: RoleItem, status: RoleStatus) {
    if (statusRoleId.value !== undefined || status === row.status) return
    const version = recordVersion
    const token = auth.getToken
    const label = dictionary.getDictData('sys_common_status', status).label
    const type = status === 0 ? 'success' : 'warning'
    statusRoleId.value = row.id
    statusDialog.value = openConfirmDialog({
      title: `${label}角色`,
      content: `确定${label}“${row.roleName}”？角色状态变更后，关联用户需要重新登录。`,
      modalProps: {
        type,
        positiveButtonProps: { type },
        onAfterLeave: () => {
          if (version === recordVersion) {
            statusRoleId.value = undefined
            statusDialog.value = undefined
          }
        },
      },
      onSubmit: () => {
        if (auth.getToken !== token || version !== recordVersion) return false
        return updateRoleStatus({ id: row.id, status })
      },
      onSuccess: table.reload,
    })
  }

  function removeRole(row: RoleItem) {
    const token = auth.getToken
    openDeleteDialog({
      content: `确定删除“${row.roleName}”？仍被用户使用的角色无法删除，请先通过“分配用户”移除关联。`,
      onSubmit: () => (auth.getToken === token ? deleteRole(row.id) : false),
      onSuccess: table.reload,
    })
  }

  onDeactivated(() => {
    active = false
    showAuthorization.value = false
    showUsers.value = false
    recordVersion++
    loadingRecord.value = false
    statusRoleId.value = undefined
    statusDialog.value = undefined
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
    authorizingRole,
    showAuthorization,
    usersRole,
    showUsers,
    reloadRoles: table.reload,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
  }
}
