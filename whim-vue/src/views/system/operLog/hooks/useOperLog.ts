import { computed, h, ref } from 'vue'
import { NSpace, NTag, type DataTableColumns } from 'naive-ui'
import dayjs from 'dayjs'
import { TableAction, useWhimTable } from '@/components/table'
import { useFormDialog } from '@/components/dialog'
import { useDictStore } from '@/stores/modules/dict'
import { useUserStore } from '@/stores/modules/user'
import {
  cleanOperLog,
  deleteOperLog,
  exportOperLog,
  fetchOperLogDetail,
  fetchOperLogPage,
} from './api'
import type { OperLog, OperLogDetail, OperLogFilters, OperLogQuery } from './types'

/** 按两空格缩进展示 JSON，历史纯文本摘要按原文展示。 */
function formatSummary(value: string | null | undefined): string {
  if (!value?.trim()) return ''
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

/** 列表与导出共用已提交的筛选条件。 */
function getFilters(query: OperLogQuery): OperLogFilters {
  return {
    title: query.title.trim() || undefined,
    operName: query.operName.trim() || undefined,
    ipAddress: query.ipAddress.trim() || undefined,
    status: query.status ?? undefined,
    logType: query.logType ?? undefined,
    startTime: query.dateRange
      ? dayjs(query.dateRange[0]).format('YYYY-MM-DD HH:mm:ss')
      : undefined,
    endTime: query.dateRange ? dayjs(query.dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : undefined,
  }
}

/** 管理操作日志及按权限加载的审计详情。 */
export function useOperLog() {
  const dictionary = useDictStore()
  const user = useUserStore()
  const exporting = ref(false)
  const canView = computed(() => user.hasPermission('system:operLog:page'))
  const canDetail = computed(() => user.hasPermission('system:operLog:detail'))
  const canDelete = computed(() => user.hasPermission('system:operLog:delete'))
  const canClean = computed(() => user.hasPermission('system:operLog:clean'))
  const sysStatusOptions = computed(() => dictionary.getDictNumberOptions('sys_result_status'))
  const sysOperTypeOptions = computed(() => dictionary.getDictNumberOptions('sys_oper_type'))
  const getDictOperTypeData = (value: number) => dictionary.getDictData('sys_oper_type', value)
  const getDictStatusData = (value: number) => dictionary.getDictData('sys_result_status', value)
  const { openDeleteDialog } = useFormDialog()
  const showModal = ref(false)
  const currentOperLog = ref<OperLogDetail | null>(null)
  const requestSummary = computed(() => formatSummary(currentOperLog.value?.requestParam))
  const responseSummary = computed(() => formatSummary(currentOperLog.value?.responseParam))
  const failureReason = computed(() => {
    const message = currentOperLog.value?.errorMessage
    // 旧版只保存了异常类名，无法从该条审计记录还原业务原因。
    return message && /^操作失败（[\w.$]*(?:Exception|Error)）$/.test(message)
      ? '此条历史日志未记录具体失败原因'
      : message
  })
  let detailVersion = 0

  const columns = computed<DataTableColumns<OperLog>>(() => {
    const result: DataTableColumns<OperLog> = [
      { title: '操作人', key: 'operName' },
      { title: '模块名称', key: 'title' },
      {
        title: '操作类型',
        key: 'logType',
        render(row) {
          const item = getDictOperTypeData(row.logType)
          return h(NTag, { bordered: false, type: item.listClass }, () => item.label)
        },
      },
      { title: '操作 IP', key: 'operIp' },
      { title: '操作地点', key: 'operLocation' },
      {
        title: '操作结果',
        key: 'status',
        render(row) {
          const item = getDictStatusData(row.status)
          return h(NTag, { bordered: false, type: item.listClass }, () => item.label)
        },
      },
      { title: '操作时间', key: 'operTime' },
      { title: '耗时', key: 'costTime', render: (row) => `${row.costTime} ms` },
    ]
    if (canDelete.value) result.unshift({ type: 'selection' })
    if (canDetail.value || canDelete.value) {
      result.push({
        title: '操作',
        key: 'action',
        fixed: 'right',
        render: (row) =>
          h(NSpace, { size: 6 }, () => [
            canDetail.value &&
              h(TableAction, {
                label: '查看',
                icon: 'eye',
                type: 'info',
                onClick: () => showDetail(row.id),
              }),
            canDelete.value &&
              h(TableAction, {
                label: '删除',
                icon: 'trash-2',
                type: 'error',
                onClick: () => removeOperLogBatch([row.id]),
              }),
          ]),
      })
    }
    return result
  })

  const [registerTable, table] = useWhimTable<OperLog, OperLogQuery>(() => ({
    title: '操作日志',
    columns,
    scrollX: 1180,
    rowKey: (row) => row.id,
    request: ({ pageNum, pageSize, ...query }) =>
      fetchOperLogPage({ ...getFilters(query), pageNum, pageSize }),
    search: {
      defaultValues: {
        title: '',
        operName: '',
        logType: null,
        status: null,
        ipAddress: '',
        dateRange: null,
      },
      fields: [
        { key: 'title', label: '模块名称' },
        { key: 'operName', label: '操作人' },
        {
          key: 'status',
          label: '操作结果',
          format: (q) => (q.status == null ? '' : getDictStatusData(q.status).label),
        },
        { key: 'ipAddress', label: 'IP 地址' },
        {
          key: 'logType',
          label: '操作类型',
          format: (q) => (q.logType == null ? '' : getDictOperTypeData(q.logType).label),
        },
        {
          key: 'dateRange',
          label: '操作时间',
          format: (q) =>
            q.dateRange?.map((t) => dayjs(t).format('YYYY-MM-DD HH:mm:ss')).join(' 至 ') ?? '',
        },
      ],
    },
    toolbar: {
      buttons: ['delete', 'export'],
      loading: { export: exporting.value },
      permissions: { delete: ['system:operLog:delete'], export: ['system:operLog:export'] },
    },
  }))

  /** 多次点击不同记录时只展示最后选中的详情。 */
  async function showDetail(id: string | number) {
    const version = ++detailVersion
    try {
      const response = await fetchOperLogDetail(id)
      if (version === detailVersion && canDetail.value && response.code === 200) {
        currentOperLog.value = response.data
        showModal.value = true
      }
    } catch {
      // 公共请求层展示读取失败原因。
    }
  }

  async function refreshLogs() {
    table.setCheckedRowKeys([])
    await table.reload()
  }

  function removeOperLogBatch(ids: Array<string | number>) {
    if (!ids.length) return
    openDeleteDialog({
      content: `确定删除选中的 ${ids.length} 条操作日志吗？`,
      onSubmit: () => deleteOperLog(ids),
      onSuccess: refreshLogs,
    })
  }

  function handleCleanOperLog() {
    openDeleteDialog({
      title: '清空全部操作日志',
      content:
        '确定清空全部操作日志吗？此操作包含当前筛选范围以外的记录，本次清空也会留下审计记录。',
      onSubmit: cleanOperLog,
      successMessage: '操作日志已清空',
      onSuccess: refreshLogs,
    })
  }

  async function exportExcel() {
    if (exporting.value) return
    exporting.value = true
    try {
      await exportOperLog(getFilters(table.submittedQuery.value))
    } catch {
      /* 下载错误由公共请求层提示。 */
    } finally {
      exporting.value = false
    }
  }

  return {
    registerTable,
    form: table.searchModel,
    canView,
    canClean,
    sysStatusOptions,
    sysOperTypeOptions,
    exportExcel,
    removeOperLogBatch,
    handleCleanOperLog,
    showModal,
    currentOperLog,
    requestSummary,
    responseSummary,
    failureReason,
    getDictOperTypeData,
    getDictStatusData,
  }
}
