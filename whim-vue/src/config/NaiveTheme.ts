import { lightTheme, type GlobalThemeOverrides } from 'naive-ui'

// 默认主色直接取自 Naive UI，深色模式使用组件库对应的原生配色。
export const defaultPrimaryColor = lightTheme.common.primaryColor

// 使用同一套全局字体变量，避免原生元素和组件库的字体不一致。
export const fontThemeOverrides = {
  fontFamily: 'var(--app-font-family)',
  fontFamilyMono: 'var(--app-font-family-mono)',
}

// 提取公共配置
const commonThemeOverrides: GlobalThemeOverrides = {
  common: {
    ...fontThemeOverrides,
    fontWeightStrong: '600',
  },
  Dialog: {
    contentMargin: '32px 0',
    iconSize: '22px',
  },
  Card: {
    paddingSmall: '12px',
    paddingMedium: '12px',
    paddingLarge: '12px',
    paddingHuge: '12px',
  },
}

export const lightThemeOverrides: GlobalThemeOverrides = {
  ...commonThemeOverrides,
}

export const darkThemeOverrides: GlobalThemeOverrides = {
  ...commonThemeOverrides,
}
