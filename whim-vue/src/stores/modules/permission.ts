import { defineStore } from 'pinia'
import { computed, ref, shallowRef } from 'vue'
import type { RouteLocationRaw } from 'vue-router'
import router from '@/router'
import type { UserMenu } from './permission/types'
import { fetchUserMenus } from './permission/api'
import { useAuthStore } from './auth'
import { useTabStore } from './tab'
import { buildNavigation, type MenuRoute } from '@/router/dynamicRoutes'
import { buildNavigationMenus, type SafeMenuOption } from '@/utils/menu'

/** 菜单请求、路由注册和首页入口由同一实例管理；退出后旧请求不能恢复旧权限。 */
export const usePermissionStore = defineStore('permission', () => {
  const sourceMenus = shallowRef<UserMenu[]>([])
  const dynamicRoutes = shallowRef<MenuRoute[]>([])
  const menus = shallowRef<SafeMenuOption[]>([])
  const initialized = ref(false)
  const entryTarget = shallowRef<RouteLocationRaw>()
  // 首页、品牌入口和固定标签共用当前授权菜单中的第一个可访问页面。
  const homePath = computed(() => router.resolve(entryTarget.value ?? '/noAccess').fullPath)
  const homeName = computed(() => router.resolve(entryTarget.value ?? '/noAccess').name)
  const getMenus = computed(() => menus.value)
  let revision = 0
  let pending: Promise<void> | undefined

  /** 校验新导航后只替换有变化的路由；请求失败时继续使用已有导航。 */
  async function loadMenus(refresh = false): Promise<void> {
    if (initialized.value && !refresh) return
    if (pending) return pending
    const version = revision
    const token = useAuthStore().getToken
    const request = (async () => {
      const response = await fetchUserMenus()
      if (version !== revision || token !== useAuthStore().getToken) return
      if (response.code !== 200) throw new Error(response.message || '菜单获取失败')
      const previousRoutes = dynamicRoutes.value
      const navigation = buildNavigation(response.data, previousRoutes)
      const previousByName = new Map(previousRoutes.map((route) => [route.name, route]))
      const nextByName = new Map(navigation.routes.map((route) => [route.name, route]))
      const staticRoutes = router
        .getRoutes()
        .filter((route) => typeof route.name !== 'string' || !previousByName.has(route.name))
      for (const route of navigation.routes) {
        if (staticRoutes.some((item) => item.name === route.name || item.path === route.path))
          throw new Error('菜单与系统路由冲突，请检查菜单配置')
      }
      const nextMenus = buildNavigationMenus(navigation.tree)
      const removedRoutes = previousRoutes.filter((route) => nextByName.get(route.name) !== route)
      const addedRoutes = navigation.routes.filter(
        (route) => previousByName.get(route.name) !== route,
      )
      const installedRoutes: string[] = []
      for (const route of removedRoutes) router.removeRoute(route.name)
      try {
        for (const route of addedRoutes) {
          router.addRoute('Layout', route)
          installedRoutes.push(route.name)
        }
      } catch (error) {
        for (const name of installedRoutes) router.removeRoute(name)
        for (const route of removedRoutes) router.addRoute('Layout', route)
        throw error
      }
      // 仅清理组件已替换、已失去访问权或已关闭缓存的页面实例。
      const invalidatedCache = new Set<string>()
      for (const route of removedRoutes) {
        const next = nextByName.get(route.name)
        if (!next?.meta?.keepAlive || next.component !== route.component)
          invalidatedCache.add(route.name)
      }
      dynamicRoutes.value = navigation.routes
      sourceMenus.value = response.data
      menus.value = nextMenus
      entryTarget.value = navigation.entryTarget
      initialized.value = true
      useTabStore().syncRoutes(invalidatedCache)
    })()
    pending = request
    try {
      await request
    } finally {
      if (pending === request) pending = undefined
    }
  }

  /** 账号切换时同时使未完成请求失效，移除本实例注册的路由和首页。 */
  function resetRouter(): void {
    revision++
    pending = undefined
    for (const route of dynamicRoutes.value) router.removeRoute(route.name)
    sourceMenus.value = []
    dynamicRoutes.value = []
    menus.value = []
    entryTarget.value = undefined
    initialized.value = false
  }

  /** 当前匹配失效时才导航；已移除页面回到当前用户的入口。 */
  async function reloadMenus(): Promise<void> {
    const version = ++revision
    pending = undefined
    await loadMenus(true)
    if (version !== revision || !initialized.value) return
    const current = router.currentRoute.value
    const matched = router.resolve(current.fullPath)
    const target =
      matched.name === 'NotFound' || matched.name === 'NoAccess' ? homePath.value : current.fullPath
    if (target !== current.fullPath || matched.matched.at(-1) !== current.matched.at(-1))
      await router.replace(target)
  }

  return {
    sourceMenus,
    dynamicRoutes,
    menus,
    initialized,
    getMenus,
    homePath,
    homeName,
    loadMenus,
    resetRouter,
    reloadMenus,
  }
})
