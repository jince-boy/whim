import { useWhimTable } from '@/components/table'
import dayjs from 'dayjs'
import { type DataTableColumn, NButton, NSpace, NTag } from 'naive-ui'
import { cleanOperLog, deleteOperLog, exportOperLog, fetchOperLogPage } from './api'
import type { OperLog, OperLogQuery, OperLogResult } from '@/views/system/operLog/hooks/types.ts'
import { useFormDialog } from '@/components/dialog'
import { useDict } from '@/components/dict/useDict.ts'
const { dictData: sysStatusOptions, getDictData: getDictStatusData } =
  await useDict('sys_result_status')
const { dictData: sysOperTypeOptions, getDictData: getDictOperTypeData } =
  await useDict('sys_oper_type')
/** 配置注册式列表并处理操作日志业务操作。 */
export function useOperLog() {
  const { openDeleteDialog } = useFormDialog()
  const showModal = ref(false)
  const currentOperLog: Ref<OperLog | null> = ref(null)

  const tableColumns: DataTableColumn<OperLogResult>[] = [
    {
      type: 'selection',
    },
    {
      title: '操作人',
      key: 'operName',
      align: 'center',
    },
    {
      title: '模块名称',
      key: 'title',
      align: 'center',
    },
    {
      title: '操作类型',
      key: 'operType',
      align: 'center',
      render(row: object) {
        const operLog = row as OperLog
        const dictData = getDictOperTypeData(String(operLog.logType))
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
      title: '操作IP',
      key: 'operIp',
      align: 'center',
    },
    {
      title: '操作地点',
      key: 'operLocation',
      align: 'center',
    },
    {
      title: '登录状态',
      key: 'status',
      align: 'center',
      render(row: object) {
        const operLog = row as OperLog
        const dictData = getDictStatusData(String(operLog.status))
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
      title: '操作时间',
      key: 'operTime',
      align: 'center',
    },
    {
      title: '耗时',
      key: 'costTime',
      align: 'center',
      render(row: object) {
        const operLog = row as OperLog
        return h('span', null, { default: () => operLog.costTime + 'ms' })
      },
    },
    {
      title: '操作',
      key: 'action',
      align: 'center',
      render(row: object) {
        const operLog = row as OperLog
        return h(NSpace, { justify: 'center' }, () => [
          h(
            NButton,
            {
              text: true,
              type: 'info',
              onClick: () => {
                showOperLogDetail(operLog)
              },
            },
            { default: () => '查看' },
          ),
          h(
            NButton,
            {
              text: true,
              type: 'error',
              onClick: () => {
                removeOperLog(operLog)
              },
            },
            { default: () => '删除' },
          ),
        ])
      },
    },
  ]

  const [registerTable, { searchModel: form, reload: getOperLogPage }] = useWhimTable<
    OperLogResult,
    OperLogQuery
  >({
    columns: tableColumns,
    rowKey: (row) => row.id,
    request: ({ dateRange, ...query }) =>
      fetchOperLogPage({
        ...query,
        startTime: dateRange ? dayjs(dateRange[0]).format('YYYY-MM-DD HH:mm:ss') : '',
        endTime: dateRange ? dayjs(dateRange[1]).format('YYYY-MM-DD HH:mm:ss') : '',
      }),
    search: {
      defaultValues: {
        title: '',
        operName: '',
        logType: null,
        status: null,
        ipAddress: '',
        dateRange: null,
      },
    },
    toolbar: {
      buttons: ['delete', 'export'],
      permissions: { delete: ['system:operLog:delete'], export: ['system:operLog:export'] },
    },
  })

  /**
   * 查看登录日志详情
   * @param value
   */
  const showOperLogDetail = (value: OperLog) => {
    showModal.value = true
    currentOperLog.value = value
  }
  /**
   * 删除登录日志
   * @param row
   */
  const removeOperLog = (row: OperLog) => {
    openDeleteDialog({
      content: `确定要删除这条操作日志吗？删除后无法恢复！`,
      onSubmit: () => deleteOperLog({ operLogIds: [row.id] }),
      onSuccess: getOperLogPage,
    })
  }
  /**
   * 批量删除登录日志
   */
  const removeOperLogBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个操作日志吗？删除后无法恢复！`,
      onSubmit: () => deleteOperLog({ operLogIds: ids }),
      onSuccess: getOperLogPage,
    })
  }

  /**
   * 清理登录日志
   */
  const handleCleanOperLog = () => {
    openDeleteDialog({
      content: `确定要清空操作日志吗？删除后无法恢复！`,
      title: '清空确认',
      onSubmit: cleanOperLog,
      successMessage: '清理成功',
      onSuccess: getOperLogPage,
    })
  }
  /**
   * 导出字典类型
   */
  const exportExcel = async () => {
    await exportOperLog()
  }
  return {
    registerTable,
    form,
    sysOperTypeOptions,
    sysStatusOptions,
    getOperLogPage,
    exportExcel,
    removeOperLogBatch,
    handleCleanOperLog,
    showModal,
    currentOperLog,
    getDictOperTypeData,
    getDictStatusData,
  }
}
