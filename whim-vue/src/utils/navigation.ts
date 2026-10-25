import { defineAsyncComponent, defineComponent, h, markRaw } from 'vue'
import type { Component } from 'vue'
import type { LocationQueryRaw, RouteLocationRaw, RouteRecordRaw } from 'vue-router'
import type { MenuItem } from '@/views/system/menu/hooks/types'
import type { BreadcrumbItem, NavigationNode } from '@/types/navigation'

const pageModules = import.meta.glob<{ default: Component }>('@/views/**/index.vue')
export const componentOptions = Object.keys(pageModules)
  .filter((path) => !/^\/src\/views\/(login|profile|error|unavailable)\//.test(path))
  .map((path) => ({
    label: path.slice('/src/views/'.length, -4),
    value: path.slice('/src/views/'.length, -4),
  }))
const availableComponents = new Set(componentOptions.map((option) => option.value))

/** 入口只从可见、组件已实现的内部页面中选择；以后独立首页配置也使用此授权范围。 */
export function firstNavigationTarget(nodes: NavigationNode[]): RouteLocationRaw | undefined {
  for (const node of nodes) {
    if (!node.visible || node.disabled || node.external) continue
    if (!node.directory) return node.target
    const target = firstNavigationTarget(node.children)
    if (target) return target
  }
}

/** 一次解析授权树，统一路由元数据、菜单目标和目录跳转，不在各组件重复解释后端字段。 */
export function buildNavigation(menus: MenuItem[]) {
  const routes: RouteRecordRaw[] = []
  const names = new Set<string>()
  const paths = new Set<string>()
  const ids = new Set<string>()
  /** 逐层建立导航；隐藏节点保留路由，按钮与停用分支不参与解析。 */
  function resolveNodes(
    nodes: MenuItem[],
    ancestry: BreadcrumbItem[] = [],
    visibleParentKey?: string,
  ): NavigationNode[] {
    return [...nodes]
      .sort((a, b) => a.sort - b.sort)
      .flatMap((menu) => {
        if (menu.status !== 0 || menu.menuType === 3) return []
        const id = String(menu.id)
        if (ids.has(id)) throw new Error('菜单节点重复，请检查菜单配置')
        ids.add(id)
        const external = menu.menuType === 4
        const key = external ? `external:${id}` : menu.routeName
        if (!key || !menu.path) throw new Error('菜单缺少路由名称或地址')
        const path = external || menu.path.startsWith('/') ? menu.path : `/${menu.path}`
        if (names.has(key)) throw new Error('菜单路由名称重复')
        names.add(key)
        if (!external) {
          if (paths.has(path)) throw new Error('菜单路由地址重复')
          paths.add(path)
        } else if (!/^https?:\/\//i.test(path)) {
          throw new Error('菜单外链地址无效')
        }
        const visible = menu.visible === 0
        const directory = menu.menuType === 1
        const breadcrumbs = [...ancestry, { title: menu.menuName, path, name: key }]
        const children = directory
          ? resolveNodes(menu.children ?? [], breadcrumbs, visible ? key : visibleParentKey)
          : []
        const loader = availableComponents.has(menu.component ?? '')
          ? pageModules[`/src/views/${menu.component}.vue`]
          : undefined
        const query: LocationQueryRaw = Object.create(null)
        new URLSearchParams(menu.param || '').forEach((value, name) => {
          const previous = query[name]
          query[name] =
            previous === undefined
              ? value
              : [...(Array.isArray(previous) ? previous : [previous]), value]
        })
        const target = directory
          ? firstNavigationTarget(children)
          : external
            ? undefined
            : { path, query }
        const node: NavigationNode = {
          key,
          name: menu.menuName,
          path: external ? path : undefined,
          target,
          icon: menu.icon || undefined,
          visible,
          disabled: directory
            ? !target && !children.some((child) => child.visible && !child.disabled)
            : !external && !loader,
          external,
          directory,
          children,
        }
        const meta = {
          title: menu.menuName,
          icon: menu.icon || undefined,
          keepAlive: menu.isCache === 1,
          isTab: !directory,
          breadcrumbs,
          menuKey: visible ? key : visibleParentKey,
        }
        if (directory) {
          if (target)
            routes.push({
              path,
              name: key,
              redirect: (to) => {
                const location = target as { path: string; query?: LocationQueryRaw }
                return { ...location, query: { ...location.query, ...to.query }, hash: to.hash }
              },
              meta,
            })
        } else if (!external) {
          const view = defineAsyncComponent(
            loader ?? pageModules['/src/views/unavailable/index.vue']!,
          )
          // 名称与路由保持一致，独立控制每个页面的缓存；透传参数到实际页面。
          const component = markRaw(
            defineComponent({
              name: key,
              inheritAttrs: false,
              setup:
                (_, { attrs }) =>
                () =>
                  h(view, attrs),
            }),
          )
          routes.push({ path, name: key, component, props: true, meta })
        }
        return [node]
      })
  }
  const tree = resolveNodes(menus)
  return { routes, tree, entryTarget: firstNavigationTarget(tree) }
}
