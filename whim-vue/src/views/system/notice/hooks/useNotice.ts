import { computed, defineAsyncComponent, h, onScopeDispose, ref, shallowRef } from 'vue'
import { NSpace, NTag, type DataTableColumns } from 'naive-ui'
import { TableAction, useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import { useDictStore } from '@/stores/modules/dict'
import { addNotice, deleteNotice, fetchNoticeDetail, fetchNoticePage, updateNotice } from './api'
import type { NoticeDetail, NoticeFilters, NoticeSummary } from './types'
const NoticeForm = defineAsyncComponent(() => import('../form.vue'))

/** 管理公告分页、编辑权限和操作弹窗。 */
export function useNotice() {
  const user = useUserStore()
  const dictionary = useDictStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const detail = shallowRef<NoticeDetail | null>(null)
  const showDetail = ref(false)
  let detailRequest = 0
  onScopeDispose(() => {
    detailRequest++
  })
  const can = (action: string) => user.hasPermission('system:notice:' + action)
  const canView = computed(() => can('page'))
  const types = computed(() => dictionary.getDictNumberOptions('sys_notice_type'))
  const statuses = computed(() => dictionary.getDictNumberOptions('sys_notice_status'))

  function tag(type: string, value: number) {
    const item = dictionary.getDictData(type, value)
    return h(NTag, { type: item.listClass, size: 'small', bordered: false }, () => item.label)
  }

  const columns = computed<DataTableColumns<NoticeSummary>>(() => {
    const result: DataTableColumns<NoticeSummary> = [
      { title: '标题', key: 'title', minWidth: 200, ellipsis: { tooltip: true } },
      {
        title: '类型',
        key: 'noticeType',
        width: 84,
        render: (row) => tag('sys_notice_type', row.noticeType),
      },
      {
        title: '状态',
        key: 'status',
        width: 84,
        render: (row) => tag('sys_notice_status', row.status),
      },
      {
        title: '发布时间',
        key: 'publishTime',
        width: 172,
        render: (row) => row.publishTime ?? '—',
      },
      { title: '创建时间', key: 'createTime', width: 172 },
    ]
    if (can('detail') || can('delete'))
      result.push({
        title: '操作',
        key: 'actions',
        fixed: 'right',
        width: 180,
        render: (row) =>
          h(NSpace, { size: 6, wrap: false }, () => [
            can('detail') &&
              h(TableAction, {
                label: '查看',
                type: 'info',
                icon: 'eye',
                onClick: () => openNotice(row, false),
              }),
            can('detail') &&
              can('update') &&
              h(TableAction, {
                label: '修改',
                type: 'warning',
                icon: 'pencil',
                onClick: () => openNotice(row, true),
              }),
            can('delete') &&
              h(TableAction, {
                label: '删除',
                type: 'error',
                icon: 'trash-2',
                onClick: () => removeNotice(row),
              }),
          ]),
      })
    return result
  })
  const [registerTable, table] = useWhimTable<NoticeSummary, NoticeFilters>(() => ({
    title: '通知公告',
    columns,
    rowKey: (row) => row.id,
    scrollX: 920,
    immediate: canView.value,
    request: ({ title, noticeType, status, pageNum, pageSize }) =>
      fetchNoticePage({
        title: title.trim() || undefined,
        noticeType: noticeType ?? undefined,
        status: status ?? undefined,
        pageNum,
        pageSize,
      }),
    search: {
      defaultValues: { title: '', noticeType: null, status: null },
      fields: [
        { key: 'title', label: '标题' },
        {
          key: 'noticeType',
          label: '类型',
          format: (query) =>
            dictionary.getDictData('sys_notice_type', String(query.noticeType)).label,
        },
        {
          key: 'status',
          label: '状态',
          format: (query) =>
            dictionary.getDictData('sys_notice_status', String(query.status)).label,
        },
      ],
    },
    toolbar: { buttons: ['add'], permissions: { add: ['system:notice:add'] } },
  }))

  function openNoticeForm(initialModel?: NoticeDetail) {
    openFormDialog({
      title: initialModel ? '修改通知公告' : '新增通知公告',
      width: 960,
      formComponent: NoticeForm,
      formProps: { initialModel },
      onSubmit: (model) => initialModel
        ? updateNotice({ ...model, id: initialModel.id, version: initialModel.version })
        : addNotice(model),
      onSuccess: table.reload,
    })
  }
  async function openNotice(row: NoticeSummary, edit: boolean) {
    const current = ++detailRequest
    try {
      const { data } = await fetchNoticeDetail({ id: row.id })
      if (current !== detailRequest) return
      if (edit) {
        openNoticeForm(data)
      } else {
        detail.value = data
        showDetail.value = true
      }
    } catch {
      /* 请求失败由公共请求层提示。 */
    }
  }
  function removeNotice(row: NoticeSummary) {
    openDeleteDialog({
      content: `确定删除“${row.title}”？删除后用户无法继续查看。`,
      onSubmit: () => deleteNotice({ id: row.id }),
      onSuccess: table.reload,
    })
  }
  return {
    registerTable,
    form: table.searchModel,
    canView,
    types,
    statuses,
    openNoticeForm,
    detail,
    showDetail,
  }
}
