import { fileURLToPath } from 'node:url'
import { preview } from 'vite'

// VitePress 1.x 的原生预览不传递 host，使用 Vite 静态预览明确绑定本机。
const server = await preview({
  root: fileURLToPath(new URL('..', import.meta.url)),
  configFile: false,
  appType: 'mpa',
  base: process.env.DOCS_BASE || '/',
  build: { outDir: '.vitepress/dist' },
  preview: { host: '127.0.0.1', port: 4175, strictPort: true },
})

process.stdout.write('文档静态预览已启动：http://127.0.0.1:4175\n')

/** 收到退出信号或到达自动验证时限时，关闭本次预览。 */
function closePreview() {
  server.httpServer.close()
}

process.once('SIGINT', closePreview)
process.once('SIGTERM', closePreview)
const timeout = Number(process.env.DOCS_PREVIEW_TIMEOUT_MS || 0)
if (Number.isFinite(timeout) && timeout > 0) setTimeout(closePreview, timeout).unref()
