import { type GlobalThemeOverrides } from 'naive-ui'

// 默认主色与登录页的鼠尾草绿保持一致。
export const defaultPrimaryColor = '#4f8466'

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
  ...commonThemeOverrides, // 复用 common 配置
  common: {
    ...commonThemeOverrides.common,
    bodyColor: '#f6f8f4',
  },
  Layout: {
    footerColor: '#fff',
    colorEmbedded: '#f6f8f4',
  },
}

export const darkThemeOverrides: GlobalThemeOverrides = {
  ...commonThemeOverrides, // 复用 common 配置
  Layout: {
    footerColor: 'rgb(24,24,28)',
  },
}
