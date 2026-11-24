import type { TablePageQuery } from '@/components/table'

export type NoticeId = string | number

export interface NoticeForm {
  title: string
  noticeType: number
  status: number
  contentHtml: string
  publishTime: string | null
}

export interface NoticeSummary extends Omit<NoticeForm, 'contentHtml'> {
  id: NoticeId
  createTime: string
}

export type NoticeUpdate = Omit<NoticeForm, 'contentHtml'> & { id: NoticeId; version: number } & (
  { contentHtml: string } | { content: string }
)

export interface NoticeDetail extends NoticeSummary {
  content: string
  contentHtml?: string | null
  version: number
}

export interface NoticeFilters {
  title: string
  noticeType: number | null
  status: number | null
}

export type NoticeQuery = Partial<NoticeFilters> & TablePageQuery

export interface NoticeItem {
  id: NoticeId
  title: string
  noticeType: number
  publishTime: string
  readFlag: boolean
}

export interface NoticeBox {
  unreadCount: number | string
  unreadCounts: { noticeType: number; unreadCount: number | string }[]
  items: NoticeItem[]
  refreshAfterMillis?: number | null
}
