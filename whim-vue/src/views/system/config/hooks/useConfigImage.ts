import { ref, watch } from 'vue'
import { fetchConfigImage } from './api'
import type { ConfigImageProps } from './types'

/** 预览当前草稿或配置中保存的图片，替换图片和关闭弹窗时释放旧资源。 */
export function useConfigImage(props: ConfigImageProps) {
  const source = ref('')
  const loading = ref(false)

  watch(() => [props.configId, props.fileId, props.file] as const, async ([id, fileId, file], _, onCleanup) => {
    source.value = ''
    loading.value = false
    if (file) {
      const url = URL.createObjectURL(file)
      source.value = url
      onCleanup(() => URL.revokeObjectURL(url))
      return
    }
    if (!id || !fileId) return
    const controller = new AbortController()
    let url = ''
    onCleanup(() => {
      controller.abort()
      if (url) URL.revokeObjectURL(url)
    })
    loading.value = true
    try {
      const blob = await fetchConfigImage(id, controller.signal)
      if (!controller.signal.aborted && blob.size && blob.type === 'image/png') {
        url = URL.createObjectURL(blob)
        source.value = url
      }
    } catch {
      // 读取失败时继续显示占位，保留替换图片的操作。
    } finally {
      if (!controller.signal.aborted) loading.value = false
    }
  }, { immediate: true })

  return { source, loading }
}
