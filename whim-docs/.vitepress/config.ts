import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitepress'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

const stylesByPage = new Map<string, string>()

export default defineConfig({
  lang: 'zh-CN',
  title: 'Whim 文档',
  description: 'Whim 自定义组件、认证授权与后端工程指南。',
  base: process.env.DOCS_BASE || '/',
  cleanUrls: true,
  appearance: true,
  lastUpdated: false,
  srcExclude: ['README.md'],
  head: [['meta', { name: 'theme-color', content: '#4f8466' }]],
  markdown: {
    theme: { light: 'github-light', dark: 'github-dark' },
    headers: { level: [2, 3] },
    lineNumbers: true,
  },
  vite: {
    plugins: [Components({ dirs: [], dts: false, resolvers: [NaiveUiResolver()] })],
    resolve: {
      alias: { '@': fileURLToPath(new URL('../../whim-vue/src', import.meta.url)) },
      dedupe: ['vue', 'naive-ui', '@css-render/vue3-ssr', 'axios'],
    },
    ssr: { noExternal: ['naive-ui', 'vueuc', '@css-render/vue3-ssr'] },
  },
  /** SSR 上下文与 HTML 转换上下文不同，按页面保存本次渲染的样式。 */
  postRender(context) {
    const { docsRelativePath, collectNaiveStyles } = context as {
      docsRelativePath?: string
      collectNaiveStyles?: () => string
    }
    if (docsRelativePath) stylesByPage.set(docsRelativePath, collectNaiveStyles?.() || '')
  },
  /** 注入对应页面的 Naive UI 样式并释放缓存，支持并发静态渲染。 */
  transformHtml(html, _id, { page }) {
    const styles = stylesByPage.get(page) || ''
    stylesByPage.delete(page)
    return html.replace('</head>', `${styles}</head>`)
  },
})
