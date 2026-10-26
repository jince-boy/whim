import { defineStore } from 'pinia'
import router from '@/router'
import type { RouteLocationNormalized, RouteLocationResolved } from 'vue-router'
import type { TabState } from '@/stores/type'
import { useKeepAliveStore } from './keepAlive'
import { usePermissionStore } from './permission'

/** 统一从成功匹配的路由生成标签，固定入口也来自当前授权导航。 */
function routeTab(route: Pick<RouteLocationResolved, 'name' | 'fullPath' | 'meta'>): TabState {
  return {
    name: String(route.name),
    title: route.meta.title || '页面',
    path: route.fullPath,
    icon: route.meta.icon || '',
    closeable: route.name !== usePermissionStore().homeName,
  }
}

/** 标签只保存访问记录，选中状态由真实路由派生。 */
export const useTabStore = defineStore('tab', {
  state: () => ({ tabs: [] as TabState[], closing: false }),
  getters: {
    /** 浏览器前进、后退和菜单跳转始终使用同一个激活状态。 */
    getAllTabs: (state) =>
      state.tabs.map((tab) => ({ ...tab, focused: router.currentRoute.value.name === tab.name })),
  },
  actions: {
    /** 退出或切换账号时清空访问记录和关闭状态。 */
    clearTabs() {
      this.tabs = []
      this.closing = false
    },
    /** 同一路由只保留一个标签，参数变化时记录最新可恢复地址。 */
    visitRoute(route: RouteLocationNormalized) {
      if (!route.meta.isTab || typeof route.name !== 'string') return
      const tab = routeTab(route)
      const index = this.tabs.findIndex((item) => item.name === tab.name)
      if (index < 0) this.tabs.push(tab)
      else this.tabs[index] = tab
    },
    /** 先完成目标导航，再删除标签；中止或重定向不应误删当前页面。 */
    async closeTabs(names: string[], preferredName?: string) {
      if (this.closing) return
      this.closing = true
      try {
        const navigation = usePermissionStore()
        const catalog = navigation.dynamicRoutes
        const current = router.currentRoute.value.name
        const index = this.tabs.findIndex((tab) => tab.name === current)
        const retained = this.tabs.filter((tab) => !tab.closeable || !names.includes(tab.name))
        const target =
          retained.find((tab) => tab.name === current) ??
          retained.find((tab) => tab.name === preferredName) ??
          retained[Math.max(0, Math.min(index - 1, retained.length - 1))]
        if (!retained.some((tab) => tab.name === current)) {
          const path = target?.path ?? navigation.homePath
          const failure = await router.push(path)
          if (failure || router.currentRoute.value.fullPath !== router.resolve(path).fullPath)
            return
        }
        if (catalog !== navigation.dynamicRoutes) return
        this.tabs = retained
        useKeepAliveStore().retainKeepAlive(retained.map((tab) => tab.name))
      } finally {
        this.closing = false
      }
    },
    /** 关闭单个可关闭标签。 */
    closeTab(name: string) {
      return this.closeTabs([name])
    },
    /** 关闭目标左侧标签，必要时切换到目标标签。 */
    closeLeftTabs(name: string) {
      const index = this.tabs.findIndex((tab) => tab.name === name)
      return this.closeTabs(index < 0 ? [] : this.tabs.slice(0, index).map((tab) => tab.name), name)
    },
    /** 关闭目标右侧标签。 */
    closeRightTabs(name: string) {
      const index = this.tabs.findIndex((tab) => tab.name === name)
      return this.closeTabs(
        index < 0 ? [] : this.tabs.slice(index + 1).map((tab) => tab.name),
        name,
      )
    },
    /** 保留目标及当前导航入口。 */
    closeOtherTabs(name: string) {
      return this.closeTabs(
        this.tabs.filter((tab) => tab.name !== name).map((tab) => tab.name),
        name,
      )
    },
    /** 关闭全部可关闭标签，返回当前用户的入口页面。 */
    closeAllTabsExceptHome() {
      return this.closeTabs(this.tabs.map((tab) => tab.name))
    },
    /** 重新授权后剔除失效页面，更新标题、地址和固定标签，并销毁旧路由缓存。 */
    syncRoutes() {
      const navigation = usePermissionStore()
      const entry = router.resolve(navigation.homePath)
      const retained = this.tabs.flatMap((tab) => {
        if (!router.hasRoute(tab.name)) return []
        const route = router.resolve(tab.path)
        return route.name === tab.name && route.meta.isTab ? [routeTab(route)] : []
      })
      const home = entry.meta.isTab ? routeTab(entry) : undefined
      this.tabs = home ? [home, ...retained.filter((tab) => tab.name !== home.name)] : retained
      useKeepAliveStore().clearKeepAlive()
    },
  },
})
