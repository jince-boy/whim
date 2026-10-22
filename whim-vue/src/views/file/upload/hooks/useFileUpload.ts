import axios from 'axios'
import {
  computed,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
  shallowRef,
  useTemplateRef,
  watch,
} from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import * as api from './api'
import type {
  AddUploadTask,
  HashReply,
  HashRequest,
  UploadOptions,
  UploadPhase,
  UploadTask,
} from './types'

/** 管理单个活动上传和服务器任务列表，暂停只停止传输，取消才清理服务器任务。 */
export function useFileUpload() {
  const fileInput = useTemplateRef<HTMLInputElement>('fileInput')
  const auth = useAuthStore()
  const options = shallowRef<UploadOptions>()
  const tasks = shallowRef<UploadTask[]>([])
  const task = shallowRef<UploadTask>()
  const selectedFile = shallowRef<File>()
  const phase = ref<UploadPhase>('IDLE')
  const running = ref(false)
  const loading = ref(false)
  const errorMessage = ref('')
  const hashProgress = ref(0)
  const transferred = ref(0)
  const page = ref(1)
  const total = ref(0)
  const downloadId = ref('')
  let resumeTarget: UploadTask | undefined
  let fingerprint = ''
  let request: AddUploadTask | undefined
  let controller: AbortController | undefined
  let disposed = false
  let listVersion = 0
  const progress = computed(() => {
    const size = selectedFile.value?.size || task.value?.fileSize || 0
    return size ? Math.min(100, Math.floor((transferred.value * 100) / size)) : 0
  })
  const phaseLabel = computed(
    () =>
      ({
        IDLE: '选择文件开始上传',
        HASHING: '正在校验文件',
        UPLOADING: '正在上传',
        PAUSING: '正在暂停',
        PAUSED: '已暂停',
        COMPLETING: '正在确认文件完整性',
        DONE: '上传完成',
        ERROR: '上传未完成，可以重试',
      })[phase.value],
  )

  /** 格式化容量，保持字节数来源不变。 */
  function formatSize(size: number): string {
    if (size < 1024) return size + ' B'
    if (size < 1024 ** 2) return (size / 1024).toFixed(1) + ' KiB'
    if (size < 1024 ** 3) return (size / 1024 ** 2).toFixed(1) + ' MiB'
    return (size / 1024 ** 3).toFixed(2) + ' GiB'
  }

  /** 显示服务器持久化状态。 */
  function statusLabel(status: UploadTask['status']): string {
    return {
      CREATED: '待初始化',
      UPLOADING: '可继续上传',
      COMPLETING: '确认合并中',
      COMPLETED: '已完成',
      CANCELING: '正在清理',
      CANCELED: '已取消',
      EXPIRED: '已过期',
      FAILED: '分片已丢失',
    }[status]
  }

  /** 刷新列表时丢弃旧页及旧登录会话的迟到响应。 */
  async function refreshTasks(): Promise<void> {
    const version = ++listVersion
    const token = auth.getToken
    loading.value = true
    try {
      const result = await api.getUploadTasks(page.value)
      if (disposed || version !== listVersion || token !== auth.getToken) return
      tasks.value = result.data
      total.value = Number(result.total)
    } catch (error) {
      if (!disposed && token === auth.getToken) errorMessage.value = getMessage(error)
    } finally {
      if (version === listVersion) loading.value = false
    }
  }

  /** 切换分页后读取对应数据。 */
  async function changePage(value: number): Promise<void> {
    page.value = value
    await refreshTasks()
  }

  /** 打开文件选择器；续传必须由用户重新授权读取原文件。 */
  function chooseFile(existing?: UploadTask): void {
    if (running.value || !options.value) return
    resumeTarget = existing
    if (fileInput.value) {
      fileInput.value.value = ''
      fileInput.value.click()
    }
  }

  /** 接受文件选择并重置当前上传的本地状态。 */
  async function selectFile(event: Event): Promise<void> {
    const file = (event.target as HTMLInputElement).files?.[0]
    if (!file || !options.value) return
    if (!file.size || file.size > options.value.maximumFileSize) {
      errorMessage.value = '请选择非空文件，大小不超过 ' + formatSize(options.value.maximumFileSize)
      return
    }
    if (
      resumeTarget &&
      (resumeTarget.fileSize !== file.size || resumeTarget.fileName !== file.name)
    ) {
      errorMessage.value = '请选择该任务对应的原文件'
      return
    }
    selectedFile.value = file
    task.value = resumeTarget
    fingerprint = ''
    request = undefined
    transferred.value = 0
    hashProgress.value = 0
    errorMessage.value = ''
    await startUpload()
  }

  /** 创建可取消的校验 Worker，取消时立即终止并释放文件引用。 */
  function hashFile(
    file: Blob,
    mode: HashRequest['mode'],
    signal: AbortSignal,
  ): Promise<HashReply> {
    signal.throwIfAborted()
    return new Promise((resolve, reject) => {
      const worker = new Worker(new URL('./hash.worker.ts', import.meta.url), { type: 'module' })
      /** 结束 Worker 并移除取消监听。 */
      function release(): void {
        worker.terminate()
        signal.removeEventListener('abort', abort)
      }
      /** 上传暂停时中断文件校验。 */
      function abort(): void {
        release()
        reject(new DOMException('上传已暂停', 'AbortError'))
      }
      signal.addEventListener('abort', abort, { once: true })
      worker.onmessage = (event: MessageEvent<HashReply>) => {
        if (event.data.error) {
          release()
          reject(new Error(event.data.error))
        } else if (event.data.value || event.data.checksums) {
          release()
          resolve(event.data)
        } else if (mode === 'fingerprint' && event.data.progress !== undefined) {
          hashProgress.value = Math.floor(event.data.progress * 100)
        }
      }
      worker.onerror = () => {
        release()
        reject(new Error('文件校验线程运行失败'))
      }
      worker.postMessage({ mode, file } satisfies HashRequest)
    })
  }

  /** 有限重试临时网络、锁冲突和服务错误；每次重试分片会重新申请签名。 */
  async function retry<T>(
    operation: () => Promise<T>,
    signal: AbortSignal,
    direct = false,
  ): Promise<T> {
    for (let attempt = 0; ; attempt++) {
      signal.throwIfAborted()
      try {
        return await operation()
      } catch (error) {
        if (signal.aborted || axios.isCancel(error) || attempt >= 3) throw error
        const status = axios.isAxiosError(error) ? error.response?.status : undefined
        if (status && status !== 429 && status < 500 && !(direct && status === 403)) throw error
        if (!axios.isAxiosError(error)) throw error
        await new Promise<void>((resolve, reject) => {
          /** 取消重试等待，不留下计时器。 */
          function abort(): void {
            clearTimeout(timer)
            reject(new DOMException('上传已暂停', 'AbortError'))
          }
          const timer = window.setTimeout(
            () => {
              signal.removeEventListener('abort', abort)
              resolve()
            },
            500 * 2 ** attempt + Math.random() * 200,
          )
          signal.addEventListener('abort', abort, { once: true })
        })
      }
    }
  }

  /** 从真实进度继续上传；所有工作线程结束前不允许启动下一轮，避免迟到写入污染状态。 */
  async function startUpload(): Promise<void> {
    const file = selectedFile.value
    const config = options.value
    if (running.value || !file || !config) return
    running.value = true
    errorMessage.value = ''
    const active = new AbortController()
    controller = active
    const signal = active.signal
    const token = auth.getToken
    let failure: unknown
    try {
      if (!fingerprint) {
        phase.value = 'HASHING'
        fingerprint = (await hashFile(file, 'fingerprint', signal)).value!
      }
      if (task.value && task.value.fingerprint !== fingerprint) {
        throw new Error('文件内容与原任务不一致，请重新选择原文件')
      }
      if (!task.value) {
        request ??= {
          requestId: crypto.randomUUID().replaceAll('-', ''),
          fileName: file.name,
          fileSize: file.size,
          contentType: file.type || 'application/octet-stream',
          fingerprint,
        }
        task.value = await retry(() => api.addUploadTask(request!, signal), signal)
      } else if (task.value.status === 'CREATED') {
        task.value = await retry(() => api.resumeUpload(task.value!.id, signal), signal)
      }
      const current = await retry(() => api.getUploadTask(task.value!.id, signal), signal)
      task.value = current
      if (current.status === 'COMPLETED') {
        transferred.value = file.size
        phase.value = 'DONE'
        return
      }
      if (current.status !== 'UPLOADING' && current.status !== 'COMPLETING') {
        throw new Error('任务已结束，无法继续上传')
      }
      if (current.status === 'UPLOADING') {
        phase.value = 'UPLOADING'
        const done = new Set(current.uploadedParts)
        const pending = Array.from({ length: current.partCount }, (_, index) => index + 1).filter(
          (number) => !done.has(number),
        )
        const loadingParts = new Map<number, number>()
        const sizeOfPart = (number: number) =>
          Math.min(current.partSize, file.size - (number - 1) * current.partSize)
        let confirmed = current.uploadedParts.reduce((sum, number) => sum + sizeOfPart(number), 0)
        transferred.value = confirmed
        let cursor = 0
        /** 一个工作线程顺序处理分片，线程之间最多保持配置指定的并发数。 */
        async function uploadWorker(): Promise<void> {
          try {
            while (cursor < pending.length) {
              signal.throwIfAborted()
              const number = pending[cursor++]!
              const blob = file!.slice((number - 1) * current.partSize, number * current.partSize)
              const digest = (await hashFile(blob, 'part', signal)).checksums!
              await retry(
                async () => {
                  loadingParts.set(number, 0)
                  const signature = await api.getPartSignature(current.id, number, digest, signal)
                  await api.uploadPart(current, number, blob, signature, signal, (loaded) => {
                    loadingParts.set(number, loaded)
                    transferred.value =
                      confirmed + [...loadingParts.values()].reduce((sum, value) => sum + value, 0)
                  })
                },
                signal,
                current.directUpload,
              )
              confirmed += blob.size
              loadingParts.delete(number)
              transferred.value =
                confirmed + [...loadingParts.values()].reduce((sum, value) => sum + value, 0)
            }
          } catch (error) {
            if (!signal.aborted) failure = error
            active.abort()
            throw error
          }
        }
        await Promise.allSettled(
          Array.from({ length: Math.min(config.concurrency, pending.length) }, uploadWorker),
        )
        if (failure) throw failure
        signal.throwIfAborted()
      }
      phase.value = 'COMPLETING'
      task.value = await retry(() => api.completeUpload(current.id, signal), signal)
      transferred.value = file.size
      phase.value = 'DONE'
    } catch (error) {
      if (failure || !signal.aborted) {
        errorMessage.value = getMessage(failure || error)
        phase.value = 'ERROR'
      } else {
        phase.value = 'PAUSED'
      }
    } finally {
      running.value = false
      controller = undefined
      if (!disposed && token === auth.getToken) {
        await refreshTasks()
      } else {
        task.value = undefined
        selectedFile.value = undefined
        errorMessage.value = ''
        phase.value = 'IDLE'
      }
    }
  }

  /** 暂停本地传输，已经到达服务器的分片留待下次查询确认。 */
  function pauseUpload(): void {
    if (!controller) return
    phase.value = 'PAUSING'
    controller.abort()
  }

  /** 重复确认合并结果，不要求用户再次选择文件。 */
  async function confirmUpload(existing: UploadTask): Promise<void> {
    if (running.value) return
    running.value = true
    selectedFile.value = undefined
    fingerprint = ''
    request = undefined
    transferred.value = 0
    task.value = existing
    phase.value = 'COMPLETING'
    errorMessage.value = ''
    const token = auth.getToken
    const active = new AbortController()
    controller = active
    try {
      const result = await api.completeUpload(existing.id, active.signal)
      active.signal.throwIfAborted()
      task.value = result
      transferred.value = existing.fileSize
      phase.value = 'DONE'
    } catch (error) {
      if (!active.signal.aborted) {
        phase.value = 'ERROR'
        errorMessage.value = getMessage(error)
      } else {
        phase.value = 'PAUSED'
      }
    } finally {
      running.value = false
      controller = undefined
      if (!disposed && token === auth.getToken) {
        await refreshTasks()
      } else {
        task.value = undefined
        errorMessage.value = ''
        phase.value = 'IDLE'
      }
    }
  }

  /** 用户明确取消后提交终止请求；活动传输须先暂停并等待结束。 */
  async function cancelTask(existing: UploadTask): Promise<void> {
    if (running.value) return
    try {
      await api.cancelUpload(existing.id)
      if (task.value?.id === existing.id) {
        task.value = undefined
        selectedFile.value = undefined
        phase.value = 'IDLE'
      }
    } catch (error) {
      errorMessage.value = getMessage(error)
    } finally {
      await refreshTasks()
    }
  }

  /** 保存完成文件，取消文件选择器不作为下载错误提示。 */
  async function downloadTask(existing: UploadTask): Promise<void> {
    downloadId.value = existing.id
    try {
      await api.downloadUploadedFile(existing)
    } catch (error) {
      if (!(error instanceof DOMException && error.name === 'AbortError')) {
        errorMessage.value = getMessage(error)
      }
    } finally {
      downloadId.value = ''
    }
  }

  /** 将失败转换为可读消息，不展示签名 URL 或底层请求配置。 */
  function getMessage(error: unknown): string {
    if (axios.isAxiosError(error) && !/[\u4e00-\u9fff]/.test(error.message)) {
      return '文件传输失败，请检查网络及存储跨域配置后重试'
    }
    return error instanceof Error ? error.message : '文件操作失败，请稍后重试'
  }

  watch(
    () => auth.getToken,
    () => {
      pauseUpload()
      listVersion++
      tasks.value = []
      task.value = undefined
      selectedFile.value = undefined
      options.value = undefined
    },
  )
  onMounted(async () => {
    const token = auth.getToken
    try {
      const config = await api.getUploadOptions()
      if (disposed || token !== auth.getToken) return
      options.value = config
      await refreshTasks()
    } catch (error) {
      errorMessage.value = getMessage(error)
    }
  })
  onDeactivated(pauseUpload)
  onBeforeUnmount(() => {
    disposed = true
    listVersion++
    pauseUpload()
  })

  return {
    options,
    tasks,
    task,
    selectedFile,
    phase,
    phaseLabel,
    running,
    loading,
    errorMessage,
    hashProgress,
    progress,
    transferred,
    page,
    total,
    downloadId,
    formatSize,
    statusLabel,
    chooseFile,
    selectFile,
    startUpload,
    pauseUpload,
    confirmUpload,
    cancelTask,
    downloadTask,
    refreshTasks,
    changePage,
  }
}
