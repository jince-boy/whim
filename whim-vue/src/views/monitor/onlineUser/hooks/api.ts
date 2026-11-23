import { request, RequestMethod } from '@/api'
import type { TablePageQuery } from '@/components/table'
import type { OnlineUserPage, OnlineUserQuery, OnlineUserSession } from './types'

/** 分页查询授权范围内的在线会话。 */
export const fetchOnlineUserPage = (params: Partial<OnlineUserQuery> & TablePageQuery) =>
  request<OnlineUserPage>('/system/monitor/onlineUser/page', RequestMethod.GET, { params })

/** 撤销一个终端的会话。 */
export const kickoutOnlineUser = (data: Pick<OnlineUserSession, 'userId' | 'sessionId'>) =>
  request<void>('/system/monitor/onlineUser/kickout', RequestMethod.POST, { data })
