import { defineStore } from 'pinia'
import { markRaw } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import router, { constantRouter } from '@/router'
import type { MenuItem } from '@/views/system/menu/hooks/types'
import { fetchUserMenus } from './permission/api'
import { useAuthStore } from './auth'
import { buildNavigationRoutes } from '@/utils/navigation'
import { buildMenus, buildUserMenus, type SafeMenuOption } from '@/utils/menu'
let pendingMenus: { token: string | null; request: Promise<void> } | undefined

export const usePermissionStore = defineStore('permission', {
  /** 菜单树是导航唯一来源，加载完成前不放行受保护路由。 */
  state: () => ({
    sourceMenus: [] as MenuItem[],
    dynamicRoutes: [] as RouteRecordRaw[],
    menus: [] as SafeMenuOption[],
    initialized: false,
  }),
  getters: {
    /** 布局的统一菜单入口。 */
    getMenus: (state) => state.menus,
  },
  actions: {
    /** 合并并发初始化，并在注册成功后标记完成。 */
    async loadMenus() {
      if (this.initialized) return
      const token = useAuthStore().getToken
      if (pendingMenus?.token === token) return pendingMenus.request
      const request = (async () => {
        const res = await fetchUserMenus()
        if (token !== useAuthStore().getToken) throw new Error('登录会话已变更')
        if (res.code !== 200) {
          window.$message?.error(res.message)
          throw new Error(res.message || '菜单获取失败')
        }
        const routes = buildNavigationRoutes(res.data)
        this.resetRouter()
        for (const route of routes) {
          if (router.hasRoute(route.name!)) throw new Error('菜单路由名称与现有路由冲突')
          router.addRoute('Layout', route)
          this.dynamicRoutes.push(markRaw(route))
        }
        this.sourceMenus = res.data
        this.menus = [...buildMenus(constantRouter), ...buildUserMenus(res.data)]
        this.initialized = true
      })()
      pendingMenus = { token, request }
      try {
        await request
      } finally {
        if (pendingMenus?.request === request) pendingMenus = undefined
      }
    },
    /** 清理本次注册的所有路由，避免账号切换遗留权限入口。 */
    resetRouter() {
      for (const route of this.dynamicRoutes) if (route.name) router.removeRoute(route.name)
      this.sourceMenus = []
      this.dynamicRoutes = []
      this.menus = []
      this.initialized = false
    },
    /** 保存后重载导航、移除失效标签并重新匹配当前页。 */
    async reloadMenus() {
      this.initialized = false
      await this.loadMenus()
      const { useTabStore } = await import('./tab')
      useTabStore().retainRoutes()
      const current = router.currentRoute.value.fullPath
      await router.replace(router.resolve(current).name === 'NotFound' ? '/index' : current)
    },
  },
})
