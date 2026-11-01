import { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { OrganizationStatusUpdate } from '@/views/system/dept/hooks/types'
import type { PostId, PostItem, PostMember, PostMemberQuery, PostQuery, PostSave } from './types'

export const fetchPostPage = (params: PostQuery & TablePageQuery) =>
  request<TablePage<PostItem>>('/system/post/page', RequestMethod.GET, { params })

export const fetchPostDetail = (id: PostId) =>
  request<PostItem>(`/system/post/${id}`, RequestMethod.GET)

export const addPost = (data: PostSave) =>
  request<PostId>('/system/post', RequestMethod.POST, { data: getPostPayload(data) })

export const updatePost = (data: PostSave) =>
  request('/system/post', RequestMethod.PUT, { data: getPostPayload(data) })

export const updatePostStatus = (data: OrganizationStatusUpdate) =>
  request('/system/post/status', RequestMethod.PUT, { data })

export const deletePost = (id: PostId) => request(`/system/post/${id}`, RequestMethod.DELETE)

export const fetchPostMemberPage = (
  id: PostId,
  params: PostMemberQuery & TablePageQuery & { assigned: boolean },
) => request<TablePage<PostMember>>(`/system/post/${id}/members`, RequestMethod.GET, { params })

/** 增量添加明确选择的用户，不替换整个成员集合。 */
export const addPostMembers = (id: PostId, userIds: Array<string | number>) =>
  request(`/system/post/${id}/members`, RequestMethod.POST, { data: { userIds } })

export const deletePostMembers = (id: PostId, userIds: Array<string | number>) =>
  request(`/system/post/${id}/members`, RequestMethod.DELETE, { data: { userIds } })

/** 全组织岗位和清空备注使用 null，保留长整型 ID 的原始精度。 */
function getPostPayload(data: PostSave): PostSave {
  return {
    ...data,
    deptId: data.deptId?.trim() || null,
    postName: data.postName.trim(),
    postCode: data.postCode.trim(),
    remark: data.remark?.trim() || null,
  }
}
