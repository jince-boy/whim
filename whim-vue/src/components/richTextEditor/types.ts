export type ImageLoader = (fileId: string, signal: AbortSignal) => Promise<Blob>

export interface RichTextEditorProps {
  placeholder?: string
  maxlength?: number
  imageHandlers?: {
    load: ImageLoader
    upload: (file: File, signal: AbortSignal) => Promise<string>
  }
}
