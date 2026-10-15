import { h } from 'vue'
import { RouterLink, type RouteLocationRaw } from 'vue-router'
import { type MenuOption, NIcon } from 'naive-ui'
import { useIcon } from '@/components/icon/useIcon'
import type { NavigationNode } from '@/types/navigation'

export type SafeMenuOption = Omit<MenuOption, 'children'> & {
  name?: string
  path?: string
  target?: RouteLocationRaw
  directory?: boolean
  children?: SafeMenuOption[]
}

const { createIcon } = useIcon()

/** 仅负责将已解析导航转成菜单组件选项，所有入口共享同一导航目标。 */
export function buildNavigationMenus(nodes: NavigationNode[]): SafeMenuOption[] {
  return nodes.map((node) => ({
    key: node.key,
    name: node.name,
    label: node.directory
      ? node.name
      : node.external
        ? () => h('a', { href: node.path, target: '_blank', rel: 'noopener noreferrer' }, node.name)
        : () => h(RouterLink, { to: node.target! }, { default: () => node.name }),
    path: node.path,
    target: node.target,
    directory: node.directory,
    show: node.visible,
    isExternal: node.external,
    disabled: node.disabled,
    icon: node.icon ? () => h(NIcon, null, { default: createIcon(node.icon!) }) : undefined,
    children: node.children.length ? buildNavigationMenus(node.children) : undefined,
  }))
}

/** 查找菜单及祖先，隐藏详情页也可沿真实层级定位。 */
export function findMenuPath(menus: SafeMenuOption[], key: string): SafeMenuOption[] {
  for (const menu of menus) {
    if (menu.key === key) return [menu]
    const children = menu.children ? findMenuPath(menu.children, key) : []
    if (children.length) return [menu, ...children]
  }
  return []
}

/** 隐藏整条菜单分支，空目录不转成可点击页面。 */
export function getVisibleMenus(menus: SafeMenuOption[]): SafeMenuOption[] {
  return menus.flatMap((menu) => {
    if (menu.show === false) return []
    const children = menu.children ? getVisibleMenus(menu.children) : undefined
    if (menu.directory && !children?.length) return []
    return [{ ...menu, children: children?.length ? children : undefined }]
  })
}
