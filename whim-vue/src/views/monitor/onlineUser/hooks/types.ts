import type { TablePage } from '@/components/dataTable'

export interface OnlineUserQuery {
  username: string
  ipAddress: string
}

export interface OnlineUserSession {
  sessionId: string
  userId: string | number
  username: string
  clientType: string | null
  deviceType: string | null
  name: string | null
  deptName: string | null
  browser: string | null
  os: string | null
  ipAddress: string | null
  loginLocation: string | null
  loginTime: string
  lastActiveTime: string | null
  current: boolean
  canKickout: boolean
}

export interface OnlineUserPage extends TablePage<OnlineUserSession> {
  accountCount: number
}
