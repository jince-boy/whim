import { computed, h, onActivated, onDeactivated, onScopeDispose, ref } from 'vue'
import { NAvatar, NButton, NSpace, NTag, type DataTableColumns, type TreeOption } from 'naive-ui'
import { TableAction, useWhimTable } from '@/components/table'
import { useFormDialog, type DialogController } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDictStore } from '@/stores/modules/dict'
import { useAvatarImages } from '@/hooks/useAvatarImages'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { message } from '@/plugins'
import {
  addUser,
  assignUserRoles,
  deleteUser,
  fetchUserDetail,
  fetchUserPage,
  fetchUserRoles,
  resetUserPassword,
  resetUserTwoFactor,
  updateUser,
} from './api'
import type { UserId, UserItem, UserQuery } from './types'
import UserForm from '../form.vue'
import AccountSecurityForm from '../AccountSecurityForm.vue'
import RoleForm from '../RoleForm.vue'

/** 部门分支筛选用户，并管理资料、密码与角色弹窗。 */
export function useUser() {
  const currentUser = useUserStore()
  const dictionary = useDictStore()
  const {
    selection,
    canLoad,
    loading: departmentLoading,
    error: departmentError,
    reload: reloadDepartments,
  } = useDeptOptions()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const can = (permission: string) => currentUser.hasPermission(permission)
  const canView = computed(() => can('system:user:list'))
  const departmentPattern = ref('')
  const departmentRefreshing = ref(false)
  const selectedDepartmentKey = ref('all')
  const selectedDepartmentId = computed(() =>
    ['all', 'unassigned'].includes(selectedDepartmentKey.value)
      ? null
      : selectedDepartmentKey.value,
  )
  const departmentTree = computed<TreeOption[]>(() => [
    { key: 'all', label: '全部用户' },
    { key: 'unassigned', label: '未分配部门' },
    ...selection.value.options,
  ])
  const editingId = ref<UserId>()
  const assigningRolesId = ref<UserId>()
  const recordLoading = computed(
    () => editingId.value !== undefined || assigningRolesId.value !== undefined,
  )
  let recordVersion = 0
  let active = true

  const isSelf = (row: UserItem) => String(row.id) === String(currentUser.user?.userId)
  const columns = computed<DataTableColumns<UserItem>>(() => {
    const columns: DataTableColumns<UserItem> = [
      {
        title: '头像',
        key: 'avatar',
        align: 'center',
        cellProps: () => ({ style: { verticalAlign: 'middle' } }),
        render: (row) => {
          const name = row.name || row.username
          const src = getAvatarUrl(row.avatar)
          const avatarText = name.slice(0, 1).toUpperCase()
          const avatar = h(
            NAvatar,
            {
              src,
              size: 36,
              round: false,
              objectFit: 'cover',
              imgProps: { alt: name },
              style: { display: 'flex', margin: '0 auto' },
            },
            {
              default: () => (src ? null : avatarText),
              fallback: () => avatarText,
            },
          )
          return src
            ? h(
                NButton,
                {
                  text: true,
                  size: 'small',
                  disabled: avatarPreviewLoading.value,
                  title: '查看头像',
                  'aria-label': `查看${name}的头像`,
                  style: { display: 'flex', width: '36px', height: '36px', margin: '0 auto' },
                  onClick: () => openAvatarPreview(row.avatar),
                },
                () => avatar,
              )
            : avatar
        },
      },
      { title: '用户名', key: 'username', ellipsis: { tooltip: true } },
      { title: '姓名', key: 'name', ellipsis: { tooltip: true } },
      {
        title: '所属部门',
        key: 'deptName',
        ellipsis: { tooltip: true },
        render: (row) => (row.deptId == null ? '未分配部门' : row.deptName || '—'),
      },
      {
        title: '联系电话',
        key: 'phone',
        ellipsis: { tooltip: true },
        render: (row) => row.phone || '—',
      },
      {
        title: '性别',
        key: 'gender',
        align: 'center',
        render: (row) => dictionary.getDictData('sys_user_gender', row.gender).label,
      },
      {
        title: '状态',
        key: 'status',
        align: 'center',
        render: (row) => {
          const status = dictionary.getDictData('sys_common_status', row.status)
          return h(NTag, { bordered: false, type: status.listClass }, () => status.label)
        },
      },
      { title: '创建时间', key: 'createTime', ellipsis: { tooltip: true } },
    ]
    const canEdit = can('system:user:detail') && can('system:user:update')
    const canResetPassword = can('system:user:resetPassword')
    const canAssignRoles = can('system:userRole:list') && can('system:userRole:assign')
    const canDelete = can('system:user:delete')
    if (canEdit || canResetPassword || canAssignRoles || canDelete) {
      columns.push({
        title: '操作',
        key: 'action',
        // 操作列与数据列均分宽度，空间不足时换行，编辑和删除优先排列。
        render: (row) => {
          const protectedAccount = row.superAdministrator && !currentUser.superAdministrator
          return h(NSpace, { size: 6, align: 'center' }, () => [
            canEdit
              ? h(TableAction, {
                  label: '编辑',
                  icon: 'pencil',
                  type: 'warning',
                  loading: editingId.value === row.id,
                  disabled: recordLoading.value || protectedAccount,
                  onClick: () => openEditDialog(row.id),
                })
              : null,
            canDelete
              ? h(TableAction, {
                  label: '删除',
                  icon: 'trash-2',
                  type: 'error',
                  disabled: isSelf(row) || protectedAccount,
                  onClick: () => removeUser(row),
                })
              : null,
            canResetPassword
              ? h(TableAction, {
                  label: '账号安全',
                  icon: 'key-round',
                  type: 'warning',
                  disabled: protectedAccount,
                  onClick: () => openAccountSecurityDialog(row),
                })
              : null,
            canAssignRoles
              ? h(TableAction, {
                  label: '分配角色',
                  icon: 'users',
                  type: 'primary',
                  loading: assigningRolesId.value === row.id,
                  disabled: recordLoading.value || isSelf(row) || row.superAdministrator,
                  tooltip: row.superAdministrator
                    ? '超级管理员默认拥有全部权限，无需分配角色'
                    : undefined,
                  onClick: () => openRoleDialog(row),
                })
              : null,
          ])
        },
      })
    }
    return columns
  })

  const [registerTable, table] = useWhimTable<UserItem, Omit<UserQuery, 'deptId' | 'unassigned'>>(
    () => ({
      title:
        selectedDepartmentKey.value === 'all'
          ? '全部用户'
          : selectedDepartmentKey.value === 'unassigned'
            ? '未分配部门的用户'
            : `${selection.value.names.get(selectedDepartmentKey.value) ?? '所选部门'}及下级部门 · 用户`,
      columns,
      rowKey: (row) => String(row.id),
      scrollX: 1372,
      immediate: canView.value,
      request: (query) =>
        fetchUserPage({
          ...query,
          username: query.username.trim(),
          name: query.name.trim(),
          phone: query.phone.trim(),
          deptId: selectedDepartmentId.value,
          unassigned: selectedDepartmentKey.value === 'unassigned' ? true : null,
        }),
      search: {
        defaultValues: { username: '', name: '', phone: '', status: null },
        fields: [
          { key: 'username', label: '用户名' },
          { key: 'name', label: '姓名' },
          { key: 'phone', label: '联系电话' },
          {
            key: 'status',
            label: '状态',
            format: (query) =>
              query.status == null
                ? ''
                : dictionary.getDictData('sys_common_status', query.status).label,
          },
        ],
      },
      toolbar: { buttons: ['add'], permissions: { add: ['system:user:create'] } },
    }),
  )

  const {
    getAvatarUrl,
    avatarPreviewUrl,
    avatarPreviewShown,
    avatarPreviewLoading,
    openAvatarPreview,
  } = useAvatarImages(() => table.data.value.map((row) => row.avatar))

  function selectDepartment(keys: Array<string | number>) {
    const key = String(keys[0] ?? 'all')
    if (key === selectedDepartmentKey.value) return
    selectedDepartmentKey.value = key
    void table.search()
  }

  function refreshDepartments() {
    if (!canLoad.value || departmentLoading.value || departmentRefreshing.value) return
    departmentRefreshing.value = true
    void reloadDepartments()
  }

  function finishDepartmentRefreshRotation() {
    if (!departmentLoading.value) departmentRefreshing.value = false
  }

  function openAddDialog() {
    openFormDialog({
      title: '新增用户',
      width: 740,
      formComponent: UserForm,
      formProps: { defaultDeptId: selectedDepartmentId.value },
      onSubmit: addUser,
      onSuccess: table.reload,
    })
  }

  /** 退出页面后丢弃详情响应，避免在其他页面弹出旧用户资料。 */
  async function openEditDialog(id: UserId) {
    if (recordLoading.value) return
    const version = ++recordVersion
    editingId.value = id
    try {
      const response = await fetchUserDetail(id)
      if (version !== recordVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: '编辑用户',
        width: 740,
        formComponent: UserForm,
        formProps: { initialModel: response.data },
        onSubmit: updateUser,
        onSuccess: table.reload,
      })
    } catch {
      // 公共请求层统一提示错误。
    } finally {
      if (version === recordVersion) {
        editingId.value = undefined
      }
    }
  }

  function openAccountSecurityDialog(row: UserItem) {
    const operatorId = currentUser.user?.userId
    const bindingId = row.twoFactorBindingId
    const sameOperator = () => operatorId != null && currentUser.user?.userId === operatorId
    const dialog: DialogController = openFormDialog({
      title: `账号安全 · ${row.username}`,
      width: 540,
      formComponent: AccountSecurityForm,
      // 每个页签提供自己的操作，密码提交仍由公共弹窗校验并锁定所有关闭入口。
      modalProps: { positiveText: undefined, negativeText: undefined },
      formProps: {
        user: row,
        self: isSelf(row),
        get showTwoFactor() { return sameOperator() && currentUser.superAdministrator },
        get submitting() { return dialog.submitting.value },
        onClose: () => dialog.close(),
        onSave: () => dialog.submit(),
      },
      onSubmit: (model) => {
        if (!sameOperator()) return false
        if ('currentPassword' in model) {
          // 使用打开弹窗时的绑定编号，避免重置用户随后重新绑定的验证器。
          if (!currentUser.superAdministrator || isSelf(row) || bindingId == null) return false
          return resetUserTwoFactor({ id: row.id, bindingId, currentPassword: model.currentPassword })
        }
        if (!can('system:user:resetPassword') || (row.superAdministrator && !currentUser.superAdministrator)) return false
        return resetUserPassword(model)
      },
      onSuccess: table.reload,
    })
  }

  /** 候选角色与已分配角色由同一接口返回，提交完整集合。 */
  async function openRoleDialog(row: UserItem) {
    if (recordLoading.value) return
    const version = ++recordVersion
    assigningRolesId.value = row.id
    try {
      const response = await fetchUserRoles(row.id)
      if (version !== recordVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      openFormDialog({
        title: `分配角色 · ${row.name}`,
        width: 900,
        formComponent: RoleForm,
        formProps: { initialModel: response.data },
        onSubmit: assignUserRoles,
        onSuccess: table.reload,
      })
    } catch {
      // 公共请求层统一提示错误。
    } finally {
      if (version === recordVersion) {
        assigningRolesId.value = undefined
      }
    }
  }

  function removeUser(row: UserItem) {
    openDeleteDialog({
      content: `确定删除用户“${row.name}（${row.username}）”？用户的角色、岗位关系将一并移除，用户名不能再次使用。`,
      onSubmit: () => deleteUser(row.id),
      onSuccess: table.reload,
    })
  }

  onDeactivated(() => {
    active = false
    recordVersion++
    editingId.value = undefined
    assigningRolesId.value = undefined
    departmentRefreshing.value = false
  })
  onActivated(() => {
    if (!active) {
      active = true
      void table.reload()
      void reloadDepartments()
    }
  })
  onScopeDispose(() => recordVersion++)

  return {
    registerTable,
    form: table.searchModel,
    canView,
    canLoadDepartments: canLoad,
    avatarPreviewUrl,
    avatarPreviewShown,
    departmentTree,
    departmentPattern,
    departmentLoading,
    departmentError,
    departmentRefreshing,
    selectedDepartmentKeys: computed(() => [selectedDepartmentKey.value]),
    selectDepartment,
    refreshDepartments,
    finishDepartmentRefreshRotation,
    openAddDialog,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
  }
}
