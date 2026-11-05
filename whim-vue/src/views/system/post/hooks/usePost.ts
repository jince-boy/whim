import { computed, h, onActivated, onDeactivated, onScopeDispose, ref, shallowRef } from 'vue'
import { NButton, NSpace, NTag, type DataTableColumns } from 'naive-ui'
import { useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { useDictStore } from '@/stores/modules/dict'
import { message } from '@/plugins'
import {
  addPost,
  deletePost,
  fetchPostDetail,
  fetchPostPage,
  updatePost,
} from './api'
import type { PostId, PostItem, PostQuery } from './types'
import PostForm from '../form.vue'

/** 岗位分页、详情、编辑及成员抽屉的页面协调。 */
export function usePost() {
  const dictionary = useDictStore()
  const user = useUserStore()
  const { selection, canLoad, reload: reloadDepartments } = useDeptOptions()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const can = (permission: string) =>
    user.permissionCode.includes('*') || user.permissionCode.includes(permission)
  const canView = computed(() => can('system:post:list'))
  const detail = shallowRef<PostItem>()
  const showDetail = ref(false)
  const memberPost = shallowRef<PostItem>()
  const showMembers = ref(false)
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
    if (can('system:post:detail') || can('system:post:member:list') || can('system:post:delete')) {
      columns.push({
        title: '操作',
        key: 'action',
        width: 230,
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
            can('system:post:member:list')
              ? h(
                  NButton,
                  { text: true, type: 'info', onClick: () => openMembers(row) },
                  () => '成员',
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

  const [registerTable, table] = useWhimTable<PostItem, PostQuery>(() => ({
    title: '岗位管理',
    columns,
    rowKey: (row) => String(row.id),
    scrollX: 1120,
    immediate: canView.value,
    request: (query) =>
      fetchPostPage({
        ...query,
        postName: query.postName.trim(),
        postCode: query.postCode.trim(),
        deptId: query.organizationWide ? null : query.deptId,
      }),
    search: {
      defaultValues: {
        postName: '',
        postCode: '',
        deptId: null,
        organizationWide: null,
        status: null,
      },
      fields: [
        { key: 'postName', label: '岗位名称' },
        { key: 'postCode', label: '岗位编码' },
        {
          key: 'deptId',
          label: '所属部门',
          format: (query) => (query.deptId ? departmentName(query.deptId) : ''),
        },
        {
          key: 'organizationWide',
          label: '岗位范围',
          format: (query) => (query.organizationWide ? '全组织岗位' : ''),
        },
        {
          key: 'status',
          label: '状态',
          format: (query) =>
            query.status === null ? '' : dictionary.getDictData('sys_common_status', query.status).label,
        },
      ],
    },
    toolbar: { buttons: ['add'], permissions: { add: ['system:post:create'] } },
  }))

  function changeOrganizationWide(value: boolean) {
    table.searchModel.value.organizationWide = value ? true : null
    if (value) table.searchModel.value.deptId = null
  }

  function openAddDialog() {
    openFormDialog({
      title: '新增岗位',
      width: 640,
      formComponent: PostForm,
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
      content: `确定删除“${row.postName}”？岗位仍有成员时无法删除，请先解除成员关联。`,
      onSubmit: () => deletePost(row.id),
      onSuccess: table.reload,
    })
  }

  function openMembers(row: PostItem) {
    memberPost.value = row
    showMembers.value = true
  }

  onDeactivated(() => {
    active = false
    recordVersion++
    recordLoading.value = false
    showDetail.value = false
    showMembers.value = false
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
    memberPost,
    showMembers,
    canLoadDepartments: canLoad,
    departmentOptions: computed(() => selection.value.options),
    departmentNames: computed(() => selection.value.names),
    departmentName,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    getStatusData: (value: number) => dictionary.getDictData('sys_common_status', value),
    openAddDialog,
    changeOrganizationWide,
  }
}
