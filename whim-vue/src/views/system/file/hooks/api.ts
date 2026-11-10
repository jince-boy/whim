import axiosInstance, { request, downloadFile, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type {
  FileItem,
  FilePageQuery,
  StorageItem,
  StorageSave,
  FileUploadSession,
  FileUploadTaskRequest,
  FileUploadPart,
} from './types'

export const getFilePage = (params: FilePageQuery & TablePageQuery) =>
  request<TablePage<FileItem>>('/system/file/page', RequestMethod.GET, { params })
export const getStorageList = () => request<StorageItem[]>('/system/file/storageList')
export const getUploadLimit = () => request<string | number>('/system/file/uploadLimit')
export const addFileUploadTask = (data: FileUploadTaskRequest, signal: AbortSignal) =>
  request<FileUploadSession>('/system/file/uploadTask', RequestMethod.POST, {
    data,
    signal,
    timeout: 300000,
  })
export const getFileUploadTask = (id: string, signal: AbortSignal) =>
  request<FileUploadSession>('/system/file/uploadTask', RequestMethod.GET, {
    params: { id },
    signal,
    timeout: 300000,
  })
export const completeFileUpload = (id: string, signal: AbortSignal) =>
  request<FileUploadSession>('/system/file/completeUpload', RequestMethod.POST, {
    params: { id },
    signal,
    timeout: 300000,
  })
export const cancelFileUpload = (id: string, signal: AbortSignal) =>
  request<FileUploadSession>('/system/file/cancelUpload', RequestMethod.DELETE, {
    params: { id },
    signal,
    timeout: 300000,
  })

/** 通过现有认证客户端提交一个分片，取消请求不会清除服务端已保存的分片。 */
export function uploadFilePart(
  parameter: FileUploadPart,
  onProgress: (percent: number) => void,
  signal: AbortSignal,
) {
  const data = new FormData()
  data.append('id', parameter.id)
  data.append('partNumber', String(parameter.partNumber))
  data.append('file', parameter.file, 'part')
  return request('/system/file/uploadPart', RequestMethod.POST, {
    data,
    signal,
    timeout: 300000,
    onUploadProgress: (event) => {
      if (event.total) onProgress(Math.min(100, (event.loaded / event.total) * 100))
    },
  })
}
export const addStorage = (data: StorageSave) =>
  request('/system/file/addStorage', RequestMethod.POST, { data: storagePayload(data) })
export const updateStorage = (data: StorageSave) =>
  request('/system/file/updateStorage', RequestMethod.PUT, { data: storagePayload(data) })
export const updateDefaultStorage = (id: string) =>
  request('/system/file/defaultStorage', RequestMethod.PUT, { params: { id } })
export const deleteStorage = (id: string) =>
  request('/system/file/deleteStorage', RequestMethod.DELETE, { params: { id } })
export const deleteFile = (id: string) =>
  request('/system/file/delete', RequestMethod.DELETE, { params: { id } })
export const downloadManagedFile = (file: FileItem) =>
  downloadFile('/system/file/download', file.originalName, {
    params: { id: file.id },
    timeout: 300000,
  })

/** 通过统一认证客户端读取缩略图，204 表示该文件不可预览。 */
export async function getFileThumbnail(id: string, signal: AbortSignal): Promise<Blob | undefined> {
  const response = await axiosInstance.get<Blob>('/system/file/thumbnail', {
    params: { id },
    responseType: 'blob',
    timeout: 35000,
    signal,
  })
  if (response.status === 204) return
  return response.data
}

/** 复用下载接口读取原图，保留认证和取消能力，不触发浏览器另存为。 */
export async function getFilePreview(file: FileItem, signal: AbortSignal): Promise<Blob> {
  const response = await axiosInstance.get<Blob>('/system/file/download', {
    params: { id: file.id },
    responseType: 'blob',
    timeout: 300000,
    signal,
  })
  // 下载接口统一返回二进制类型，预览时使用文件记录中的真实图片类型。
  return response.data.slice(0, response.data.size, file.contentType)
}

/** 只提交可编辑字段，列表中的审计信息和默认标志不进入请求。 */
function storagePayload(model: StorageSave): StorageSave {
  return {
    id: model.id || undefined,
    name: model.name.trim(),
    platform: model.platform,
    basePath: model.basePath.trim(),
    endpoint: model.endpoint.trim(),
    bucket: model.bucket.trim(),
    region: model.region.trim(),
    accessKey: model.accessKey.trim(),
    secretKey: model.secretKey,
  }
}
