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
  uploadSource: string
  fileName: string
  originalName: string
  fileSuffix: string
  objectKey: string
  fileSize: string | number
  contentType: string
  createBy: string | number | null
  createByName: string | null
  createTime: string
}
export interface FileQuery {
  fileName: string
  originalName: string
  fileSuffix: string
  storageId: string | null
  platform: string | null
  uploadSource: string | null
  dateRange: [number, number] | null
}

export interface FileUploadTaskRequest {
  requestId: string
  originalName: string
  fileSize: number
  contentType: string
  uploadSource: string
}

export interface FileUploadSession {
  id: string
  partSize: number
  fileSize: string | number
  uploadedParts: number[]
  fileId: string | null
  cancelled: boolean
  expiresAt: string
}

export interface FileUploadPart {
  id: string
  partNumber: number
  file: Blob
}

/** 仅用于浏览器上传交互，不属于业务字典。 */
export type FileUploadState =
  | 'waiting'
  | 'uploading'
  | 'paused'
  | 'completing'
  | 'success'
  | 'error'
  | 'cancelling'
  | 'cancelFailed'
  | 'cancelled'

export interface FileUploadTask {
  id: string
  request: FileUploadTaskRequest
  file: File | null
  sessionId?: string
  started: boolean
  state: FileUploadState
  confirmedBytes: number
  transferredBytes: number
  error: string
}

export interface FileUploadView {
  id: string
  name: string
  state: FileUploadState
  stateLabel: string
  progress: number
  progressStatus: 'default' | 'success' | 'error' | 'warning'
  sizeLabel: string
  transferredLabel: string
  error: string
}

export interface FileUploadSummary {
  total: number
  active: number
  finished: number
  failed: number
  pausable: number
  resumable: number
  clearable: number
}

export interface FilePageQuery extends Omit<FileQuery, 'dateRange'> {
  startTime?: string
  endTime?: string
}

export interface FileThumbnailProps {
  file: FileItem
  canPreview: boolean
}
