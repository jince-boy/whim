import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  if (env.VITE_USE_MOCK && !['true', 'false'].includes(env.VITE_USE_MOCK)) {
    throw new Error('VITE_USE_MOCK 只能配置为 true 或 false')
  }
  // 校验项目文案，避免遗漏本地配置时生成未替换的 HTML 占位符。
  for (const key of ['VITE_APP_TITLE', 'VITE_APP_DESCRIPTION', 'VITE_APP_COPYRIGHT']) {
    if (!env[key]?.trim()) {
      throw new Error(`缺少环境变量 ${key}，请复制 .env.example 为 .env 并填写配置`)
    }
  }
  const apiBaseUrl = env.VITE_API_BASE_URL
  if (!apiBaseUrl || !/^\/(?!\/)[^?#\\\s]*[^/?#\\\s]$/.test(apiBaseUrl)) {
    throw new Error('VITE_API_BASE_URL 必须是以 / 开头、不带末尾 /、查询参数或片段的接口路径前缀')
  }
  // 转义路径中的正则字符，并限制边界，避免 /api 意外匹配 /apiExtra。
  const apiProxyPattern = `^${apiBaseUrl.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(?=/|\\?|$)`
  return {
    plugins: [
      vue(),
      vueJsx(),
      vueDevTools(),
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia',
          {
            'naive-ui': ['useDialog', 'useMessage', 'useNotification', 'useLoadingBar'],
          },
        ],
      }),
      Components({
        resolvers: [NaiveUiResolver()],
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: '0.0.0.0',
      port: Number(env.VITE_PORT), // 使用环境变量，默认3000
      proxy: {
        [apiProxyPattern]: {
          target: 'http://localhost:8089',
          changeOrigin: true,
          // 只剥离公共前缀，后端继续使用 /system/... 路径。
          rewrite: (path: string) => path.slice(apiBaseUrl.length).replace(/^(\?|$)/, '/$1'),
        },
      },
    },
  }
})
