import { computed, h, shallowRef, watch } from 'vue'
import type { Ref } from 'vue'
import type { DataTableColumns } from 'naive-ui'
import { TableAction, useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import type { DialogController } from '@/components/dialog/types'
import { useUserStore } from '@/stores/modules/user'
import { useAuthStore } from '@/stores/modules/auth'
import RoleUserForm from '../components/RoleUserForm.vue'
import { useRoleUserColumns } from './useRoleUserColumns'
import { assignRoleUsers, fetchRoleUserPage, unassignRoleUsers } from './api'
import type { RoleContextProps, RoleId, RoleUserItem, RoleUserQuery } from './types'

/** 管理角色成员和候选用户分页，只增删选中的关联，不覆盖完整成员集合。 */
export function useRoleUsers(props: RoleContextProps, show: Ref<boolean>) {
  const user = useUserStore()
  const auth = useAuthStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const canAssign = computed(() => user.hasPermission('system:roleUser:assign'))
  const candidateDialog = shallowRef<DialogController>()
  const assigning = computed(() => candidateDialog.value?.submitting.value ?? false)
  const removalDialog = shallowRef<DialogController>()
  const removing = computed(() => removalDialog.value?.submitting.value ?? false)
  const removalPending = computed(() => !!removalDialog.value)
  const isSelf = (row: RoleUserItem) => String(row.id) === String(user.user?.userId)

  const userColumns = useRoleUserColumns()
  const memberColumns = computed<DataTableColumns<RoleUserItem>>(() => [
    ...(canAssign.value
      ? [
          {
            type: 'selection' as const,
            width: 44,
            disabled: (row: RoleUserItem) => isSelf(row) || removalPending.value,
          },
        ]
      : []),
    ...userColumns,
    ...(canAssign.value
      ? [
          {
            title: '操作',
            key: 'action',
            width: 70,
            align: 'center' as const,
            render: (row: RoleUserItem) =>
              h(TableAction, {
                label: '移除用户',
                icon: 'trash-2',
                type: 'error',
                disabled: isSelf(row) || removalPending.value,
                tooltip: isSelf(row) ? '不能修改当前账号的角色' : undefined,
                onClick: () => removeUsers([row.id]),
              }),
          },
        ]
      : []),
  ])
  const [registerMembers, members] = useWhimTable<
    RoleUserItem,
    Omit<RoleUserQuery, 'roleId' | 'pageNum' | 'pageSize'>
  >({
    title: '已分配用户',
    columns: memberColumns,
    rowKey: (row) => String(row.id),
    scrollX: 700,
    immediate: false,
    search: {
      defaultValues: { username: '', name: '' },
      fields: [
        { key: 'username', label: '用户名' },
        { key: 'name', label: '姓名' },
      ],
    },
    request: (query) =>
      fetchRoleUserPage({
        ...query,
        roleId: props.role.id,
        username: query.username.trim(),
        name: query.name.trim(),
      }),
  })
  const selectedMemberIds = computed(() => members.getCheckedRowKeys())

  /** 与分配角色复用公共表单弹窗，候选列表和选择草稿随弹窗销毁。 */
  function openCandidates() {
    if (
      !canAssign.value ||
      props.role.status !== 0 ||
      candidateDialog.value ||
      removalPending.value
    )
      return
    const token = auth.getToken
    const roleId = props.role.id
    candidateDialog.value = openFormDialog({
      title: `分配用户 · ${props.role.roleName}`,
      width: 900,
      formComponent: RoleUserForm,
      formProps: { role: props.role },
      modalProps: {
        onAfterLeave: () => {
          candidateDialog.value = undefined
        },
      },
      onSubmit: async (model) => {
        if (token !== auth.getToken || roleId !== props.role.id || !show.value) return false
        const response = await assignRoleUsers(model)
        if (token !== auth.getToken || roleId !== props.role.id || !show.value) return false
        return response
      },
      onSuccess: members.reload,
    })
  }

  /** 批量移除仅删除角色关联，确认内容明确账号及其他角色不会被删除。 */
  function removeUsers(userIds: RoleId[]) {
    if (!canAssign.value || removalPending.value || !userIds.length) return
    const token = auth.getToken
    const roleId = props.role.id
    removalDialog.value = openDeleteDialog({
      title: '移除角色用户',
      successMessage: '移除用户成功',
      content: `确定从“${props.role.roleName}”中移除选中的${userIds.length}名用户？此操作不删除用户账号，也不影响其其他角色。`,
      modalProps: {
        positiveText: '移除',
        onAfterLeave: () => {
          removalDialog.value = undefined
        },
      },
      onSubmit: () => {
        if (token !== auth.getToken || roleId !== props.role.id || !show.value) return false
        return unassignRoleUsers({ roleId, userIds })
      },
      onSuccess: async () => {
        members.setCheckedRowKeys([])
        await members.reload()
      },
    })
  }

  watch(
    [show, () => props.role.id],
    ([visible]) => {
      candidateDialog.value?.close()
      removalDialog.value?.close()
      if (visible) void members.resetSearch()
    },
    { immediate: true },
  )
  return {
    registerMembers,
    memberForm: members.searchModel,
    canAssign,
    assigning,
    removing,
    removalPending,
    selectedMemberIds,
    openCandidates,
    removeUsers,
  }
}
