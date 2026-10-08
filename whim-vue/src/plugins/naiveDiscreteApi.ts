import { type ConfigProviderProps, createDiscreteApi } from 'naive-ui'
import { useThemeStore } from '@/stores/modules/theme.ts'

/** 注册响应当前主题配置的全局消息、通知、对话框和加载条。 */
export function setupNaiveDiscreteApi() {
  const configProviderPropsRef = computed<ConfigProviderProps>(() => ({
    theme: useThemeStore().getTheme,
    themeOverrides: useThemeStore().getThemeOverrides,
  }))
  const { message, notification, dialog, loadingBar, modal } = createDiscreteApi(
    ['message', 'notification', 'dialog', 'loadingBar', 'modal'],
    {
      configProviderProps: configProviderPropsRef,
    },
  )

  window['$message'] = message
  window['$dialog'] = dialog
  window['$notification'] = notification
  window['$loadingBar'] = loadingBar
  window['$modal'] = modal
}
