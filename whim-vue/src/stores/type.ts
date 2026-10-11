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

export type ThemeMode = 'light' | 'dark' | 'system'
export type LayoutMode = 'side' | 'mixed' | 'top'
export type PageTransitionMode = 'slide' | 'fade' | 'fadeSlide' | 'zoom' | 'none'
export type TabStyle = 'button' | 'line' | 'browser'

export interface ThemeConfig {
  theme: ThemeMode
  layoutMode: LayoutMode
  pageTransition: PageTransitionMode
  primaryColor: string
  menuInverted: boolean // 菜单是否反转
  showTabs: boolean // 显示标签
  cacheTabs: boolean // 按菜单设置缓存标签页面
  showTabIcon: boolean // 显示标签图标
  showWatermark: boolean // 显示水印
  watermarkText: string
  showLogo: boolean
  tabStyle: TabStyle
}
