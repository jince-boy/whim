import { h } from 'vue'
import AppIcon from '../AppIcon.vue'

/** 创建供按钮、菜单使用的图标渲染函数。 */
export function createIcon(name?: string | null, size?: number | string, color?: string) {
  return () => h(AppIcon, { name, size, color })
}

/** 保留现有 useIcon 调用方式。 */
export function useIcon() {
  return { createIcon }
}
