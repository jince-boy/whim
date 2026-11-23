import axiosInstance, { request, RequestMethod } from '@/api'
import { message } from '@/plugins'
import type { TablePage, TablePageQuery } from '@/components/table'
import type {
  NoticeBox,
  NoticeDetail,
  NoticeForm,
  NoticeId,
  NoticeItem,
  NoticeQuery,
  NoticeSummary,
} from './types'

const base = '/system/notice'

/** 上传到项目默认存储，只将后端返回的文件ID写入公告。 */
export async function uploadNoticeImage(file: File, signal: AbortSignal): Promise<string> {
  const data = new FormData()
  data.append('file', file)
  const response = await request<string>(`${base}/uploadImage`, RequestMethod.POST, {
    data,
    signal,
    timeout: 60000,
  })
  if (response.code !== 200 || !/^[1-9]\d{0,18}$/.test(String(response.data))) {
    message.error(response.message || '公告图片上传失败')
    throw new Error('公告图片上传失败')
  }
  return String(response.data)
}

export async function fetchNoticeImage(
  params: { noticeId: NoticeId; fileId: string },
  signal: AbortSignal,
): Promise<Blob> {
  const response = await axiosInstance.get<Blob>(`${base}/image`, {
    params,
    signal,
    responseType: 'blob',
    timeout: 35000,
  })
  if (!response.data.size || response.data.type !== 'image/png') throw new Error('公告图片不可用')
  return response.data
}

export const fetchNoticePage = (params: NoticeQuery) =>
  request<TablePage<NoticeSummary>>(`${base}/page`, RequestMethod.GET, { params })

export const fetchNoticeDetail = (params: { id: NoticeId }) =>
  request<NoticeDetail>(`${base}/detail`, RequestMethod.GET, { params })

export const addNotice = (data: NoticeForm) => request(`${base}/add`, RequestMethod.POST, { data })

export const updateNotice = (data: NoticeForm & { id: NoticeId; version: number }) =>
  request(`${base}/update`, RequestMethod.PUT, { data })

export const deleteNotice = (params: { id: NoticeId }) =>
  request(`${base}/delete`, RequestMethod.DELETE, { params })

export const fetchNoticeBox = (params: { noticeType: number }, signal: AbortSignal) =>
  request<NoticeBox>(`${base}/box`, RequestMethod.GET, { params, signal })

export const fetchNoticeInbox = (
  params: TablePageQuery & { noticeType: number },
  signal: AbortSignal,
) => request<TablePage<NoticeItem>>(`${base}/inbox`, RequestMethod.GET, { params, signal })

/** 按确认时的消息边界清空当前账号的系统消息。 */
export const clearSystemMessages = (params: { throughId: NoticeId }, signal: AbortSignal) =>
  request<void>(`${base}/clearSystemMessages`, RequestMethod.DELETE, { params, signal })

export const readNotice = (params: { id: NoticeId }, signal: AbortSignal) =>
  request<NoticeDetail>(`${base}/read`, RequestMethod.POST, { params, signal })
