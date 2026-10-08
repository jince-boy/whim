import { downloadFile, request, RequestMethod } from '@/api'
import type { LoginLogPageResult } from './types'

/** 分页查询登录日志。 */
export const fetchLoginLogPage = (params: object) => {
  return request<LoginLogPageResult>('/system/loginLog/page', RequestMethod.GET, { params })
}

/** 导出登录日志。 */
export const exportLoginLog = () => {
  return downloadFile('/system/loginLog/export')
}

/** 删除指定登录日志。 */
export const deleteLoginLog = (params: object) => {
  return request('/system/loginLog/delete', RequestMethod.DELETE, { params })
}

/** 清空登录日志。 */
export const cleanLoginLog = () => {
  return request('/system/loginLog/clean', RequestMethod.DELETE)
}
