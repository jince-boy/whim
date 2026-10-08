import { h } from 'vue'
import Icon from '@/components/icon/Icon.vue'

export function useIcon() {
  /**
   * createIcon 方法，返回一个渲染函数
   * @param name 图标名称
   * @param size 图标大小
   * @param color 图标颜色
   */
  function createIcon(name: string, size: number = 16, color?: string) {
    return () => h(Icon, { name, size, color })
  }

  return { createIcon }
}
