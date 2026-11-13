export interface ImageCropperProps {
  /** 待裁剪的本地图片；关闭时可以为空。 */
  file: File | null
  title?: string
  /** 导出宽高同时决定裁剪框比例。 */
  outputWidth?: number
  outputHeight?: number
  outputType?: 'image/png' | 'image/jpeg' | 'image/webp'
  quality?: number
}
