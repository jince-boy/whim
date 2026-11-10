import { computed, onBeforeUnmount, onMounted, ref, useTemplateRef, type ComputedRef } from 'vue'
import { message } from '@/plugins'
import * as api from './api'
import { formatFileSize } from '../utils'
import type { FileUploadTask, FileUploadView, FileUploadSummary, FileUploadState } from './types'

/** 管理当前文件页的上传队列，Popover 的开合不影响任务执行。 */
export function useFileUpload(
  canUpload: ComputedRef<boolean>,
  refreshFiles: () => Promise<unknown>,
) {
  const uploadInput = useTemplateRef<HTMLInputElement>('uploadInput')
  const uploadLimit = ref<number>()
  const tasks = ref<FileUploadTask[]>([])
  const controllers = new Map<string, AbortController>()
  const cancellationControllers = new Set<AbortController>()
  let processing = false
  let disposed = false

  // 这些状态描述本地传输过程，与持久化的业务字典无关。
  const stateLabels: Record<FileUploadState, string> = {
    waiting: '等待上传',
    uploading: '上传中',
    paused: '已暂停',
    completing: '正在保存',
    success: '上传完成',
    error: '上传失败',
    cancelling: '正在取消',
    cancelFailed: '取消失败',
    cancelled: '已取消',
  }

  const uploads = computed<FileUploadView[]>(() =>
    tasks.value.map((task) => {
      return {
        id: task.id,
        name: task.request.originalName,
        state: task.state,
        stateLabel: stateLabels[task.state],
        progress: Math.floor((task.transferredBytes / task.request.fileSize) * 100),
        progressStatus:
          task.state === 'success'
            ? 'success'
            : task.state === 'error' || task.state === 'cancelFailed'
              ? 'error'
              : task.state === 'paused'
                ? 'warning'
                : 'default',
        sizeLabel: formatFileSize(task.request.fileSize),
        transferredLabel: formatFileSize(task.transferredBytes),
        error: task.error,
      }
    }),
  )
  const uploadSummary = computed<FileUploadSummary>(() => {
    const summary: FileUploadSummary = {
      total: tasks.value.length,
      active: 0,
      finished: 0,
      failed: 0,
      pausable: 0,
      resumable: 0,
      clearable: 0,
    }
    for (const task of tasks.value) {
      if (task.state === 'waiting' || task.state === 'uploading') summary.pausable++
      if (task.state === 'paused') summary.resumable++
      if (task.state === 'success') summary.finished++
      if (task.state === 'error' || task.state === 'cancelFailed') summary.failed++
      if (task.state === 'success' || task.state === 'cancelled') summary.clearable++
      if (task.state === 'completing') summary.active++
    }
    summary.active += summary.pausable
    return summary
  })

  /** 点击上传直接打开系统多选文件选择器，已有上传不阻止继续添加文件。 */
  function selectUploadFiles() {
    if (canUpload.value && uploadLimit.value) uploadInput.value?.click()
  }

  /** 选择后立即加入队列，重置输入框以允许重新选择同名文件。 */
  function addUploadFiles(event: Event) {
    const input = event.target as HTMLInputElement
    const selected = Array.from(input.files ?? [])
    input.value = ''
    if (!canUpload.value || !uploadLimit.value) return
    if (!globalThis.crypto?.getRandomValues) {
      message.error('当前浏览器不支持安全随机数，请更新浏览器后重试')
      return
    }
    for (const file of selected) {
      if (!file.size || file.size > uploadLimit.value) {
        message.error(
          `“${file.name}”必须为非空且不超过 ${formatFileSize(uploadLimit.value)} 的文件`,
        )
        continue
      }
      // 普通 HTTP 部署没有 randomUUID，使用同样安全且不依赖 HTTPS 的随机字节生成32位标识。
      const requestId = Array.from(crypto.getRandomValues(new Uint8Array(16)), (byte) =>
        byte.toString(16).padStart(2, '0'),
      ).join('')
      tasks.value.push({
        id: requestId,
        file,
        started: false,
        state: 'waiting',
        confirmedBytes: 0,
        transferredBytes: 0,
        error: '',
        request: {
          requestId,
          originalName: file.name,
          fileSize: file.size,
          contentType: file.type || 'application/octet-stream',
          uploadSource: 'fileManagement',
        },
      })
    }
    void processUploadQueue()
  }

  /** 逐个传输文件，继续时先读取服务端分片清单，再补传缺失分片。 */
  async function processUploadQueue() {
    if (processing || disposed) return
    processing = true
    try {
      while (!disposed && canUpload.value) {
        const task = tasks.value.find((item) => item.state === 'waiting')
        if (!task) break
        const file = task.file
        if (!file) break
        const controller = new AbortController()
        controllers.set(task.id, controller)
        task.state = 'uploading'
        task.error = ''
        task.started = true
        try {
          const response = task.sessionId
            ? await api.getFileUploadTask(task.sessionId, controller.signal)
            : await api.addFileUploadTask(task.request, controller.signal)
          if (controller.signal.aborted || disposed) continue
          if (response.code !== 200) throw new Error(response.message)
          const session = response.data
          task.sessionId = session.id
          if (session.cancelled) {
            task.state = 'cancelled'
            task.file = null
            continue
          }
          const uploadedParts = new Set(session.uploadedParts)
          task.confirmedBytes = session.uploadedParts.reduce(
            (total, number) =>
              total + Math.min(session.partSize, file.size - (number - 1) * session.partSize),
            0,
          )
          task.transferredBytes = task.confirmedBytes
          if (!session.fileId) {
            const partCount = Math.ceil(file.size / session.partSize)
            for (let partNumber = 1; partNumber <= partCount; partNumber++) {
              if (controller.signal.aborted || disposed) break
              if (uploadedParts.has(partNumber)) continue
              const start = (partNumber - 1) * session.partSize
              const part = file.slice(start, Math.min(start + session.partSize, file.size))
              const result = await api.uploadFilePart(
                { id: session.id, partNumber, file: part },
                (percent) => {
                  if (!controller.signal.aborted && task.state === 'uploading') {
                    task.transferredBytes =
                      task.confirmedBytes + Math.floor((part.size * percent) / 100)
                  }
                },
                controller.signal,
              )
              if (controller.signal.aborted || disposed) break
              if (result.code !== 200) throw new Error(result.message)
              task.confirmedBytes += part.size
              task.transferredBytes = task.confirmedBytes
            }
            if (controller.signal.aborted || disposed) continue
            // 分片已传完仍须完成服务端合并，保存期间禁止暂停或取消，避免歧义。
            task.state = 'completing'
            const completed = await api.completeFileUpload(session.id, controller.signal)
            if (controller.signal.aborted || disposed) continue
            if (completed.code !== 200) throw new Error(completed.message)
          }
          await finishUpload(task)
        } catch (error) {
          if (!controller.signal.aborted && !disposed) {
            task.state = 'error'
            task.error = error instanceof Error ? error.message : '上传失败，请重试'
            task.transferredBytes = task.confirmedBytes
          }
        } finally {
          controllers.delete(task.id)
        }
      }
    } finally {
      processing = false
    }
  }

  /** 暂停当前分片或等待任务，保留已确认的分片供继续使用。 */
  function pauseUpload(id: string) {
    const task = tasks.value.find((item) => item.id === id)
    if (!task || !['waiting', 'uploading'].includes(task.state)) return
    task.state = 'paused'
    controllers.get(id)?.abort()
    task.transferredBytes = task.confirmedBytes
  }

  /** 暂停所有可暂停任务，不中断已开始的服务端合并。 */
  function pauseAllUploads() {
    for (const task of tasks.value) {
      if (task.state !== 'waiting' && task.state !== 'uploading') continue
      task.state = 'paused'
      controllers.get(task.id)?.abort()
      task.transferredBytes = task.confirmedBytes
    }
  }

  /** 将暂停或失败任务重新排队，幂等请求及分片清单避免重复生成文件。 */
  function resumeUpload(id: string) {
    const task = tasks.value.find((item) => item.id === id)
    if (!task || !['paused', 'error'].includes(task.state) || !canUpload.value) return
    task.state = 'waiting'
    task.error = ''
    void processUploadQueue()
  }

  /** 继续所有暂停任务，失败任务仍由用户单独重试。 */
  function resumeAllUploads() {
    if (!canUpload.value) return
    for (const task of tasks.value) {
      if (task.state !== 'paused') continue
      task.state = 'waiting'
      task.error = ''
    }
    void processUploadQueue()
  }

  /** 取消任务并清理服务端分片，创建响应丢失时先通过幂等请求找回任务。 */
  async function cancelUpload(id: string) {
    const task = tasks.value.find((item) => item.id === id)
    if (!task || ['success', 'cancelled', 'cancelling', 'completing'].includes(task.state)) return
    task.state = 'cancelling'
    task.error = ''
    controllers.get(id)?.abort()
    const controller = new AbortController()
    cancellationControllers.add(controller)
    try {
      if (task.started) {
        if (!task.sessionId) {
          const initialized = await api.addFileUploadTask(task.request, controller.signal)
          if (controller.signal.aborted || disposed) return
          if (initialized.code !== 200) throw new Error(initialized.message)
          task.sessionId = initialized.data.id
        }
        const result = await api.cancelFileUpload(task.sessionId, controller.signal)
        if (controller.signal.aborted || disposed) return
        if (result.code !== 200) throw new Error(result.message)
        if (result.data.fileId) {
          await finishUpload(task)
          return
        }
      }
      task.state = 'cancelled'
      task.file = null
      task.transferredBytes = task.confirmedBytes
    } catch (error) {
      if (!controller.signal.aborted && !disposed) {
        task.state = 'cancelFailed'
        task.error = error instanceof Error ? error.message : '取消失败，请重试'
      }
    } finally {
      cancellationControllers.delete(controller)
    }
  }

  /** 统一处理确认保存的结果，列表刷新失败不把已保存文件改回上传失败。 */
  async function finishUpload(task: FileUploadTask) {
    task.state = 'success'
    task.confirmedBytes = task.request.fileSize
    task.transferredBytes = task.request.fileSize
    task.file = null
    try {
      await refreshFiles()
    } catch {
      // 请求层负责提示刷新失败，上传结果保持成功。
    }
  }

  /** 仅移除已结束的显示记录，失败和暂停任务保留文件引用。 */
  function clearFinishedUploads() {
    tasks.value = tasks.value.filter(
      (task) => task.state !== 'success' && task.state !== 'cancelled',
    )
  }

  /** 从真实接口获取上传上限，失败时保持选择入口禁用。 */
  async function loadUploadLimit() {
    if (!canUpload.value) return
    try {
      const response = await api.getUploadLimit()
      if (response.code === 200) uploadLimit.value = Number(response.data)
      else message.error(response.message)
    } catch {
      /* 公共请求层提示错误。 */
    }
  }
  onMounted(loadUploadLimit)
  onBeforeUnmount(() => {
    disposed = true
    controllers.forEach((controller) => controller.abort())
    cancellationControllers.forEach((controller) => controller.abort())
    tasks.value = []
  })
  return {
    uploads,
    uploadSummary,
    uploadLimit,
    selectUploadFiles,
    addUploadFiles,
    pauseUpload,
    resumeUpload,
    cancelUpload,
    pauseAllUploads,
    resumeAllUploads,
    clearFinishedUploads,
    loadUploadLimit,
  }
}
