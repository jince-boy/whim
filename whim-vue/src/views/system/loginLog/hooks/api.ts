import { downloadFile, request, RequestMethod } from '@/api'
import type { LoginLogFilters, LoginLogPageResult } from './types'

/** 分页查询登录日志。 */
export const fetchLoginLogPage = (
  params: LoginLogFilters & { pageNum: number; pageSize: number },
) => {
  return request<LoginLogPageResult>('/system/loginLog/page', RequestMethod.GET, { params })
}

/** 导出登录日志。 */
export const exportLoginLog = (params: LoginLogFilters) => {
  return downloadFile('/system/loginLog/export', null, { params })
}

/** 删除指定登录日志。 */
export const deleteLoginLog = (params: { loginLogIds: string }) => {
  return request('/system/loginLog/delete', RequestMethod.DELETE, { params })
}

/** 清空登录日志。 */
export const cleanLoginLog = () => {
  return request('/system/loginLog/clean', RequestMethod.DELETE)
}
