import { type GlobalThemeOverrides } from 'naive-ui'

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
}

export const lightThemeOverrides: GlobalThemeOverrides = {
  ...commonThemeOverrides, // 复用 common 配置
  Layout: {
    footerColor: '#fff',
    colorEmbedded: '#f0f2f5',
  },
}

export const darkThemeOverrides: GlobalThemeOverrides = {
  ...commonThemeOverrides, // 复用 common 配置
  Layout: {
    footerColor: 'rgb(24,24,28)',
  },
}
