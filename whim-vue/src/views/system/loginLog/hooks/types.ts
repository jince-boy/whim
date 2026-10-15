/** 登录日志查询草稿，日期范围在发起请求时转换。 */
export interface LoginLogQuery {
  username: string
  status: number | null
  ipAddress: string
  dateRange: [number, number] | null
}

export interface LoginLog {
  id: string
  username: string
  browser: string
  os: string
  deviceType: string
  loginLocation: string
  ipAddress: string
  status: number
  remark: string
  loginTime: string
}

export interface LoginLogPageResult {
  currentPage: number
  data: LoginLog[]
  pages: number
  size: number
  total: number
}
