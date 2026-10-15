import { useWhimTable } from '@/components/table'
import type { LoginLog, LoginLogQuery } from '@/views/system/loginLog/hooks/types.ts'
import { cleanLoginLog, deleteLoginLog, exportLoginLog, fetchLoginLogPage } from './api'
import { type DataTableColumn, NButton, NSpace, NTag, useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { useFormDialog } from '@/components/dialog/useFormDialog.ts'
import { useDict } from '@/components/dict/useDict.ts'
const { dictData: sysStatusOptions, getDictData } = await useDict('sys_result_status')
/** 配置注册式列表并处理登录日志业务操作。 */
export function useLoginLog() {
  const { openDeleteDialog } = useFormDialog()
  const message = useMessage()
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
        const dictData = getDictData(String(loginLog.status))
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
  >({
    columns: tableColumns,
    rowKey: (row) => row.id,
    request: ({ dateRange, ...query }) =>
      fetchLoginLogPage({
        ...query,
        startTime: dateRange ? dayjs(dateRange[0]).format('YYYY-MM-DD HH:mm:ss') : '',
        endTime: dateRange ? dayjs(dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : '',
      }),
    search: { defaultValues: { username: '', status: null, ipAddress: '', dateRange: null } },
    toolbar: {
      buttons: ['delete', 'export'],
      permissions: { delete: ['system:loginLog:delete'], export: ['system:loginLog:export'] },
    },
  })

  /**
   * 删除登录日志
   * @param row
   */
  const removeLoginLog = (row: LoginLog) => {
    openDeleteDialog({
      content: `确定要删除${row.username}这条登录日志吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteLoginLog({ loginLogIds: [row.id] }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getLoginLogPage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }
  /**
   * 批量删除登录日志
   */
  const removeLoginLogBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个登录日志吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteLoginLog({ loginLogIds: ids }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getLoginLogPage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }

  /**
   * 清理登录日志
   */
  const handleCleanLoginLog = () => {
    openDeleteDialog({
      content: `确定要清空登录日志吗？删除后无法恢复！`,
      onSubmit: () => {
        return cleanLoginLog().then((res) => {
          if (res.code == 200) {
            message.success(`清理成功`)
            getLoginLogPage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }
  /**
   * 导出字典类型
   */
  const exportExcel = async () => {
    await exportLoginLog()
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
