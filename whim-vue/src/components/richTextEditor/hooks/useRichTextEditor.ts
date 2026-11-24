import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch, type Ref } from 'vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor-next/editor'
import { message } from '@/plugins'
import { createRichImageSession, getRichText } from '../richTextHtml'
import type { RichTextEditorProps } from '../types'

export function useRichTextEditor(options: RichTextEditorProps, model: Ref<string>, busy: Ref<boolean>) {
  let images = createRichImageSession()
  let currentHtml = model.value
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
    const session = images
    const html = model.value
    failed.value = false
    const complete = await session.load(html, options.imageHandlers?.load)
    if (session.signal.aborted) return
    if (!complete) {
      failed.value = true
      return
    }
    initialHtml.value = session.display(html)
    ready.value = true
  }

  const toolbarConfig = computed<Partial<IToolbarConfig>>(() => ({
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
      ...(options.imageHandlers ? ['uploadImage'] : []),
      'insertTable',
      '|',
      'undo',
      'redo',
    ],
  }))
  const editorConfig = computed<Partial<IEditorConfig>>(() => ({
    placeholder: options.placeholder,
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
        checkImage: (src: string) => images.owns(src) || '请通过上传插入正文图片',
      },
      uploadImage: {
        allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif'],
        maxFileSize: 20 * 1024 * 1024,
        base64LimitSize: 0,
        async customUpload(
          file: File,
          insert: (src: string, alt: string, href: string) => void | Promise<void>,
        ) {
          if (!options.imageHandlers) return
          if (
            !['image/jpeg', 'image/png', 'image/gif'].includes(file.type) ||
            !file.size ||
            file.size > 20 * 1024 * 1024
          ) {
            message.warning('请选择 20 MB 以内的 JPG、PNG 或 GIF 图片')
            return
          }
          const session = images
          uploads.value++
          try {
            const id = await options.imageHandlers.upload(file, session.signal)
            if (!session.signal.aborted) {
              await insert(session.remember(id, file), file.name || '正文图片', '')
              if (!session.signal.aborted && editor.value) updateHtml(editor.value.getHtml())
            }
          } catch {
            // 上传回调负责展示业务错误，组件卸载或切换正文时取消的请求无需提示。
          } finally {
            if (!session.signal.aborted) uploads.value--
          }
        },
      },
    },
  }))
  watch([model, () => Boolean(options.imageHandlers)], ([html, supportsImages], [, supportedImages]) => {
    // 自身输入保留光标和撤销栈，外部回填或重置时重新加载对应正文的图片。
    if (html === currentHtml && supportsImages === supportedImages) return
    currentHtml = html
    ready.value = false
    images.dispose()
    editor.value?.destroy()
    editor.value = undefined
    images = createRichImageSession()
    uploads.value = 0
    void initialize()
  })
  onMounted(initialize)
  onBeforeUnmount(() => {
    editor.value?.destroy()
    images.dispose()
  })
  function updateHtml(html: string) {
    if (!ready.value || images.signal.aborted) return
    currentHtml = images.serialize(html)
    model.value = currentHtml
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
