import { computed } from 'vue'
import { type ConfigProviderProps, createDiscreteApi, dateZhCN, zhCN } from 'naive-ui'
import store from '@/stores'
import { useThemeStore } from '@/stores/modules/theme'

// 在组件 setup 外创建单一实例；显式传入 Pinia，初次导航前即可使用。
const themeStore = useThemeStore(store)
const configProviderProps = computed<ConfigProviderProps>(() => ({
  theme: themeStore.getTheme,
  themeOverrides: themeStore.getThemeOverrides,
  locale: zhCN,
  dateLocale: dateZhCN,
}))

/** 消息和进度条统一使用官方离散 API，随应用主题同步更新。 */
export const { message, loadingBar } = createDiscreteApi(['message', 'loadingBar'], {
  configProviderProps,
})
