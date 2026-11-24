import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDataTable } from '@/components/dataTable'
import { fetchAvailableRoleUserPage } from './api'
import { roleUserRules } from './rule'
import { useRoleUserColumns } from './useRoleUserColumns'
import type { RoleContextProps, RoleUserAssignment, RoleUserItem, RoleUserQuery } from './types'

/** 维护独立的用户选择草稿，搜索和翻页均保留完整勾选集合。 */
export function useRoleUserForm(props: RoleContextProps) {
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<RoleUserAssignment>({ roleId: props.role.id, userIds: [] })
  const columns = useRoleUserColumns()
  const [registerTable, table] = useDataTable<
    RoleUserItem,
    Omit<RoleUserQuery, 'roleId' | 'pageNum' | 'pageSize'>
  >({
    title: '可分配用户',
    columns: [{ type: 'selection', width: 44 }, ...columns],
    rowKey: (row) => String(row.id),
    scrollX: 640,
    maxHeight: 320,
    immediate: false,
    preserveSelection: true,
    pagination: { pageSize: 10, prefix: ({ itemCount }) => `共 ${itemCount} 个用户` },
    toolbar: { showRefresh: false, showColumns: false, showStriped: false, showFullscreen: false },
    search: {
      defaultValues: { username: '', name: '' },
      fields: [
        { key: 'username', label: '用户名' },
        { key: 'name', label: '姓名' },
      ],
    },
    // 候选接口按实际分配权限过滤，不依赖用户管理页面的查询授权。
    request: (query) =>
      fetchAvailableRoleUserPage({
        ...query,
        roleId: props.role.id,
        username: query.username.trim(),
        name: query.name.trim(),
      }),
    onUpdateCheckedRowKeys: (keys) => {
      formModel.value = { ...formModel.value, userIds: keys.map(String) }
      formRef.value?.restoreValidation()
    },
  })
  watch(
    () => props.role.id,
    (roleId) => {
      formModel.value = { roleId, userIds: [] }
      table.setCheckedRowKeys([])
      void table.resetSearch()
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )
  const selectedCount = computed(() => formModel.value.userIds.length)
  return {
    formRef,
    formModel,
    rules: roleUserRules,
    registerTable,
    searchModel: table.searchModel,
    selectedCount,
  }
}
