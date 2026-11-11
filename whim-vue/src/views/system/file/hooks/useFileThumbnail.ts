import {
  computed,
  onActivated,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
  useTemplateRef,
  watch,
} from 'vue'
import { message } from '@/plugins'
import { getFilePreview, getFileThumbnail } from './api'
import type { FileThumbnailProps } from './types'

/** 可见图片按需加载；请求沿用下载权限，离开页面或替换记录时释放图片资源。 */
export function useFileThumbnail(props: FileThumbnailProps) {
  const thumbnailElement = useTemplateRef<HTMLElement>('thumbnailElement')
  const visible = ref(false)
  const active = ref(true)
  const imageUrl = ref<string>()
  const loading = ref(false)
  const unavailable = ref(false)
  const previewUrl = ref<string>()
  const previewShown = ref(false)
  const previewLoading = ref(false)
  const isImage = computed(() => /^image\//i.test(props.file.contentType))
  let observer: IntersectionObserver | undefined
  let previewController: AbortController | undefined

  watch(
    [() => props.file.id, () => props.canPreview, isImage, visible, active],
    async ([id, allowed, image, shown, enabled], _previous, onCleanup) => {
      imageUrl.value = undefined
      loading.value = false
      unavailable.value = false
      resetPreview()
      if (!allowed || !image || !shown || !enabled) return

      const controller = new AbortController()
      let objectUrl: string | undefined
      onCleanup(() => {
        controller.abort()
        if (objectUrl) URL.revokeObjectURL(objectUrl)
        resetPreview()
      })
      loading.value = true
      try {
        const thumbnail = await getFileThumbnail(id, controller.signal)
        // 分页、删除或缓存停用后，过期响应不能重新创建已释放的预览。
        if (controller.signal.aborted) return
        if (thumbnail) {
          objectUrl = URL.createObjectURL(thumbnail)
          imageUrl.value = objectUrl
        } else {
          unavailable.value = true
        }
      } catch {
        if (!controller.signal.aborted) unavailable.value = true
      } finally {
        if (!controller.signal.aborted) loading.value = false
      }
    },
    { immediate: true },
  )

  onMounted(() => {
    observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        visible.value = true
        observer?.disconnect()
      }
    })
    if (thumbnailElement.value) observer.observe(thumbnailElement.value)
  })
  onActivated(() => {
    active.value = true
  })
  onDeactivated(() => {
    active.value = false
  })
  onBeforeUnmount(() => observer?.disconnect())

  /** 图片无法解码时展示占位图标，避免表格出现破损图片。 */
  function handleImageError() {
    unavailable.value = true
  }

  /** 首次点击加载后端生成的安全预览，成功后复用当前记录的资源。 */
  async function openPreview() {
    if (!props.canPreview || !isImage.value || !active.value || previewLoading.value) return
    if (previewUrl.value) {
      previewShown.value = true
      return
    }

    const controller = new AbortController()
    previewController = controller
    previewLoading.value = true
    try {
      let preview: Blob | undefined
      try {
        preview = await getFilePreview(props.file.id, controller.signal)
      } catch {
        // 请求错误由公共客户端提示，离开页面后的取消请求无需再次提示。
        return
      }
      if (controller.signal.aborted) return
      if (!preview) {
        message.error('该图片无法预览')
        return
      }
      previewUrl.value = URL.createObjectURL(preview)
      const image = new Image()
      image.src = previewUrl.value
      try {
        await image.decode()
      } catch {
        if (!controller.signal.aborted) {
          URL.revokeObjectURL(previewUrl.value)
          previewUrl.value = undefined
          message.error('该图片无法预览')
        }
        return
      }
      // 图片解码期间切页、刷新记录或失去权限后，不再打开过期预览。
      if (!controller.signal.aborted) previewShown.value = true
    } finally {
      if (!controller.signal.aborted) previewLoading.value = false
    }
  }

  /** 记录替换、权限变更或页面停用时关闭预览并释放图片。 */
  function resetPreview() {
    previewController?.abort()
    previewController = undefined
    previewShown.value = false
    previewLoading.value = false
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = undefined
  }

  return {
    imageUrl,
    loading,
    unavailable,
    isImage,
    handleImageError,
    previewUrl,
    previewShown,
    previewLoading,
    openPreview,
  }
}
