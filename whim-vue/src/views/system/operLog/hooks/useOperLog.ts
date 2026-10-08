import dayjs from 'dayjs'
import { type DataTableColumn, NButton, NSpace, NTag, useMessage } from 'naive-ui'
import { cleanOperLog, deleteOperLog, exportOperLog, fetchOperLogPage } from './api'
import type { OperLog } from '@/views/system/operLog/hooks/types.ts'
import { useFormDialog } from '@/components/dialog/useFormDialog.ts'
import { useDict } from '@/components/dict/useDict.ts'
const { dictData: sysStatusOptions, getDictData: getDictStatusData } =
  await useDict('sys_result_status')
const { dictData: sysOperTypeOptions, getDictData: getDictOperTypeData } =
  await useDict('sys_oper_type')
export function useOperLog() {
  const { openDeleteDialog } = useFormDialog()
  const message = useMessage()
  const form = reactive({
    title: '',
    operName: '',
    logType: null,
    status: null,
    ipAddress: '',
    startTime: '',
    endTime: '',
  })
  const showModal = ref(false)
  const currentOperLog: Ref<OperLog | null> = ref(null)
  const pagination = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    pageCount: 0,
  })

  const tableColumns: DataTableColumn[] = [
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

  const tableData = ref<object[]>([])

  const loading = ref(false)

  const dateRange = ref(null)

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
      onSubmit: () => {
        return deleteOperLog({ operLogIds: [row.id] }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getOperLogPage()
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
  const removeOperLogBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个操作日志吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteOperLog({ operLogIds: ids }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getOperLogPage()
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
  const handleCleanOperLog = () => {
    openDeleteDialog({
      content: `确定要清空操作日志吗？删除后无法恢复！`,
      onSubmit: () => {
        return cleanOperLog().then((res) => {
          if (res.code == 200) {
            message.success(`清理成功`)
            getOperLogPage()
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
    await exportOperLog()
  }
  /**
   * 获取登录日志分页
   */
  const getOperLogPage = () => {
    fetchOperLogPage({
      ...form,
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
    }).then((res) => {
      if (res.code === 200) {
        tableData.value = res.data.data
        pagination.page = res.data.currentPage
        pagination.pageSize = res.data.size
        pagination.itemCount = res.data.total
        pagination.pageCount = res.data.pages
        loading.value = false
      }
    })
  }

  /**
   * 时间选择器回调
   * @param timestamps
   */
  const handleDateChange = (timestamps: number[]) => {
    if (timestamps && timestamps.length === 2) {
      form.startTime = dayjs(timestamps[0]).format('YYYY-MM-DD HH:mm:ss')
      form.endTime = dayjs(timestamps[1]).format('YYYY-MM-DD HH:mm:ss')
    } else {
      form.startTime = ''
      form.endTime = ''
    }
  }
  /** 恢复查询条件并清空页面维护的日期范围。 */
  const handleReset = (values: Record<string, unknown>) => {
    Object.assign(form, values)
    dateRange.value = null
  }

  onMounted(() => {
    getOperLogPage()
  })
  return {
    form,
    handleReset,
    sysOperTypeOptions,
    dateRange,
    handleDateChange,
    sysStatusOptions,
    tableColumns,
    tableData,
    pagination,
    loading,
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
