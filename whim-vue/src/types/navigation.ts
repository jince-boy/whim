import type { RouteLocationRaw } from 'vue-router'

/** 导航和面包屑共享菜单路径。 */
export interface BreadcrumbItem {
  title: string
  path?: string
  name?: string
}
/** 后端菜单解析后的导航节点，路由、菜单、搜索共用同一份解析结果。 */
export interface NavigationNode {
  key: string
  name: string
  path?: string
  target?: RouteLocationRaw
  icon?: string
  visible: boolean
  disabled: boolean
  external: boolean
  directory: boolean
  children: NavigationNode[]
}
