import type { GlobalThemeOverrides } from 'naive-ui'

export interface MenuItem {
  id: string
  name: string
  title: string
  path: string
  queryParam: string
  component: string
  icon: string
  keepAlive: 0 | 1
  visible: 0 | 1
  redirect: string
  remark: string
  children?: MenuItem[]
}

// 定义 TabState 的类型
export interface TabState {
  title: string
  path: string
  name: string
  icon: string
  closeable: boolean
  focused?: boolean
}

export interface ThemeConfig {
  theme: 'light' | 'dark'
  themeOverrides: {
    lightThemeOverrides: GlobalThemeOverrides
    darkThemeOverrides: GlobalThemeOverrides
  }
  menuInverted: boolean // 菜单是否反转
  showTabs: boolean // 显示标签
  showTabIcon: boolean // 显示标签图标
  showWatermark: boolean // 显示水印
  watermarkText: string
  showLogo: boolean
  tabStyle: 'button' | 'tag'
}
