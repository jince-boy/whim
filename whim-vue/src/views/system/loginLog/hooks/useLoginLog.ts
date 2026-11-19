import { computed, h, ref } from 'vue'
import { NTag, type DataTableColumns } from 'naive-ui'
import dayjs from 'dayjs'
import { useFormDialog } from '@/components/dialog'
import { TableAction, useWhimTable } from '@/components/table'
import { useDictStore } from '@/stores/modules/dict'
import { useUserStore } from '@/stores/modules/user'
import { cleanLoginLog, deleteLoginLog, exportLoginLog, fetchLoginLogPage } from './api'
import type { LoginLog, LoginLogFilters, LoginLogQuery } from './types'

/** 将已提交的表单条件转换成列表和导出共用的参数。 */
function getLoginLogFilters(query: LoginLogQuery): LoginLogFilters {
  return {
    username: query.username.trim() || undefined,
    ipAddress: query.ipAddress.trim() || undefined,
    status: query.status ?? undefined,
    startTime: query.dateRange
      ? dayjs(query.dateRange[0]).format('YYYY-MM-DD HH:mm:ss')
      : undefined,
    endTime: query.dateRange ? dayjs(query.dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : undefined,
  }
}

/** 管理登录日志的查询、导出和删除。 */
export function useLoginLog() {
  const user = useUserStore()
  const dictionary = useDictStore()
  const exporting = ref(false)
  const canView = computed(() => user.hasPermission('system:loginLog:page'))
  const canDelete = computed(() => user.hasPermission('system:loginLog:delete'))
  const canClean = computed(() => user.hasPermission('system:loginLog:clean'))
  const sysStatusOptions = computed(() => dictionary.getDictNumberOptions('sys_result_status'))
  const { openDeleteDialog } = useFormDialog()
  const columns = computed<DataTableColumns<LoginLog>>(() => {
    const result: DataTableColumns<LoginLog> = [
      { title: '用户名', key: 'username' },
      { title: '登录 IP', key: 'ipAddress' },
      { title: '登录地点', key: 'loginLocation' },
      { title: '浏览器', key: 'browser' },
      { title: '操作系统', key: 'os' },
      { title: '设备类型', key: 'deviceType' },
      {
        title: '登录结果',
        key: 'status',
        render(row) {
          const item = dictionary.getDictData('sys_result_status', row.status)
          return h(NTag, { bordered: false, type: item.listClass }, () => item.label)
        },
      },
      { title: '结果说明', key: 'remark' },
      { title: '登录时间', key: 'loginTime' },
    ]
    if (canDelete.value) {
      result.unshift({ type: 'selection' })
      result.push({
        title: '操作',
        key: 'action',
        fixed: 'right',
        render: (row) =>
          h(TableAction, {
            label: '删除',
            icon: 'trash-2',
            type: 'error',
            onClick: () => removeLoginLogs([row.id]),
          }),
      })
    }
    return result
  })

  const [registerTable, table] = useWhimTable<LoginLog, LoginLogQuery>(() => ({
    title: '登录日志',
    columns,
    scrollX: 1320,
    immediate: canView.value,
    rowKey: (row) => row.id,
    request: ({ pageNum, pageSize, ...query }) =>
      fetchLoginLogPage({ ...getLoginLogFilters(query), pageNum, pageSize }),
    search: {
      defaultValues: { username: '', status: null, ipAddress: '', dateRange: null },
      fields: [
        { key: 'username', label: '用户名' },
        {
          key: 'status',
          label: '登录结果',
          format: (query) =>
            dictionary.getDictData('sys_result_status', String(query.status)).label,
        },
        { key: 'ipAddress', label: 'IP 地址' },
        {
          key: 'dateRange',
          label: '登录时间',
          format: (query) =>
            query.dateRange
              ?.map((time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'))
              .join(' 至 ') ?? '',
        },
      ],
    },
    toolbar: {
      buttons: ['delete', 'export'],
      loading: { export: exporting.value },
      permissions: { delete: ['system:loginLog:delete'], export: ['system:loginLog:export'] },
    },
  }))

  /** 删除后清理已失效的选择并在当前页刷新。 */
  async function refreshLoginLogs() {
    table.setCheckedRowKeys([])
    await table.reload()
  }

  /** 行内删除和批量删除使用相同的接口和确认流程。 */
  function removeLoginLogs(ids: Array<string | number>) {
    if (!ids.length) return
    openDeleteDialog({
      content: `确定删除选中的 ${ids.length} 条登录日志吗？`,
      onSubmit: () => deleteLoginLog({ loginLogIds: ids.join(',') }),
      onSuccess: refreshLoginLogs,
    })
  }

  /** 清空独立授权，确认文案明确操作范围。 */
  function handleCleanLoginLog() {
    openDeleteDialog({
      title: '清空全部登录日志',
      content: '确定清空全部登录日志吗？此操作包含当前筛选范围以外的记录。',
      onSubmit: cleanLoginLog,
      successMessage: '登录日志已清空',
      onSuccess: refreshLoginLogs,
    })
  }

  /** 导出列表当前已提交条件下的全部记录。 */
  async function exportExcel() {
    if (exporting.value) return
    exporting.value = true
    try {
      await exportLoginLog(getLoginLogFilters(table.submittedQuery.value))
    } catch {
      // 下载错误由公共请求层提示。
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
    exportExcel,
    removeLoginLogs,
    handleCleanLoginLog,
  }
}
