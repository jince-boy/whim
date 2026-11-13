import { computed, onDeactivated, onScopeDispose, ref, useTemplateRef, watch, type Ref } from 'vue'
import type Cropper from 'cropperjs'
import type { ImageCropperProps } from '../types'

/** 管理图片裁剪、导出和资源释放，不承担业务上传或保存。 */
export function useImageCropper(
  props: Required<ImageCropperProps>,
  show: Ref<boolean>,
  confirm: (file: File) => void,
) {
  const container = useTemplateRef<HTMLDivElement>('cropperContainer')
  const loading = ref(true)
  const saving = ref(false)
  const ready = ref(false)
  const error = ref('')
  const modalTarget = computed(() => show.value ? (document.fullscreenElement as HTMLElement | null) ?? undefined : undefined)
  let cropper: Cropper | undefined
  let sourceUrl: string | undefined
  let observer: ResizeObserver | undefined
  let version = 0

  /** 关闭或更换图片后取消旧结果，并释放原图及裁剪器。 */
  function clearCropper() {
    version++
    observer?.disconnect()
    observer = undefined
    cropper?.destroy()
    cropper = undefined
    if (sourceUrl) URL.revokeObjectURL(sourceUrl)
    sourceUrl = undefined
    ready.value = false
    saving.value = false
    loading.value = false
  }

  /** 将图片居中覆盖裁剪框，并约束拖动和缩放，避免导出空白区域。 */
  function resetCropper() {
    if (!cropper || saving.value) return
    const canvas = cropper.getCropperCanvas()!
    const image = cropper.getCropperImage()!
    const selection = cropper.getCropperSelection()!
    const ratio = props.outputWidth / props.outputHeight
    const width = Math.min(canvas.clientWidth, canvas.clientHeight * ratio) * 0.8
    const height = width / ratio
    const left = (canvas.clientWidth - width) / 2
    const top = (canvas.clientHeight - height) / 2
    selection.aspectRatio = ratio
    selection.$change(left, top, width, height, ratio)
    image.maxInset = `${top}px ${left}px ${top}px ${left}px`
    // Cropper更新边界时会异步重置图片，等待其完成后一次性应用居中覆盖变换。
    void image.$nextTick(() => {
      if (cropper?.getCropperImage() !== image || !show.value || saving.value) return
      const scale = Math.max(width / image.clientWidth, height / image.clientHeight)
      image.$setTransform(scale, 0, 0, scale,
        (canvas.clientWidth - image.clientWidth) / 2, (canvas.clientHeight - image.clientHeight) / 2)
    })
  }

  /** 弹窗进入后按需加载裁剪依赖，损坏或超大像素图片保留错误提示。 */
  async function initializeCropper() {
    clearCropper()
    const currentVersion = version
    const file = props.file
    const target = container.value
    if (!show.value || !target) return
    loading.value = true
    error.value = ''
    try {
      if (!file) { error.value = '请先选择图片'; return }
      if (![props.outputWidth, props.outputHeight].every((size) => Number.isInteger(size) && size > 0 && size <= 2048)) {
        error.value = '裁剪输出宽高必须为1至2048之间的整数'
        return
      }
      sourceUrl = URL.createObjectURL(file)
      const image = new Image()
      image.src = sourceUrl
      image.alt = '待裁剪图片'
      const [module] = await Promise.all([import('cropperjs'), image.decode()])
      if (currentVersion !== version || !show.value) return
      if (image.naturalWidth > 16384 || image.naturalHeight > 16384 || image.naturalWidth * image.naturalHeight > 24_000_000) {
        error.value = '图片像素过大，请选择不超过2400万像素且单边不超过16384像素的图片'
        return
      }
      cropper = new module.default(image, {
        container: target,
        template: `<cropper-canvas background>
          <cropper-image scalable translatable zoom-around-center></cropper-image>
          <cropper-shade hidden></cropper-shade>
          <cropper-handle action="move" plain></cropper-handle>
          <cropper-selection outlined precise>
            <cropper-grid role="grid" covered></cropper-grid>
            <cropper-crosshair centered></cropper-crosshair>
            <cropper-handle action="move" theme-color="transparent"></cropper-handle>
          </cropper-selection>
        </cropper-canvas>`,
      })
      await cropper.getCropperImage()!.$ready()
      if (currentVersion !== version || !show.value) return
      resetCropper()
      observer = new ResizeObserver(resetCropper)
      observer.observe(target)
      ready.value = true
    } catch {
      if (currentVersion === version) {
        error.value = '无法加载图片或裁剪器，请重新选择有效的图片后重试'
      }
    } finally {
      if (currentVersion === version) loading.value = false
    }
  }

  /** 缩放图片，裁剪框比例和导出尺寸保持不变。 */
  function zoomImage(scale: number) {
    if (ready.value && !saving.value) cropper?.getCropperImage()?.$zoom(scale)
  }

  /** 提供键盘移动，按住Shift可加快调整，触屏仍可直接拖动。 */
  function moveImageByKeyboard(event: KeyboardEvent) {
    if (!ready.value || saving.value || !['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(event.key)) return
    event.preventDefault()
    const step = event.shiftKey ? 10 : 1
    const x = event.key === 'ArrowLeft' ? -step : event.key === 'ArrowRight' ? step : 0
    const y = event.key === 'ArrowUp' ? -step : event.key === 'ArrowDown' ? step : 0
    cropper?.getCropperImage()?.$move(x, y)
  }

  /** 导出裁剪后的文件，只有成功导出才通知业务方替换草稿。 */
  async function confirmCrop(): Promise<false> {
    if (!ready.value || saving.value || !cropper || !props.file) return false
    const currentVersion = version
    const originalName = props.file.name
    saving.value = true
    error.value = ''
    cropper.getCropperCanvas()!.disabled = true
    try {
      const canvas = await cropper.getCropperSelection()!.$toCanvas({
        width: props.outputWidth,
        height: props.outputHeight,
        beforeDraw: (context, output) => {
          if (props.outputType === 'image/jpeg') {
            context.fillStyle = '#fff'
            context.fillRect(0, 0, output.width, output.height)
          }
        },
      })
      const blob = await new Promise<Blob>((resolve, reject) => {
        canvas.toBlob((result) => result ? resolve(result) : reject(new Error('图片导出失败，请重试')),
          props.outputType, props.quality)
      })
      if (currentVersion !== version || !show.value) return false
      const extension = blob.type === 'image/jpeg' ? 'jpg' : blob.type === 'image/webp' ? 'webp' : 'png'
      const name = originalName.replace(/\.[^/.]+$/u, '') || 'image'
      confirm(new File([blob], `${name}.${extension}`, { type: blob.type, lastModified: Date.now() }))
      show.value = false
    } catch {
      if (currentVersion === version) error.value = '图片裁剪失败，请重试或重新选择图片'
    } finally {
      if (currentVersion === version) {
        saving.value = false
        if (cropper) cropper.getCropperCanvas()!.disabled = false
      }
    }
    return false
  }

  watch(show, (visible) => {
    if (!visible) { version++; ready.value = false }
  })
  watch([() => props.file, () => props.outputWidth, () => props.outputHeight], () => {
    if (show.value && container.value) void initializeCropper()
  })
  onDeactivated(() => { show.value = false; clearCropper() })
  onScopeDispose(clearCropper)

  return { modalTarget, loading, saving, ready, error, initializeCropper, clearCropper, resetCropper, zoomImage, moveImageByKeyboard, confirmCrop }
}
