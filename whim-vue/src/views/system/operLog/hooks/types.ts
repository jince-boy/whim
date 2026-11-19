export interface OperLog {
  id: string | number
  title: string
  logType: number
  operName: string | null
  operIp: string | null
  operLocation: string | null
  status: number
  operTime: string
  costTime: number
}

export interface OperLogDetail extends OperLog {
  methodName: string | null
  requestMethod: string | null
  requestUrl: string | null
  requestParam: string | null
  responseParam: string | null
  errorMessage: string | null
}

export interface OperLogQuery {
  title: string
  operName: string
  logType: number | null
  status: number | null
  ipAddress: string
  dateRange: [number, number] | null
}

export interface OperLogFilters {
  title?: string
  operName?: string
  ipAddress?: string
  logType?: number
  status?: number
  startTime?: string
  endTime?: string
}

export interface OperLogPageResult {
  currentPage: number
  data: OperLog[]
  pages: number
  size: number
  total: number
}
