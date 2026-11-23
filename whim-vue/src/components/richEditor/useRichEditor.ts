import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch, type Ref } from 'vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor-next/editor'
import { message } from '@/plugins'
import { createRichImageSession, getRichText, type ImageLoader } from './richText'

interface EditorOptions {
  loadImage: ImageLoader
  uploadImage: (file: File, signal: AbortSignal) => Promise<string>
}

export function useRichEditor(options: EditorOptions, model: Ref<string>, busy: Ref<boolean>) {
  const images = createRichImageSession()
  const editor = shallowRef<IDomEditor>()
  const initialHtml = ref('')
  const ready = ref(false)
  const failed = ref(false)
  const uploads = ref(0)
  const count = computed(() => getRichText(model.value).length)
  watch(
    () => !ready.value || uploads.value > 0,
    (value) => {
      busy.value = value
    },
    { immediate: true, flush: 'sync' },
  )

  async function initialize() {
    failed.value = false
    const complete = await images.load(model.value, options.loadImage)
    if (images.signal.aborted) return
    if (!complete) {
      failed.value = true
      return
    }
    initialHtml.value = images.display(model.value)
    ready.value = true
  }

  const toolbarConfig: Partial<IToolbarConfig> = {
    toolbarKeys: [
      'headerSelect',
      '|',
      'bold',
      'italic',
      'underline',
      'through',
      '|',
      'bulletedList',
      'numberedList',
      '|',
      'insertLink',
      'uploadImage',
      'insertTable',
      '|',
      'undo',
      'redo',
    ],
  }
  const editorConfig: Partial<IEditorConfig> = {
    placeholder: '输入公告正文，或插入图片…',
    autoFocus: false,
    sanitizeHtml: (html) => images.display(images.serialize(html)),
    hoverbarKeys: {
      text: { menuKeys: ['bold', 'italic', 'underline', 'insertLink'] },
      image: { menuKeys: ['deleteImage'] },
      table: {
        menuKeys: [
          'tableHeader',
          'insertTableRow',
          'deleteTableRow',
          'insertTableCol',
          'deleteTableCol',
          'deleteTable',
        ],
      },
    },
    MENU_CONF: {
      insertImage: {
        checkImage: (src: string) => images.owns(src) || '请通过上传图片插入公告配图',
      },
      uploadImage: {
        allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif'],
        maxFileSize: 20 * 1024 * 1024,
        base64LimitSize: 0,
        async customUpload(
          file: File,
          insert: (src: string, alt: string, href: string) => void | Promise<void>,
        ) {
          if (
            !['image/jpeg', 'image/png', 'image/gif'].includes(file.type) ||
            !file.size ||
            file.size > 20 * 1024 * 1024
          ) {
            message.warning('请选择 20 MB 以内的 JPG、PNG 或 GIF 图片')
            return
          }
          uploads.value++
          try {
            const id = await options.uploadImage(file, images.signal)
            if (!images.signal.aborted) {
              await insert(images.remember(id, file), file.name || '公告图片', '')
              if (editor.value) model.value = images.serialize(editor.value.getHtml())
            }
          } catch {
            // HTTP 层已显示上传错误，取消弹窗时无需另行提示。
          } finally {
            uploads.value--
          }
        },
      },
    },
  }
  onMounted(initialize)
  onBeforeUnmount(() => {
    editor.value?.destroy()
    images.dispose()
  })
  function updateHtml(html: string) {
    model.value = images.serialize(html)
  }
  return {
    editor,
    initialHtml,
    ready,
    failed,
    uploads,
    count,
    toolbarConfig,
    editorConfig,
    initialize,
    updateHtml,
  }
}
