import { defineStore } from 'pinia'
import { computed, ref, shallowRef } from 'vue'
import type { RouteLocationRaw, RouteRecordRaw } from 'vue-router'
import router from '@/router'
import type { MenuItem } from '@/views/system/menu/hooks/types'
import { fetchUserMenus } from './permission/api'
import { useAuthStore } from './auth'
import { useTabStore } from './tab'
import { buildNavigation } from '@/utils/navigation'
import { buildNavigationMenus, type SafeMenuOption } from '@/utils/menu'

/** 菜单请求、路由注册和首页入口由同一实例管理；退出后旧请求不能恢复旧权限。 */
export const usePermissionStore = defineStore('permission', () => {
  const sourceMenus = shallowRef<MenuItem[]>([])
  const dynamicRoutes = shallowRef<RouteRecordRaw[]>([])
  const menus = shallowRef<SafeMenuOption[]>([])
  const initialized = ref(false)
  const entryTarget = shallowRef<RouteLocationRaw>()
  const homePath = computed(() => router.resolve(entryTarget.value ?? '/noAccess').fullPath)
  const homeName = computed(() =>
    entryTarget.value ? router.resolve(entryTarget.value).name : undefined,
  )
  const getMenus = computed(() => menus.value)
  let revision = 0
  let removers: (() => void)[] = []
  let pending: Promise<void> | undefined

  /** 先完整解析并检查冲突，再替换当前路由；请求失败时保留原有导航。 */
  async function loadMenus(): Promise<void> {
    if (initialized.value) return
    if (pending) return pending
    const version = revision
    const token = useAuthStore().getToken
    const request = (async () => {
      const response = await fetchUserMenus()
      if (version !== revision || token !== useAuthStore().getToken) return
      if (response.code !== 200) throw new Error(response.message || '菜单获取失败')
      const navigation = buildNavigation(response.data)
      const previousNames = new Set(dynamicRoutes.value.map((route) => route.name))
      const staticRoutes = router.getRoutes().filter((route) => !previousNames.has(route.name))
      for (const route of navigation.routes) {
        if (staticRoutes.some((item) => item.name === route.name || item.path === route.path))
          throw new Error('菜单与系统路由冲突，请检查菜单配置')
      }
      const nextMenus = buildNavigationMenus(navigation.tree)
      const previousRoutes = dynamicRoutes.value
      removers.forEach((remove) => remove())
      removers = []
      try {
        for (const route of navigation.routes) removers.push(router.addRoute('Layout', route))
      } catch (error) {
        removers.forEach((remove) => remove())
        removers = previousRoutes.map((route) => router.addRoute('Layout', route))
        throw error
      }
      dynamicRoutes.value = navigation.routes
      sourceMenus.value = response.data
      menus.value = nextMenus
      entryTarget.value = navigation.entryTarget
      initialized.value = true
      useTabStore().syncRoutes()
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
    removers.forEach((remove) => remove())
    removers = []
    sourceMenus.value = []
    dynamicRoutes.value = []
    menus.value = []
    entryTarget.value = undefined
    initialized.value = false
  }

  /** 保存后重新匹配当前路由；已移除页面回到当前用户的入口。 */
  async function reloadMenus(): Promise<void> {
    const wasInitialized = initialized.value
    revision++
    const version = revision
    pending = undefined
    initialized.value = false
    try {
      await loadMenus()
    } catch (error) {
      if (version === revision) initialized.value = wasInitialized
      throw error
    }
    if (!initialized.value) return
    const current = router.currentRoute.value.fullPath
    const name = router.resolve(current).name
    await router.replace(name === 'NotFound' || name === 'NoAccess' ? homePath.value : current)
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
