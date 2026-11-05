import { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { PostId, PostItem, PostMember, PostMemberQuery, PostQuery, PostSave } from './types'

export const fetchPostPage = (params: PostQuery & TablePageQuery) =>
  request<TablePage<PostItem>>('/system/post/page', RequestMethod.GET, { params })

export const fetchPostDetail = (id: PostId) =>
  request<PostItem>('/system/post/detail', RequestMethod.GET, { params: { id } })

export const addPost = (data: PostSave) =>
  request('/system/post/add', RequestMethod.POST, { data: getPostPayload(data) })

export const updatePost = (data: PostSave) =>
  request('/system/post/update', RequestMethod.PUT, { data: getPostPayload(data) })

export const deletePost = (id: PostId) =>
  request('/system/post/delete', RequestMethod.DELETE, { params: { id } })

export const fetchPostMemberPage = (
  params: PostMemberQuery & TablePageQuery & { postId: PostId },
) => request<TablePage<PostMember>>('/system/post/memberPage', RequestMethod.GET, { params })

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
