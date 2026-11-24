import { computed, h, ref, useTemplateRef, watch } from 'vue'
import { NTag, NText, type DataTableColumns, type FormInst } from 'naive-ui'
import { useDataTable } from '@/components/dataTable'
import { useDictStore } from '@/stores/modules/dict'
import { roleRules } from './rule'
import type { RoleFormModel, RoleFormProps, UserRoleOption, UserRoleQuery } from './types'

/** 保留无权调整的已分配角色，避免提交时静默丢失授权。 */
export function useRoleForm(props: RoleFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<RoleFormModel>({ id: props.initialModel.id, roleIds: [] })
  const columns: DataTableColumns<UserRoleOption> = [
    { type: 'selection', width: 44, disabled: (row) => row.disabled },
    { title: '角色名称', key: 'roleName', minWidth: 220, ellipsis: { tooltip: true } },
    {
      title: '角色编码',
      key: 'roleCode',
      minWidth: 240,
      ellipsis: { tooltip: true },
      render: (row) => h(NText, { depth: 3 }, () => row.roleCode),
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
  const [registerTable, table] = useDataTable<UserRoleOption, UserRoleQuery>({
    title: '可分配角色',
    columns,
    rowKey: (row) => String(row.id),
    data: () => props.initialModel.options,
    scrollX: 620,
    maxHeight: 320,
    preserveSelection: true,
    pagination: { pageSize: 10, prefix: ({ itemCount }) => `共 ${itemCount} 个角色` },
    toolbar: { showRefresh: false, showColumns: false, showStriped: false, showFullscreen: false },
    search: {
      defaultValues: { roleName: '', roleCode: '' },
      fields: [
        { key: 'roleName', label: '角色名称' },
        { key: 'roleCode', label: '角色编码' },
      ],
    },
    transformData: (rows, query) => {
      const roleName = query.roleName.trim().toLocaleLowerCase()
      const roleCode = query.roleCode.trim().toLocaleLowerCase()
      return rows.filter(
        (role) =>
          role.roleName.toLocaleLowerCase().includes(roleName) &&
          role.roleCode.toLocaleLowerCase().includes(roleCode),
      )
    },
    // 保存完整选择集合，不能用当前页行数据覆盖筛选外的已分配角色。
    onUpdateCheckedRowKeys: (keys) => {
      formModel.value = { ...formModel.value, roleIds: keys.map(String) }
      formRef.value?.restoreValidation()
    },
  })
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = { id: model.id, roleIds: model.roleIds.map(String) }
      table.setCheckedRowKeys(formModel.value.roleIds)
      void table.resetSearch()
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )
  const selectedCount = computed(() => formModel.value.roleIds.length)
  return {
    formRef,
    formModel,
    rules: roleRules,
    registerTable,
    searchModel: table.searchModel,
    selectedCount,
  }
}
