import type { Theme } from 'vitepress'
import { setup } from '@css-render/vue3-ssr'
import Layout from './Layout.vue'
import DemoBlock from './components/DemoBlock.vue'
import { collectStylesKey } from './types'
import '@/assets/style/fonts.css'
import './styles.css'

export default {
  Layout,
  /** 注册文档演示容器；静态渲染按页面独立收集组件样式。 */
  enhanceApp({ app }) {
    app.component('DemoBlock', DemoBlock)
    if (import.meta.env.SSR) {
      const { collect } = setup(app)
      app.provide(collectStylesKey, collect)
    }
  },
} satisfies Theme
