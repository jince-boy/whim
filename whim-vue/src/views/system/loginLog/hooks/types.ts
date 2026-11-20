/** 登录日志查询草稿，日期范围在发起请求时转换。 */
export interface LoginLogQuery {
  username: string
  status: number | null
  ipAddress: string
  dateRange: [number, number] | null
}

export interface LoginLog {
  id: string | number
  username: string
  browser: string | null
  os: string | null
  deviceType: string | null
  loginLocation: string | null
  ipAddress: string | null
  status: number
  remark: string
  loginTime: string
}

export interface LoginLogFilters {
  username?: string
  status?: number
  ipAddress?: string
  startTime?: string
  endTime?: string
}
