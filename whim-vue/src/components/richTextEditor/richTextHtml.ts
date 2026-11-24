import DOMPurify from 'dompurify'
import type { ImageLoader } from './types'

const blockSelector = 'p,h1,h2,h3,h4,h5,h6,li:not(:has(p,li)),th:not(:has(p)),td:not(:has(p))'
const validFileId = /^[1-9]\d{0,18}$/

function fragment(html: string) {
  const template = document.createElement('template')
  template.innerHTML = html
  return template
}

/** 将旧正文逐行放入文本节点，避免把历史内容解析为标签。 */
export function plainTextToHtml(value: string): string {
  const container = document.createElement('div')
  for (const line of value.split(/\r\n|\r|\n/)) {
    const paragraph = document.createElement('p')
    paragraph.textContent = line
    container.append(paragraph)
  }
  return container.innerHTML
}

export function getRichText(html: string): string {
  const template = fragment(html)
  // wangEditor 用单个 br 撑起空段落供光标落位，整篇为空时不计作正文换行。
  if (template.innerHTML === '<p><br></p>') return ''
  template.content.querySelectorAll('br').forEach((node) => node.replaceWith('\n'))
  template.content.querySelectorAll(blockSelector).forEach((node) => node.append('\n'))
  return (template.content.textContent ?? '').replace(/\n$/, '')
}

export function hasRichImage(html: string): boolean {
  return Array.from(fragment(html).content.querySelectorAll('img')).some((img) =>
    validFileId.test(img.dataset.fileId ?? ''),
  )
}

/** 仅保留带文件标识的图片，由调用方读取后再生成预览地址。 */
export function sanitizeRichHtml(html: string): string {
  const template = fragment(html)
  for (const img of template.content.querySelectorAll('img')) {
    img.removeAttribute('src')
    img.removeAttribute('srcset')
    if (!validFileId.test(img.dataset.fileId ?? '')) img.remove()
  }
  return DOMPurify.sanitize(template.innerHTML, {
    ALLOWED_TAGS: [
      'p',
      'br',
      'h1',
      'h2',
      'h3',
      'h4',
      'h5',
      'h6',
      'strong',
      'b',
      'em',
      'i',
      'u',
      's',
      'a',
      'ul',
      'ol',
      'li',
      'img',
      'table',
      'thead',
      'tbody',
      'tr',
      'th',
      'td',
    ],
    ALLOWED_ATTR: ['href', 'start', 'colspan', 'rowspan', 'data-file-id', 'alt'],
    ALLOWED_URI_REGEXP: /^https?:\/\//i,
    ADD_URI_SAFE_ATTR: ['start', 'colspan', 'rowspan', 'data-file-id', 'alt'],
    ALLOW_DATA_ATTR: false,
    ALLOW_ARIA_ATTR: false,
  })
}

/** 每次打开编辑器或详情独占图片地址，关闭时取消请求并释放 Blob。 */
export function createRichImageSession() {
  const controller = new AbortController()
  const urls = new Map<string, string>()
  const ids = new Map<string, string>()
  const { signal } = controller

  function remember(id: string, image: Blob) {
    signal.throwIfAborted()
    if (!validFileId.test(id)) throw new Error('上传结果缺少有效的图片标识')
    const previous = urls.get(id)
    if (previous) return previous
    const url = URL.createObjectURL(image)
    urls.set(id, url)
    ids.set(url, id)
    return url
  }

  function display(html: string) {
    const template = fragment(sanitizeRichHtml(html))
    template.content.querySelectorAll('img').forEach((img) => {
      const url = urls.get(img.dataset.fileId ?? '')
      if (url) img.src = url
      img.alt ||= '正文图片'
    })
    return template.innerHTML
  }

  function serialize(html: string) {
    const template = fragment(html)
    template.content.querySelectorAll('img').forEach((img) => {
      const id = ids.get(img.getAttribute('src') ?? '')
      if (id) img.dataset.fileId = id
      else img.remove()
    })
    return sanitizeRichHtml(template.innerHTML)
  }

  async function load(html: string, loader?: ImageLoader) {
    const images = fragment(html).content.querySelectorAll<HTMLImageElement>('img[data-file-id]')
    const missing = new Set(
      Array.from(images, (img) => img.dataset.fileId!).filter(
        (id) => validFileId.test(id) && !urls.has(id),
      ),
    )
    if (!loader) return missing.size === 0
    const loadImage = loader
    const queue = missing.values()
    let complete = true
    async function worker() {
      for (const id of queue) {
        if (signal.aborted) return
        try {
          remember(id, await loadImage(id, signal))
        } catch {
          complete = false
        }
      }
    }
    await Promise.all(Array.from({ length: Math.min(4, missing.size) }, worker))
    return complete && !signal.aborted
  }

  function dispose() {
    controller.abort()
    urls.forEach((url) => URL.revokeObjectURL(url))
    urls.clear()
    ids.clear()
  }

  return {
    signal,
    remember,
    display,
    serialize,
    load,
    dispose,
    owns: (url: string) => ids.has(url),
  }
}
