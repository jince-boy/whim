import { createContentLoader } from 'vitepress'
import type { SearchPage } from './types'

declare const data: SearchPage[]
export { data }

export default createContentLoader(['guide/*.md', 'components/*.md', 'backend/*.md'], {
  includeSrc: true,
  /** 搜索索引由正文构建，移除前置信息和 Vue 脚本，不向浏览器发送源码脚本。 */
  transform(pages): SearchPage[] {
    return pages.map((page) => ({
      title: String(page.frontmatter.title || page.url),
      url: page.url,
      text: (page.src || '')
        .replace(/^---[\s\S]*?---/, '')
        .replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi, '')
        .replace(/<[^>]*>/g, ' ')
        .replace(/[#*`|]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim(),
    }))
  },
})
