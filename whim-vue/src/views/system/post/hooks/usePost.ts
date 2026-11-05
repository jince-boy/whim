import { computed, h, onActivated, onDeactivated, onScopeDispose, ref, shallowRef } from 'vue'
import { NButton, NSpace, NTag, type DataTableColumns, type TreeOption } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import { addPost, deletePost, fetchPostDetail, fetchPostPage, updatePost } from './api'
import type { PostId, PostItem, PostQuery } from './types'
import PostForm from '../form.vue'

/** 部门树联动岗位分页，以及岗位详情和编辑。 */
export function usePost() {
  const dictionary = useDictStore()
  const user = useUserStore()
  const {
    selection,
    canLoad,
    loading: departmentLoading,
    error: departmentError,
    reload: reloadDepartments,
  } = useDeptOptions()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const can = (permission: string) =>
    user.permissionCode.includes('*') || user.permissionCode.includes(permission)
  const canView = computed(() => can('system:post:list'))
  const detail = shallowRef<PostItem>()
  const showDetail = ref(false)
  const departmentPattern = ref('')
  const selectedDepartmentKey = ref('all')
  const selectedDepartmentId = computed(() =>
    ['all', 'organization'].includes(selectedDepartmentKey.value)
      ? null
      : selectedDepartmentKey.value,
  )
  const departmentTree = computed<TreeOption[]>(() => [
    { key: 'all', label: '全部岗位' },
    { key: 'organization', label: '全组织岗位' },
    ...selection.value.options,
  ])
  const recordLoading = ref(false)
  let recordVersion = 0
  let active = true

  function departmentName(id: string | number | null) {
    return id == null ? '全组织岗位' : (selection.value.names.get(String(id)) ?? `部门 ${id}`)
  }

  const columns = computed(() => {
    const columns: DataTableColumns<PostItem> = [
      { title: '岗位名称', key: 'postName', minWidth: 160, ellipsis: { tooltip: true } },
      { title: '岗位编码', key: 'postCode', minWidth: 160, ellipsis: { tooltip: true } },
      {
        title: '所属部门',
        key: 'deptId',
        minWidth: 160,
        ellipsis: { tooltip: true },
        render: (row) => departmentName(row.deptId),
      },
      { title: '排序', key: 'sort', width: 80, align: 'center' },
      {
        title: '状态',
        key: 'status',
        width: 100,
        align: 'center',
        render: (row) => {
          const status = dictionary.getDictData('sys_common_status', row.status)
          return h(NTag, { bordered: false, type: status.listClass }, () => status.label)
        },
      },
      { title: '创建时间', key: 'createTime', width: 180 },
    ]
    if (can('system:post:detail') || can('system:post:delete')) {
      columns.push({
        title: '操作',
        key: 'action',
        width: 180,
        fixed: 'right',
        render: (row) =>
          h(NSpace, { size: 12, wrap: false }, () => [
            can('system:post:detail')
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'info',
                    disabled: recordLoading.value,
                    onClick: () => openRecord(row.id),
                  },
                  () => '查看',
                )
              : null,
            can('system:post:detail') && can('system:post:update')
              ? h(
                  NButton,
                  {
                    text: true,
                    type: 'warning',
                    disabled: recordLoading.value,
                    onClick: () => openRecord(row.id, true),
                  },
                  () => '修改',
                )
              : null,
            can('system:post:delete')
              ? h(
                  NButton,
                  { text: true, type: 'error', onClick: () => removePost(row) },
                  () => '删除',
                )
              : null,
          ]),
      })
    }
    return columns
  })

  const [registerTable, table] = useWhimTable<
    PostItem,
    Omit<PostQuery, 'deptId' | 'organizationWide'>
  >(() => ({
    title:
      selectedDepartmentKey.value === 'all'
        ? '全部岗位'
        : selectedDepartmentId.value == null
          ? '全组织岗位'
          : `${departmentName(selectedDepartmentId.value)} · 岗位`,
    columns,
    rowKey: (row) => String(row.id),
    scrollX: 1060,
    immediate: canView.value,
    request: (query) =>
      fetchPostPage({
        ...query,
        postName: query.postName.trim(),
        postCode: query.postCode.trim(),
        deptId: selectedDepartmentId.value,
        organizationWide: selectedDepartmentKey.value === 'organization' ? true : null,
      }),
    search: {
      defaultValues: {
        postName: '',
        postCode: '',
        status: null,
      },
      fields: [
        { key: 'postName', label: '岗位名称' },
        { key: 'postCode', label: '岗位编码' },
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
    toolbar: { buttons: ['add'], permissions: { add: ['system:post:create'] } },
  }))

  /** 部门选择独立于搜索表单，重置名称和状态时仍保留当前部门。 */
  function selectDepartment(keys: Array<string | number>) {
    const key = String(keys[0] ?? 'all')
    if (key === selectedDepartmentKey.value) return
    selectedDepartmentKey.value = key
    void table.search()
  }

  function openAddDialog() {
    openFormDialog({
      title: '新增岗位',
      width: 640,
      formComponent: PostForm,
      formProps: { defaultDeptId: selectedDepartmentId.value },
      onSubmit: addPost,
      onSuccess: table.reload,
    })
  }

  async function openRecord(id: PostId, editing = false) {
    if (recordLoading.value) return
    const version = ++recordVersion
    recordLoading.value = true
    try {
      const response = await fetchPostDetail(id)
      if (version !== recordVersion) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      if (editing) {
        openFormDialog({
          title: '修改岗位',
          width: 640,
          formComponent: PostForm,
          formProps: { initialModel: response.data },
          onSubmit: updatePost,
          onSuccess: table.reload,
        })
      } else {
        detail.value = response.data
        showDetail.value = true
      }
    } catch {
      // 公共请求层提示错误。
    } finally {
      if (version === recordVersion) recordLoading.value = false
    }
  }

  function removePost(row: PostItem) {
    openDeleteDialog({
      content: `确定删除“${row.postName}”？仍被用户使用的岗位无法删除，请先在用户管理中调整岗位。`,
      onSubmit: () => deletePost(row.id),
      onSuccess: table.reload,
    })
  }

  onDeactivated(() => {
    active = false
    recordVersion++
    recordLoading.value = false
    showDetail.value = false
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
    detail,
    showDetail,
    canLoadDepartments: canLoad,
    departmentTree,
    departmentPattern,
    departmentLoading,
    departmentError,
    selectedDepartmentKeys: computed(() => [selectedDepartmentKey.value]),
    selectDepartment,
    reloadDepartments,
    departmentName,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    getStatusData: (value: number) => dictionary.getDictData('sys_common_status', value),
    openAddDialog,
  }
}
