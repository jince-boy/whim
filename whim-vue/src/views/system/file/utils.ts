/** 文件列表和上传进度统一按字节格式化，兼容后端 Long 的字符串表示。 */
export function formatFileSize(size: string | number) {
  const bytes = Number(size)
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}
