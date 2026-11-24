import { defineComponent, h, type Component } from 'vue'
import type { LocationQueryRaw, RouteLocationRaw, RouteRecordRaw } from 'vue-router'
import { MenuType, type HomeRoute, type UserMenu } from '@/stores/modules/permission/types'
import type { BreadcrumbItem, NavigationNode } from '@/types/navigation'

// 按菜单填写的文件路径加载业务页面，账号入口和异常页由固定路由加载。
const pageLoaders = import.meta.glob<{ default: Component }>([
  '/src/views/**/*.vue',
  '!/src/views/{login,profile,error,unavailable}/**',
])

export type MenuRoute = RouteRecordRaw & {
  name: string
  menuId: string
  signature: string
  viewPath?: string
}

/** 同一菜单重新匹配时复用已加载的组件，保留正在使用的页面实例。 */
function createPageComponent(
  name: string,
  loader: (() => Promise<{ default: Component }>) | undefined,
) {
  let page: Component | undefined
  return async () => {
    if (page) return page
    const { default: view } = await (loader ? loader() : import('@/views/unavailable/index.vue'))
    page ??= defineComponent({
      name,
      inheritAttrs: false,
      setup:
        (_, { attrs }) =>
        () =>
          h(view, attrs),
    })
    return page
  }
}

/** 目录跳转和未配置首页的账号使用首个可访问页面。 */
function firstNavigationTarget(nodes: NavigationNode[]): RouteLocationRaw | undefined {
  return nodes.find((node) => node.visible && !node.disabled && node.target)?.target
}

/** 一次解析授权树，统一路由元数据、菜单目标和目录跳转，不在各组件重复解释后端字段。 */
export function buildNavigation(
  menus: UserMenu[],
  homeRoutes: HomeRoute[],
  previousByName: ReadonlyMap<string, MenuRoute>,
) {
  const routes: MenuRoute[] = []
  const names = new Set<string>()
  const paths = new Set<string>()
  const ids = new Set<string>()
  let homeMenu: NavigationNode | undefined
  const menusByPath = new Map<
    string,
    { id: string; path: string; viewPath?: string; target?: RouteLocationRaw }
  >()
  /** 逐层建立导航；隐藏节点保留路由，按钮与停用分支不参与解析。 */
  function resolveNodes(
    nodes: UserMenu[],
    ancestry: BreadcrumbItem[] = [],
    visibleParentKey?: string,
  ): NavigationNode[] {
    return [...nodes]
      .sort((a, b) => a.sort - b.sort)
      .flatMap((menu) => {
        if (menu.status !== 0 || menu.menuType === MenuType.BUTTON) return []
        const id = String(menu.id)
        if (ids.has(id)) throw new Error('菜单节点重复，请检查菜单配置')
        ids.add(id)
        const external = menu.menuType === MenuType.EXTERNAL
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
        const directory = menu.menuType === MenuType.DIRECTORY
        const breadcrumbs = [...ancestry, { title: menu.menuName, path, name: key }]
        const children = directory
          ? resolveNodes(menu.children ?? [], breadcrumbs, visible ? key : visibleParentKey)
          : []
        // 兼容数据库已有的首页组件标识。
        const configuredPath = menu.component?.replace(/\.vue$/, '')
        const componentPath =
          configuredPath === 'layout/default/index' ? 'home/index' : configuredPath
        const loader = componentPath ? pageLoaders[`/src/views/${componentPath}.vue`] : undefined
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
          isHomeEntry: menu.builtIn === 1 && menu.perms === 'system:home:view',
          breadcrumbs,
          menuKey: visible ? key : visibleParentKey,
        }
        if (meta.isHomeEntry) homeMenu = node
        if (external) return [node]

        // 路由定义没有变化时保留匹配记录，菜单排序及页面默认参数仍可独立更新。
        const viewPath = loader ? (componentPath ?? undefined) : undefined
        const pathKey = path.toLowerCase()
        if (!menusByPath.has(pathKey))
          menusByPath.set(pathKey, { id, path, viewPath: directory ? undefined : viewPath, target })
        const signature = JSON.stringify([path, meta, directory, directory ? target : viewPath])
        const previous = previousByName.get(key)
        const sameMenu = previous?.menuId === id ? previous : undefined
        if (sameMenu?.signature === signature) {
          routes.push(sameMenu)
        } else if (directory) {
          routes.push({
            path,
            name: key,
            menuId: id,
            signature,
            redirect: (to) => {
              if (!target) return { path: '/noAccess' }
              const location = target as { path: string; query?: LocationQueryRaw }
              return { ...location, query: { ...location.query, ...to.query }, hash: to.hash }
            },
            meta,
          })
        } else {
          const component = sameMenu?.viewPath === viewPath ? sameMenu?.component : undefined
          routes.push({
            path,
            name: key,
            menuId: id,
            signature,
            viewPath,
            component: component ?? createPageComponent(key, loader),
            props: true,
            meta,
          })
        }
        return [node]
      })
  }
  const tree = resolveNodes(menus)
  let entryTarget: RouteLocationRaw | undefined
  for (const home of homeRoutes) {
    const existing = menusByPath.get(home.path.toLowerCase())
    if (existing) {
      // 不可访问、已换址或已更换组件的菜单不能转为独立首页。
      if (
        existing.id !== String(home.menuId) ||
        existing.path !== home.path ||
        existing.viewPath !== home.component
      )
        continue
      entryTarget = existing.target
    } else {
      const name = 'Configured-Home'
      const loader = pageLoaders[`/src/views/${home.component}.vue`]
      if (home.menuId != null || !loader || names.has(name)) continue
      const previous = previousByName.get(name)
      const menuKey = homeMenu?.key ?? name
      const signature = JSON.stringify([home.path, home.component, menuKey])
      routes.push(
        previous?.signature === signature
          ? previous
          : {
              path: home.path,
              name,
              menuId: name,
              viewPath: home.component,
              signature,
              component: createPageComponent(name, loader),
              meta: {
                title: '首页',
                icon: 'house',
                menuKey,
                isTab: true,
                breadcrumbs: [{ title: '首页', path: home.path, name }],
              },
            },
      )
      entryTarget = { path: home.path }
    }
    if (!homeMenu) {
      tree.unshift({
        key: existing ? 'Home-Shortcut' : 'Configured-Home',
        name: '首页',
        icon: 'house',
        target: entryTarget,
        visible: true,
        disabled: false,
        external: false,
        directory: false,
        children: [],
      })
    }
    break
  }
  entryTarget ??= firstNavigationTarget(tree)
  if (homeMenu && entryTarget) {
    homeMenu.target = entryTarget
    homeMenu.disabled = false
  }
  return { routes, tree, entryTarget }
}
