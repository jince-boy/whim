import { request, downloadFile, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { FileItem, FileQuery, StorageItem, StorageSave } from './types'

export const getFilePage = (params: FileQuery & TablePageQuery) =>
  request<TablePage<FileItem>>('/system/file/page', RequestMethod.GET, { params })
export const getStorageList = () => request<StorageItem[]>('/system/file/storageList')
export const getUploadLimit = () => request<string | number>('/system/file/uploadLimit')
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

/** 仅提交文件，由后端统一选择默认配置；浏览器负责生成 multipart 边界。 */
export function uploadFile(file: File, onProgress: (percent: number) => void) {
  const data = new FormData()
  data.append('file', file)
  return request<FileItem>('/system/file/upload', RequestMethod.POST, {
    data,
    timeout: 300000,
    onUploadProgress: (event) => {
      if (event.total) onProgress(Math.round((event.loaded / event.total) * 100))
    },
  })
}
