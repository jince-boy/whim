import { defineStore } from 'pinia'
import { darkTheme, type GlobalTheme, type GlobalThemeOverrides, lightTheme } from 'naive-ui'
import type {
  LayoutMode,
  PageTransitionMode,
  TabStyle,
  ThemeConfig,
  ThemeMode,
} from '@/stores/type'
import tinycolor from 'tinycolor2'
import { darkThemeOverrides, fontThemeOverrides, lightThemeOverrides } from '@/config/NaiveTheme'
import systemSetting from '@/config/SystemSetting'
import { isPageTransitionMode } from '@/config/PageTransition'
import { isTabStyle } from '@/config/TabStyle'

/** 返回主题和布局的完整默认配置。 */
function getDefaultThemeConfig(): ThemeConfig {
  return {
    theme: 'light',
    layoutMode: 'side',
    pageTransition: 'slide',
    primaryColor: '#18a058',
    menuInverted: false,
    showTabIcon: false,
    showTabs: true,
    cacheTabs: true,
    showWatermark: false,
    watermarkText: systemSetting.title,
    showLogo: true,
    tabStyle: 'button',
  }
}

/** 校验缓存并补齐新增字段，兼容旧主题色配置和损坏的本地数据。 */
function loadThemeConfig(): ThemeConfig {
  const config = getDefaultThemeConfig()
  try {
    const saved = localStorage.getItem('themeConfig')
    if (!saved) return config
    const parsed: unknown = JSON.parse(saved)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return config
    const cached = parsed as Omit<Partial<ThemeConfig>, 'tabStyle'> & {
      tabStyle?: unknown
      themeOverrides?: { lightThemeOverrides?: { common?: { primaryColor?: unknown } } }
    }
    if (cached.theme === 'light' || cached.theme === 'dark' || cached.theme === 'system') {
      config.theme = cached.theme
    }
    if (
      cached.layoutMode === 'side' ||
      cached.layoutMode === 'mixed' ||
      cached.layoutMode === 'top'
    ) {
      config.layoutMode = cached.layoutMode
    }
    if (isPageTransitionMode(cached.pageTransition)) config.pageTransition = cached.pageTransition
    const primaryColor =
      cached.primaryColor ?? cached.themeOverrides?.lightThemeOverrides?.common?.primaryColor
    if (typeof primaryColor === 'string' && tinycolor(primaryColor).isValid()) {
      config.primaryColor = tinycolor(primaryColor).toHexString()
    }
    for (const key of [
      'menuInverted',
      'showTabIcon',
      'showTabs',
      'cacheTabs',
      'showWatermark',
      'showLogo',
    ] as const) {
      if (typeof cached[key] === 'boolean') config[key] = cached[key]
    }
    if (typeof cached.watermarkText === 'string')
      config.watermarkText = cached.watermarkText.slice(0, 100)
    if (isTabStyle(cached.tabStyle)) {
      config.tabStyle = cached.tabStyle
    } else if (cached.tabStyle === 'card') {
      config.tabStyle = 'button'
    } else if (cached.tabStyle === 'tag' || cached.tabStyle === 'capsule') {
      // 兼容此前保存的标签、胶囊配置。
      config.tabStyle = 'browser'
    }
  } catch (error) {
    console.warn('读取主题配置失败，已使用默认配置', error)
  }
  return config
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    config: loadThemeConfig(),
    systemDark: window.matchMedia('(prefers-color-scheme: dark)').matches,
  }),
  getters: {
    /** 根据用户选择和系统偏好计算实际使用的主题。 */
    getTheme(state): GlobalTheme {
      const dark =
        state.config.theme === 'dark' || (state.config.theme === 'system' && state.systemDark)
      return dark ? darkTheme : lightTheme
    },
    /** 从基础颜色独立生成派生色，并保留默认组件样式。 */
    getThemeOverrides(): GlobalThemeOverrides {
      const dark = this.getTheme === darkTheme
      const defaults = dark ? darkThemeOverrides : lightThemeOverrides
      const primary = tinycolor(this.config.primaryColor)
      const base = dark ? primary.clone().lighten(10) : primary
      const primaryColor = base.toHexString()
      const hover = base
        .clone()
        .lighten(dark ? 8 : 10)
        .toHexString()
      const pressed = base.clone().darken(10).toHexString()
      const activeTextColor = tinycolor.mostReadable(primaryColor, ['#fff', '#000'])!.toHexString()
      return {
        ...defaults,
        common: {
          ...defaults.common,
          ...fontThemeOverrides,
          primaryColor,
          primaryColorHover: hover,
          primaryColorPressed: pressed,
          primaryColorSuppl: hover,
        },
        Menu: {
          ...defaults.Menu,
          itemColorActiveInverted: primaryColor,
          itemColorActiveCollapsedInverted: primaryColor,
          itemColorActiveHoverInverted: primaryColor,
          itemTextColorActiveInverted: activeTextColor,
          itemTextColorActiveHoverInverted: activeTextColor,
          itemIconColorActiveInverted: activeTextColor,
          itemIconColorActiveHoverInverted: activeTextColor,
        },
      }
    },
    /** 返回菜单反色开关。 */
    getMenuInverted: (state): boolean => state.config.menuInverted,
    /** 返回标签栏显示开关。 */
    getShowTabs: (state): boolean => state.config.showTabs,
    /** 返回标签页面缓存的全局开关。 */
    getCacheTabs: (state): boolean => state.config.cacheTabs,
    /** 返回标签图标显示开关。 */
    getShowTabIcon: (state): boolean => state.config.showTabIcon,
    /** 返回水印显示开关。 */
    getShowWatermark: (state): boolean => state.config.showWatermark,
    /** 返回水印文字。 */
    getWatermarkText: (state): string => state.config.watermarkText,
    /** 返回标识显示开关。 */
    getShowLogo: (state): boolean => state.config.showLogo,
    /** 返回标签栏风格。 */
    getTabStyle: (state): TabStyle => state.config.tabStyle,
  },
  actions: {
    /** 更新并持久化用户设置，存储不可用时仍允许当前页面切换。 */
    updateConfig(partialConfig: Partial<ThemeConfig>) {
      this.config = { ...this.config, ...partialConfig }
      try {
        localStorage.setItem('themeConfig', JSON.stringify(this.config))
      } catch (error) {
        console.warn('保存主题配置失败，当前设置仅在本次使用中生效', error)
      }
    },
    /** 保存主题模式，系统模式保留跟随系统的意图。 */
    setThemeMode(value: ThemeMode) {
      this.updateConfig({ theme: value })
    },
    /** 同步系统偏好，不覆盖用户设置或写入缓存。 */
    syncSystemTheme(value: boolean) {
      this.systemDark = value
    },
    /** 设置布局模式。 */
    setLayoutMode(value: LayoutMode) {
      this.updateConfig({ layoutMode: value })
    },
    /** 设置主内容区的页面切换动画。 */
    setPageTransition(value: PageTransitionMode) {
      this.updateConfig({ pageTransition: value })
    },
    /** 校验并保存基础主题色。 */
    setThemeColor(value: string) {
      const color = tinycolor(value)
      if (color.isValid()) this.updateConfig({ primaryColor: color.toHexString() })
    },
    /** 设置菜单反色。 */
    setMenuInverted(value: boolean) {
      this.updateConfig({ menuInverted: value })
    },
    /** 设置标签栏显示状态。 */
    setShowTabs(value: boolean) {
      this.updateConfig({ showTabs: value })
    },
    /** 设置标签页面缓存；菜单自身的缓存设置仍决定页面是否可缓存。 */
    setCacheTabs(value: boolean) {
      this.updateConfig({ cacheTabs: value })
    },
    /** 设置标签图标显示状态。 */
    setShowTabIcon(value: boolean) {
      this.updateConfig({ showTabIcon: value })
    },
    /** 设置水印显示状态。 */
    setShowWatermark(value: boolean) {
      this.updateConfig({ showWatermark: value })
    },
    /** 设置水印文字。 */
    setWatermarkText(value: string) {
      this.updateConfig({ watermarkText: value })
    },
    /** 设置标识显示状态。 */
    setShowLogo(value: boolean) {
      this.updateConfig({ showLogo: value })
    },
    /** 设置标签栏风格。 */
    setTabStyle(value: TabStyle) {
      this.updateConfig({ tabStyle: value })
    },
    /** 恢复并保存完整默认配置。 */
    resetToDefault() {
      this.updateConfig(getDefaultThemeConfig())
    },
  },
})
