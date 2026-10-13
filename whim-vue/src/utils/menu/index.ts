import { type RouteRecordRaw, RouterLink } from 'vue-router'
import { type MenuOption, NIcon } from 'naive-ui'
import { useIcon } from '@/components/icon/useIcon.ts'
import type { MenuItem } from '@/views/system/menu/hooks/types'
import { firstMenuPath, pageModules } from '@/utils/navigation'

export type SafeMenuOption = Omit<MenuOption, 'children'> & {
  name?: string
  path?: string
  children?: SafeMenuOption[]
}

const { createIcon } = useIcon()

/** 从用户授权树直接生成组件选项，按钮不进入导航，目录保持真实层级。 */
export function buildUserMenus(nodes: MenuItem[]): SafeMenuOption[] {
  return nodes.flatMap((node) => {
    if (node.menuType === 3 || node.status !== 0) return []
    const children = buildUserMenus(node.children ?? [])
    if (node.menuType === 1 && !children.length) return []
    const external = node.menuType === 4
    const path = node.menuType === 1 ? firstMenuPath(node.children ?? []) : node.path || undefined
    const query = Object.fromEntries(new URLSearchParams(node.param || ''))
    const label =
      node.menuType === 1
        ? node.menuName
        : external
          ? () =>
              h('a', { href: path, target: '_blank', rel: 'noopener noreferrer' }, node.menuName)
          : () =>
              h(RouterLink, { to: { path: path || '/', query } }, { default: () => node.menuName })
    return [
      {
        key: node.routeName || `external:${node.id}`,
        name: node.menuName,
        label,
        path,
        show: node.visible === 0,
        isExternal: external,
        disabled: node.menuType === 2 && !pageModules[`/src/views/${node.component}.vue`],
        icon: node.icon ? () => h(NIcon, null, { default: createIcon(node.icon!) }) : undefined,
        children: children.length ? children : undefined,
      },
    ]
  })
}

/** 查找菜单及其父级，支持面包屑和混合布局定位一级菜单。 */
export function findMenuPath(menus: SafeMenuOption[], key: string): SafeMenuOption[] {
  for (const menu of menus) {
    if (menu.key === key) return [menu]
    if (menu.children) {
      const childPath = findMenuPath(menu.children, key)
      if (childPath.length) return [menu, ...childPath]
    }
  }
  return []
}

/** 排除隐藏菜单和没有可见子项的目录，保持各布局的菜单可见性一致。 */
export function getVisibleMenus(menus: SafeMenuOption[]): SafeMenuOption[] {
  return menus.flatMap((menu) => {
    if (menu.show === false) return []
    const children = menu.children ? getVisibleMenus(menu.children) : undefined
    if (menu.children?.length && !children?.length && !menu.path) return []
    return [{ ...menu, children: children?.length ? children : undefined }]
  })
}
/**
 * 路径拼接
 * @param base
 * @param path
 */
export const joinPaths = (base: string, path: string): string => {
  if (path.startsWith('/')) return path
  if (base.endsWith('/')) return base + path
  return `${base}/${path}`
}
/**
 * 构建菜单
 * @param routes
 * @param basePath
 */
export const buildMenus = (routes: RouteRecordRaw[], basePath = ''): SafeMenuOption[] => {
  return routes.reduce<SafeMenuOption[]>((result, route) => {
    const fullPath = joinPaths(basePath, route.path)
    const meta = route.meta || {}
    const {
      title = '未命名',
      isMenu = false,
      isShow = true,
      isExternal = false,
      icon: iconName,
    } = meta

    const hasChildren = Array.isArray(route.children) && route.children.length > 0
    const key = typeof route.name === 'string' ? route.name : fullPath
    const icon = iconName ? () => h(NIcon, null, { default: createIcon(iconName) }) : undefined

    // 非菜单项但有子路由，直接递归处理子路由
    if (!isMenu && hasChildren) {
      return [...result, ...buildMenus(route.children!, fullPath)]
    }

    // 非菜单项且无子路由，跳过
    if (!isMenu) {
      return result
    }

    // 菜单项处理
    const menuItem: SafeMenuOption = {
      label: title,
      key,
      icon,
      show: isShow,
      name: title,
      path: '',
    }

    // 有子路由的处理
    if (hasChildren) {
      const childrenMenu = buildMenus(route.children!, fullPath)
      if (childrenMenu.length > 0) {
        menuItem.children = childrenMenu
      }
      result.push(menuItem)
      return result
    }
    // 处理外部链接（从 redirect 获取链接地址）
    if (isExternal && route.meta!.link) {
      const externalUrl = route.meta!.link as string
      menuItem.label = () =>
        h(
          'a',
          {
            href: externalUrl,
            target: '_blank',
            rel: 'noopener noreferrer',
          },
          title,
        )
      menuItem.path = externalUrl
      menuItem.isExternal = true
    }

    // 无子路由但有组件的处理
    if (route.component) {
      menuItem.label = () => h(RouterLink, { to: fullPath }, { default: () => title })
      menuItem.path = fullPath
    }

    result.push(menuItem)
    return result
  }, [])
}
