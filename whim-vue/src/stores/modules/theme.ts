import { darkTheme, type GlobalTheme, type GlobalThemeOverrides, lightTheme } from 'naive-ui'
import type { ThemeConfig } from '@/stores/type.ts'
import tinycolor from 'tinycolor2'
import { darkThemeOverrides, fontThemeOverrides, lightThemeOverrides } from '@/config/NaiveTheme.ts'
import systemSetting from '@/config/SystemSetting.ts'

/**
 * 返回包含项目默认水印的初始主题配置。
 */
function getDefaultThemeConfig(): ThemeConfig {
  return {
    theme: 'light',
    menuInverted: false,
    themeOverrides: {
      lightThemeOverrides: { ...lightThemeOverrides },
      darkThemeOverrides: { ...darkThemeOverrides },
    },
    showTabIcon: false,
    showTabs: true,
    showWatermark: false,
    watermarkText: systemSetting.title,
    showLogo: true,
    tabStyle: 'button',
  }
}

export const useThemeStore = defineStore('theme', {
  state: () => {
    const savedConfig = localStorage.getItem('themeConfig')
    const config = savedConfig ? (JSON.parse(savedConfig) as ThemeConfig) : getDefaultThemeConfig()
    return {
      config,
    }
  },
  getters: {
    getTheme(state): GlobalTheme {
      return state.config.theme === 'light' ? lightTheme : darkTheme
    },
    /** 合并当前主题和全局字体，兼容旧缓存及自定义主题色生成的配置。 */
    getThemeOverrides(state): GlobalThemeOverrides {
      const overrides =
        state.config.theme === 'light'
          ? state.config.themeOverrides.lightThemeOverrides
          : state.config.themeOverrides.darkThemeOverrides
      return {
        ...overrides,
        common: {
          ...overrides.common,
          ...fontThemeOverrides,
        },
      }
    },
    getMenuInverted(state): boolean {
      return state.config.menuInverted
    },
    getShowTabs(state): boolean {
      return state.config.showTabs
    },
    getShowTabIcon(state): boolean {
      return state.config.showTabIcon
    },
    getShowWatermark(state): boolean {
      return state.config.showWatermark
    },
    getWatermarkText(state): string {
      return state.config.watermarkText
    },
    getShowLogo(state): boolean {
      return state.config.showLogo
    },
    getTabStyle(state): 'button' | 'tag' {
      return state.config.tabStyle
    },
  },
  actions: {
    updateConfig(partialConfig?: Partial<ThemeConfig>) {
      this.config = {
        ...this.config,
        ...partialConfig,
      }
      localStorage.setItem('themeConfig', JSON.stringify(this.config))
    },
    setLightTheme() {
      this.updateConfig({
        theme: 'light',
      })
    },
    setDarkTheme() {
      this.updateConfig({
        theme: 'dark',
      })
    },
    setSystemTheme() {
      const newTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
      this.updateConfig({
        theme: newTheme,
      })
    },
    setThemeColor(value: string) {
      const color = tinycolor(value)
      if (!color.isValid()) return // 颜色无效时退出

      // 1. 生成亮色主题配色
      const lightCommon = {
        primaryColor: color.toHexString(),
        primaryColorHover: color.lighten(10).toHexString(), // 亮度 +10%
        primaryColorPressed: color.darken(10).toHexString(), // 亮度 -10%
        primaryColorSuppl: color.lighten(10).toHexString(), // 同 Hover
      }

      // 2. 使用亮色的 pressed 颜色作为暗色基础色
      const darkBaseColor = tinycolor(lightCommon.primaryColorHover)

      // 3. 生成暗色主题衍生色
      const darkCommon = {
        primaryColor: darkBaseColor.toHexString(), // 直接使用亮色的 pressed 色
        primaryColorHover: darkBaseColor.lighten(15).toHexString(), // 亮度 +15%
        primaryColorPressed: darkBaseColor.darken(10).toHexString(), // 亮度 -10%
        primaryColorSuppl: tinycolor({
          // 特殊处理
          h: (darkBaseColor.toHsl().h + 5) % 360, // 微调色相
          s: Math.max(0, darkBaseColor.toHsl().s - 0.2), // 降饱和度
          l: Math.max(0, darkBaseColor.toHsl().l - 0.3), // 降亮度
        }).toRgbString(), // 输出 RGB 格式
      }
      const menuThemeColor = {
        itemColorActiveInverted: darkBaseColor.toHexString(),
        itemColorActiveCollapsedInverted: darkBaseColor.toHexString(),
        itemColorActiveHoverInverted: darkBaseColor.lighten(15).toHexString(),
      }
      const switchThemeColor = {
        railColorActive: darkBaseColor.toHexString(),
        loadingColor: darkBaseColor.toHexString(),
      }

      // 4. 合并主题配置
      this.config.themeOverrides = {
        lightThemeOverrides: {
          common: { ...lightCommon },
        },
        darkThemeOverrides: {
          common: { ...darkCommon },
          Menu: {
            ...menuThemeColor,
          },
          Switch: {
            ...switchThemeColor,
          },
        },
      }
      this.updateConfig()
    },
    setMenuInverted(value: boolean) {
      this.updateConfig({ menuInverted: value })
    },
    setShowTabs(value: boolean) {
      this.updateConfig({ showTabs: value })
    },
    setShowTabIcon(value: boolean) {
      this.updateConfig({ showTabIcon: value })
    },
    setShowWatermark(value: boolean) {
      this.updateConfig({ showWatermark: value })
    },
    setWatermarkText(value: string) {
      this.updateConfig({ watermarkText: value })
    },
    setShowLogo(value: boolean) {
      this.updateConfig({ showLogo: value })
    },
    setTabStyle(value: 'button' | 'tag') {
      this.updateConfig({ tabStyle: value })
    },
    resetToDefault() {
      localStorage.removeItem('themeConfig')

      this.config = getDefaultThemeConfig()

      localStorage.setItem('themeConfig', JSON.stringify(this.config))
    },
  },
})
