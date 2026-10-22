import axios from 'axios'
import http, { request, RequestMethod, downloadFile, type ApiResponse } from '@/api'
import type {
  AddUploadTask,
  PartSignature,
  PartChecksums,
  UploadOptions,
  UploadPage,
  UploadTask,
} from './types'

const root = '/system/fileUpload'

/** 统一解包业务响应，保留公共请求层的认证和错误处理。 */
function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== 200) throw new Error(response.message || '文件操作失败')
  return response.data
}

/** 获取实际上传约束。 */
export const getUploadOptions = async () => unwrap(await request<UploadOptions>(root + '/options'))

/** 查询当前用户任务列表。 */
export const getUploadTasks = async (pageNum: number) =>
  unwrap(
    await request<UploadPage>(root + '/tasks', RequestMethod.GET, {
      params: { pageNum, pageSize: 10 },
    }),
  )

/** 以固定请求标识创建任务，超时重试不会创建重复记录。 */
export const addUploadTask = async (data: AddUploadTask, signal: AbortSignal) =>
  unwrap(
    await request<UploadTask>(root + '/tasks', RequestMethod.POST, {
      data,
      signal,
      timeout: 60000,
    }),
  )

/** 恢复尚未成功初始化的任务，保留原有效期。 */
export const resumeUpload = async (id: string, signal: AbortSignal) =>
  unwrap(
    await request<UploadTask>(root + '/tasks/' + id + '/resume', RequestMethod.POST, {
      signal,
      timeout: 60000,
    }),
  )

/** 读取存储端确认的进度。 */
export const getUploadTask = async (id: string, signal?: AbortSignal) =>
  unwrap(
    await request<UploadTask>(root + '/tasks/' + id, RequestMethod.GET, {
      signal,
      timeout: 300000,
    }),
  )

/** 登记分片摘要并获取短期直传签名。 */
export const getPartSignature = async (
  id: string,
  number: number,
  checksums: PartChecksums,
  signal: AbortSignal,
) =>
  unwrap(
    await request<PartSignature>(
      root + '/tasks/' + id + '/parts/' + number + '/signature',
      RequestMethod.POST,
      { data: checksums, signal, timeout: 60000 },
    ),
  )

/** 直传使用独立客户端，绝不向对象存储发送应用登录令牌。 */
export async function uploadPart(
  task: UploadTask,
  number: number,
  file: Blob,
  signature: PartSignature,
  signal: AbortSignal,
  progress: (loaded: number) => void,
): Promise<void> {
  if (task.directUpload) {
    if (!signature.url) throw new Error('缺少分片上传地址')
    await axios.put(signature.url, file, {
      headers: signature.headers,
      signal,
      timeout: 300000,
      withCredentials: false,
      onUploadProgress: (event) => progress(Math.min(file.size, event.loaded)),
    })
    return
  }
  const data = new FormData()
  data.append('file', file, 'part')
  unwrap(
    await request(root + '/tasks/' + task.id + '/parts/' + number, RequestMethod.PUT, {
      data,
      signal,
      timeout: 300000,
      onUploadProgress: (event) => progress(Math.min(file.size, event.loaded)),
    }),
  )
}

/** 完成请求可安全重试，响应丢失后由服务端确认最终对象。 */
export const completeUpload = async (id: string, signal?: AbortSignal) =>
  unwrap(
    await request<UploadTask>(root + '/tasks/' + id + '/complete', RequestMethod.POST, {
      signal,
      timeout: 300000,
    }),
  )

/** 取消任务，清理失败仍保留服务端取消意图。 */
export const cancelUpload = async (id: string) =>
  unwrap(await request(root + '/tasks/' + id, RequestMethod.DELETE, { timeout: 60000 }))

/** 支持文件系统访问的浏览器流式保存大文件，其余浏览器仅允许有界缓冲的小文件。 */
export async function downloadUploadedFile(task: UploadTask): Promise<void> {
  const picker = (
    window as Window & {
      showSaveFilePicker?: (options: { suggestedName: string }) => Promise<{
        createWritable: () => Promise<WritableStream<Uint8Array>>
      }>
    }
  ).showSaveFilePicker
  if (!picker) {
    if (task.fileSize > 20 * 1024 * 1024) {
      throw new Error('当前浏览器不支持大文件流式保存，请使用最新版 Chrome 或 Edge 的 HTTPS 页面')
    }
    await downloadFile(root + '/tasks/' + task.id + '/download', task.fileName, { timeout: 300000 })
    return
  }
  // 文件选择器必须在用户点击的同步调用链中打开。
  const handle = await picker.call(window, { suggestedName: task.fileName })
  const output = await handle.createWritable()
  try {
    const response = await http.get<ReadableStream<Uint8Array>>(
      root + '/tasks/' + task.id + '/download',
      {
        adapter: 'fetch',
        responseType: 'stream',
        timeout: 300000,
      },
    )
    await response.data.pipeTo(output)
  } catch (error) {
    await output.abort().catch(() => undefined)
    throw error
  }
}
