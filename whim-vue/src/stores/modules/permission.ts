import type { MenuItem } from '@/stores/type.ts'
import type { RouteRecordRaw } from 'vue-router'
import router, { constantRouter } from '@/router'
import { buildMenus, type SafeMenuOption } from '@/utils/menu'
import type { MenuOption } from 'naive-ui'

const modules = import.meta.glob('@/views/**/*.vue')
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    dynamicRoutes: [] as RouteRecordRaw[],
    menus: [] as SafeMenuOption[],
  }),
  getters: {
    getDynamicRoutes(state): RouteRecordRaw[] {
      return state.dynamicRoutes
    },
    getMenus(state): MenuOption[] {
      return state.menus
    },
  },
  actions: {
    /**
     * 生成动态路由
     * @param menus
     */
    generateRoutes(menus: MenuItem[]) {
      // 内部递归方法，真正构建路由
      const buildRoutes = (menus: MenuItem[]): RouteRecordRaw[] => {
        const result: RouteRecordRaw[] = []
        menus.forEach((menu) => {
          const route: RouteRecordRaw = {
            path: menu.path || `external-${menu.id}`, // 如果没有path，使用id生成
            name: menu.name || `external-${menu.id}`, // 如果没有name，使用id生成
            props: true,
            meta: {
              title: menu.title,
              keepAlive: menu.keepAlive !== 0,
              isMenu: true,
              isShow: menu.visible !== 1,
              isExternal: false,
              icon: menu.icon,
            },
            children: [],
          }
          if (menu.component) {
            route.component = modules[`/src/views/${menu.component}.vue`]
          } else {
            if (menu.redirect) {
              route.meta!.link = menu.redirect
              route.meta!.isExternal = true
            }
            route.redirect = 'no-found'
          }
          if (menu.children && menu.children.length > 0) {
            route.children = buildRoutes(menu.children)
          }
          result.push(route)
        })
        return result
      }

      // 外层调用：生成并赋值
      this.dynamicRoutes = buildRoutes(menus)
    },
    /**
     * 重置路由
     */
    resetRouter() {
      // 递归删除路由的方法
      const removeRoutes = (routes: RouteRecordRaw[]) => {
        routes.forEach((route) => {
          if (route.name && typeof route.name === 'string') {
            router.removeRoute(route.name)
          }
          if (route.children && route.children.length > 0) {
            removeRoutes(route.children)
          }
        })
      }
      // 删除当前记录的动态路由
      removeRoutes(this.dynamicRoutes)
      // 清空动态路由记录
      this.dynamicRoutes = []
      this.menus = []
    },
    /**
     * 生成菜单
     */
    generateMenus() {
      this.menus = [
        ...buildMenus(constantRouter),
        ...buildMenus(this.dynamicRoutes),
      ] as SafeMenuOption[]
    },
  },
})
