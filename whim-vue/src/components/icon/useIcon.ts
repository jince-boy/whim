import { h } from 'vue'
import Icon from './Icon.vue'

/** 创建供按钮、菜单使用的图标渲染函数。 */
export function createIcon(name?: string | null, size?: number | string, color?: string) {
  return () => h(Icon, { name, size, color })
}

/** 保留现有 useIcon 调用方式。 */
export function useIcon() {
  return { createIcon }
}
