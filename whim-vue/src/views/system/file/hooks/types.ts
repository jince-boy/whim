export interface StorageItem {
  id: string
  name: string
  platform: string
  basePath: string
  endpoint: string
  bucket: string
  region: string
  accessKey: string
  defaultFlag: number | null
  createTime: string
}
export interface StorageSave {
  id?: string
  name: string
  platform: string
  basePath: string
  endpoint: string
  bucket: string
  region: string
  accessKey: string
  secretKey: string
}
export interface StorageFormProps {
  initialModel?: StorageItem
}
export interface FileItem {
  id: string
  storageId: string
  storageName: string
  platform: string
  originalName: string
  objectKey: string
  fileSize: string | number
  contentType: string
  createTime: string
}
export interface FileQuery {
  originalName: string
  storageId: string | null
}
