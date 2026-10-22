/** 上传任务的服务端状态。 */
export type UploadStatus =
  | 'CREATED'
  | 'UPLOADING'
  | 'COMPLETING'
  | 'COMPLETED'
  | 'CANCELING'
  | 'CANCELED'
  | 'EXPIRED'
  | 'FAILED'

/** 仅包含当前用户可见的任务属性。 */
export interface UploadTask {
  id: string
  fileName: string
  fileSize: number
  contentType: string
  fingerprint: string
  partSize: number
  partCount: number
  status: UploadStatus
  directUpload: boolean
  expiresAt: string
  createTime: string
  uploadedParts: number[]
}

export interface UploadOptions {
  maximumFileSize: number
  partSize: number
  concurrency: number
}

export interface AddUploadTask {
  requestId: string
  fileName: string
  fileSize: number
  contentType: string
  fingerprint: string
}

export interface UploadPage {
  data: UploadTask[]
  total: number
}

export interface PartSignature {
  url: string | null
  headers: Record<string, string>
}

export interface HashRequest {
  mode: 'fingerprint' | 'part'
  file: Blob
}

export interface HashReply {
  progress?: number
  value?: string
  checksums?: PartChecksums
  error?: string
}

/** 适配不同对象存储的传输校验算法，摘要在首次签名前固定。 */
export interface PartChecksums {
  contentMd5: string
  contentSha256: string
}

export type UploadPhase =
  'IDLE' | 'HASHING' | 'UPLOADING' | 'PAUSING' | 'PAUSED' | 'COMPLETING' | 'DONE' | 'ERROR'
