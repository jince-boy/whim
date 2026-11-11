import { useWhimTable } from '@/components/table'
import type { LoginLog, LoginLogQuery } from '@/views/system/loginLog/hooks/types.ts'
import { cleanLoginLog, deleteLoginLog, exportLoginLog, fetchLoginLogPage } from './api'
import { type DataTableColumn, NButton, NSpace, NTag } from 'naive-ui'
import dayjs from 'dayjs'
import { useFormDialog } from '@/components/dialog'
import { computed, ref } from 'vue'
import { useDictStore } from '@/stores/modules/dict'
/** 配置注册式列表并处理登录日志业务操作。 */
export function useLoginLog() {
  const dictionary = useDictStore()
  const exporting = ref(false)
  const sysStatusOptions = computed(() => dictionary.getDictOptions('sys_result_status'))
  const { openDeleteDialog } = useFormDialog()
  const tableColumns: DataTableColumn<LoginLog>[] = [
    {
      type: 'selection',
    },
    {
      title: '用户名',
      key: 'username',
      align: 'center',
    },
    {
      title: '登录IP',
      key: 'ipAddress',
      align: 'center',
    },
    {
      title: '登录地点',
      key: 'loginLocation',
      align: 'center',
    },
    {
      title: '浏览器',
      key: 'browser',
      align: 'center',
    },
    {
      title: '操作系统',
      key: 'os',
      align: 'center',
    },
    {
      title: '设备类型',
      key: 'deviceType',
      align: 'center',
    },
    {
      title: '登录状态',
      key: 'status',
      align: 'center',
      render(row: object) {
        const loginLog = row as LoginLog
        const dictData = dictionary.getDictData('sys_result_status', loginLog.status)
        return h(
          NTag,
          {
            bordered: false,
            type: dictData!.listClass,
          },
          { default: () => dictData!.label },
        )
      },
    },
    {
      title: '备注',
      key: 'remark',
      align: 'center',
    },
    {
      title: '登录时间',
      key: 'loginTime',
      align: 'center',
    },
    {
      title: '操作',
      key: 'action',
      align: 'center',
      render(row: object) {
        const loginLog = row as LoginLog
        return h(NSpace, { justify: 'center' }, () => [
          h(
            NButton,
            {
              text: true,
              type: 'error',
              onClick: () => {
                removeLoginLog(loginLog)
              },
            },
            { default: () => '删除' },
          ),
        ])
      },
    },
  ]

  const [registerTable, { searchModel: form, reload: getLoginLogPage }] = useWhimTable<
    LoginLog,
    LoginLogQuery
  >(() => ({
    columns: tableColumns,
    rowKey: (row) => row.id,
    request: ({ dateRange, ...query }) =>
      fetchLoginLogPage({
        ...query,
        startTime: dateRange ? dayjs(dateRange[0]).format('YYYY-MM-DD HH:mm:ss') : '',
        endTime: dateRange ? dayjs(dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : '',
      }),
    search: {
      defaultValues: { username: '', status: null, ipAddress: '', dateRange: null },
      fields: [
        { key: 'username', label: '用户名' },
        {
          key: 'status',
          label: '登录状态',
          format: (values) => dictionary.getDictData('sys_result_status', String(values.status)).label,
        },
        { key: 'ipAddress', label: 'IP 地址' },
        {
          key: 'dateRange',
          label: '登录时间',
          format: (values) =>
            values.dateRange
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

  /**
   * 删除登录日志
   * @param row
   */
  const removeLoginLog = (row: LoginLog) => {
    openDeleteDialog({
      content: `确定要删除${row.username}这条登录日志吗？删除后无法恢复！`,
      onSubmit: () => deleteLoginLog({ loginLogIds: [row.id] }),
      onSuccess: getLoginLogPage,
    })
  }
  /**
   * 批量删除登录日志
   */
  const removeLoginLogBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个登录日志吗？删除后无法恢复！`,
      onSubmit: () => deleteLoginLog({ loginLogIds: ids }),
      onSuccess: getLoginLogPage,
    })
  }

  /**
   * 清理登录日志
   */
  const handleCleanLoginLog = () => {
    openDeleteDialog({
      content: `确定要清空登录日志吗？删除后无法恢复！`,
      title: '清空确认',
      onSubmit: cleanLoginLog,
      successMessage: '清理成功',
      onSuccess: getLoginLogPage,
    })
  }
  /**
   * 导出登录日志，等待下载结束后恢复按钮。
   */
  const exportExcel = async () => {
    if (exporting.value) return
    exporting.value = true
    try {
      await exportLoginLog()
    } catch {
      // 下载错误由公共请求层提示，失败后允许重试。
    } finally {
      exporting.value = false
    }
  }

  return {
    registerTable,
    form,
    sysStatusOptions,
    getLoginLogPage,
    exportExcel,
    removeLoginLogBatch,
    handleCleanLoginLog,
  }
}
