import { defineAsyncComponent, defineComponent, h, markRaw } from 'vue'
import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import type { MenuItem } from '@/views/system/menu/hooks/types'
import type { BreadcrumbItem } from '@/types/navigation'
export const pageModules = import.meta.glob<{ default: Component }>('@/views/**/index.vue')
export const componentOptions = Object.keys(pageModules)
  .filter((path) => !/^\/src\/views\/(login|profile|error|welcome|unavailable)\//.test(path))
  .map((path) => ({
    label: path.slice('/src/views/'.length, -4),
    value: path.slice('/src/views/'.length, -4),
  }))

/** 目录跳转到第一个可导航内部页面，排除外链、隐藏或未开放页面。 */
export function firstMenuPath(nodes: MenuItem[]): string | undefined {
  for (const node of nodes) {
    if (node.visible === 1 || node.status !== 0) continue
    if (node.menuType === 2 && node.path && pageModules[`/src/views/${node.component}.vue`])
      return node.path
    const path = firstMenuPath(node.children ?? [])
    if (path) return path
  }
}

/** 菜单层级用于导航和面包屑，路由平铺使页面缓存不受目录层数影响。 */
export function buildNavigationRoutes(
  nodes: MenuItem[],
  ancestry: BreadcrumbItem[] = [],
): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []
  for (const node of nodes) {
    if (node.menuType === 3 || node.menuType === 4 || !node.path || !node.routeName) continue
    const breadcrumbs = [
      ...ancestry,
      { title: node.menuName, path: node.path, name: node.routeName },
    ]
    if (node.menuType === 1) {
      const redirect = firstMenuPath(node.children ?? [])
      if (redirect) routes.push({ path: node.path, name: node.routeName, redirect })
      routes.push(...buildNavigationRoutes(node.children ?? [], breadcrumbs))
      continue
    }
    const loader = pageModules[`/src/views/${node.component}.vue`]
    const view = defineAsyncComponent(loader ?? pageModules['/src/views/unavailable/index.vue']!)
    // 包装组件名与路由名一致，避免页面 defineOptions 不一致导致缓存无法清理。
    const component = markRaw(defineComponent({ name: node.routeName, setup: () => () => h(view) }))
    routes.push({
      path: node.path,
      name: node.routeName,
      component,
      props: true,
      meta: {
        title: node.menuName,
        icon: node.icon || undefined,
        keepAlive: node.isCache === 1,
        isMenu: true,
        isShow: node.visible === 0,
        breadcrumbs,
        menuKey: node.routeName,
      },
    })
  }
  return routes
}
