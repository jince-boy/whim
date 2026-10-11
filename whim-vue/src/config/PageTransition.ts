import type { PageTransitionMode } from '@/stores/type'

export const pageTransitionOptions: { label: string; value: PageTransitionMode }[] = [
  { label: '滑动', value: 'slide' },
  { label: '淡入淡出', value: 'fade' },
  { label: '渐隐滑动', value: 'fadeSlide' },
  { label: '缩放', value: 'zoom' },
  { label: '无动画', value: 'none' },
]

/** 校验页面切换动画，供配置面板和缓存恢复共用。 */
export function isPageTransitionMode(value: unknown): value is PageTransitionMode {
  return pageTransitionOptions.some((option) => option.value === value)
}
