export interface LoginLogForm {
  username: string
  status: number | null // 假设状态是数字类型
  ipAddress: string
  startTime: string // 前端使用 Date 对象
  endTime: string
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
