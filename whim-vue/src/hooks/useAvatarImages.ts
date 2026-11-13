import { onActivated, onDeactivated, onScopeDispose, ref, watch } from 'vue'
import { message } from '@/plugins'
import { useAuthStore } from '@/stores/modules/auth'
import { fetchUserAvatar } from '@/views/system/user/hooks/api'

// 已保存的头像地址可能经过纯文本清洗；只识别本项目头像路由，不能直接把转义地址交给img。
const avatarSourcePattern = /^\/system\/user\/avatar\?id(?:=|&#0*61;|&#x0*3d;)(\d+)$/i

/** 通过认证客户端加载当前页面的头像，分页或离开页面后释放图片资源。 */
export function useAvatarImages(getSources: () => Array<string | null | undefined>) {
  const auth = useAuthStore()
  const imageUrls = ref(new Map<string, string>())
  const active = ref(true)
  const avatarPreviewUrl = ref<string>()
  const avatarPreviewShown = ref(false)
  const avatarPreviewLoading = ref(false)
  let previewSource: string | undefined
  let previewController: AbortController | undefined
  let previewObjectUrl: string | undefined

  watch([getSources, () => auth.getToken, active], ([sources, token, enabled], _previous, onCleanup) => {
    imageUrls.value = new Map()
    resetAvatarPreview()
    if (!enabled) return
    const controller = new AbortController()
    const objectUrls: string[] = []
    onCleanup(() => {
      controller.abort()
      objectUrls.forEach((url) => URL.revokeObjectURL(url))
      resetAvatarPreview()
    })
    for (const source of new Set(sources)) {
      if (!source) continue
      const match = avatarSourcePattern.exec(source)
      if (!match) {
        if (/^(https?:\/\/|blob:|\/(?!\/))/i.test(source)) imageUrls.value.set(source, source)
        continue
      }
      if (!token) continue
      void fetchUserAvatar({ id: match[1]! }, controller.signal).then((blob) => {
        // 分页、账号或页面切换后，旧请求不能恢复已经释放的图片。
        if (controller.signal.aborted || !blob) return
        const objectUrl = URL.createObjectURL(blob)
        objectUrls.push(objectUrl)
        imageUrls.value.set(source, objectUrl)
      }).catch(() => {
        // 请求层统一提示错误，加载失败的头像由NAvatar显示姓名首字。
      })
    }
  }, { immediate: true })

  onDeactivated(() => { active.value = false })
  onActivated(() => { active.value = true })
  onScopeDispose(resetAvatarPreview)

  watch(avatarPreviewShown, (shown, _previous, onCleanup) => {
    if (!shown) return
    // 原生预览和表单弹窗都会处理Esc，提前消费事件，避免关闭预览时连同编辑草稿一起关闭。
    const closePreview = (event: KeyboardEvent) => {
      if (event.key !== 'Escape' || !avatarPreviewShown.value) return
      event.preventDefault()
      event.stopPropagation()
      avatarPreviewShown.value = false
    }
    window.addEventListener('keydown', closePreview, true)
    onCleanup(() => window.removeEventListener('keydown', closePreview, true))
  })

  /** 返回头像的可展示地址，未设置或尚未加载时保留默认头像。 */
  function getAvatarUrl(source: string | null | undefined) {
    return source ? imageUrls.value.get(source) : undefined
  }

  /** 点击后才读取头像原文件；本地裁剪草稿直接预览，关闭编辑或切页会取消旧请求。 */
  async function openAvatarPreview(source: string | null | undefined) {
    if (!source || !active.value || avatarPreviewLoading.value || !getAvatarUrl(source)) return
    if (previewSource === source && avatarPreviewUrl.value) {
      avatarPreviewShown.value = true
      return
    }
    resetAvatarPreview()
    previewSource = source
    const controller = new AbortController()
    previewController = controller
    avatarPreviewLoading.value = true
    try {
      const match = avatarSourcePattern.exec(source)
      if (match) {
        let blob: Blob | undefined
        try {
          blob = await fetchUserAvatar({ id: match[1]!, preview: true }, controller.signal)
        } catch {
          return // 网络错误由公共客户端提示。
        }
        if (controller.signal.aborted) return
        if (!blob) { message.error('该头像无法预览'); return }
        previewObjectUrl = URL.createObjectURL(blob)
        avatarPreviewUrl.value = previewObjectUrl
      } else {
        avatarPreviewUrl.value = getAvatarUrl(source)
      }
      const image = new Image()
      image.src = avatarPreviewUrl.value!
      await image.decode()
      if (!controller.signal.aborted) avatarPreviewShown.value = true
    } catch {
      if (!controller.signal.aborted) {
        resetAvatarPreview()
        message.error('该头像无法预览')
      }
    } finally {
      if (!controller.signal.aborted) avatarPreviewLoading.value = false
    }
  }

  /** 释放大图预览，避免关闭表单或切换分页后仍然展示旧用户头像。 */
  function resetAvatarPreview() {
    previewController?.abort()
    previewController = undefined
    previewSource = undefined
    avatarPreviewShown.value = false
    avatarPreviewLoading.value = false
    avatarPreviewUrl.value = undefined
    if (previewObjectUrl) URL.revokeObjectURL(previewObjectUrl)
    previewObjectUrl = undefined
  }

  return { getAvatarUrl, avatarPreviewUrl, avatarPreviewShown, avatarPreviewLoading, openAvatarPreview }
}
