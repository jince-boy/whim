import { type RouteRecordRaw, RouterLink } from 'vue-router'
import { type MenuOption, NIcon } from 'naive-ui'
import { useIcon } from '@/components/icon/useIcon.ts'

export type SafeMenuOption = Omit<MenuOption, 'children'> & {
  name?: string
  path?: string
  children?: SafeMenuOption[]
}

const { createIcon } = useIcon()
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
