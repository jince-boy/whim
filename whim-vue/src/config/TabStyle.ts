import type { TabStyle } from '@/stores/type'

export const tabStyleOptions: { label: string; value: TabStyle }[] = [
  { label: '按钮风格', value: 'button' },
  { label: '下划线分割', value: 'line' },
  { label: '浏览器标签', value: 'browser' },
]

/** 校验标签风格，供主题面板和缓存恢复共用。 */
export function isTabStyle(value: unknown): value is TabStyle {
  return tabStyleOptions.some((option) => option.value === value)
}
