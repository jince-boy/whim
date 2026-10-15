export interface OperLog {
  id: string
  title: string
  logType: number
  methodName: string
  requestMethod: string
  operName: string
  operIp: string
  operLocation: string
  requestUrl: string
  requestParam: string
  responseParam: string
  status: number
  errorMessage: string
  operTime: string
  costTime: number
}

/** 操作日志查询草稿，日期范围随表单整体重置。 */
export interface OperLogQuery {
  title: string
  operName: string
  logType: number | null
  status: number | null
  ipAddress: string
  dateRange: [number, number] | null
}

export interface OperLogResult {
  id: string
  title: string
  logType: number
  methodName: string
  requestMethod: string
  operName: string
  operIp: string
  requestUrl: string
  requestParam: string
  responseParam: string
  status: number
  errorMessage: string
  operTime: string
  costTime: number
}

export interface OperLogPageResult {
  currentPage: number
  data: OperLogResult[]
  pages: number
  size: number
  total: number
}
