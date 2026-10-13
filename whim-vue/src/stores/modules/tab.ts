import { defineStore } from 'pinia'
import router from '@/router'
import type { RouteLocationNormalized } from 'vue-router'
import type { TabState } from '@/stores/type'
import { useKeepAliveStore } from './keepAlive'

const homeTab: TabState = {
  title: '首页',
  path: '/index',
  name: 'home',
  icon: 'smile',
  closeable: false,
}

/** 页面标签只保存访问记录，激活状态始终由真实路由派生。 */
export const useTabStore = defineStore('tab', {
  /** 首页固定，其他标签以路由名称标识，同一路由参数变化更新地址。 */
  state: () => ({ tabs: [{ ...homeTab }] as TabState[] }),
  getters: {
    /** 标签是否激活由路由计算，避免关闭或浏览器后退时状态漂移。 */
    getAllTabs: (state) =>
      state.tabs.map((tab) => ({ ...tab, focused: router.currentRoute.value.name === tab.name })),
  },
  actions: {
    /** 导航成功后记录页面最新标题、查询参数和可关闭性。 */
    visitRoute(route: RouteLocationNormalized) {
      const tab: TabState = {
        name: String(route.name),
        title: route.meta.title || '页面',
        path: route.fullPath,
        icon: route.meta.icon || '',
        closeable: route.name !== 'home',
      }
      const index = this.tabs.findIndex((item) => item.name === tab.name)
      if (index < 0) this.tabs.push(tab)
      else this.tabs[index] = tab
    },
    /** 移除标签前确定实际当前页面；关闭当前页才导航到相邻保留页面。 */
    async closeTabs(names: string[], preferredName?: string) {
      const current = router.currentRoute.value.name
      const index = this.tabs.findIndex((tab) => tab.name === current)
      const retained = this.tabs.filter((tab) => !tab.closeable || !names.includes(tab.name))
      const target =
        retained.find((tab) => tab.name === current) ??
        retained.find((tab) => tab.name === preferredName) ??
        retained[Math.max(0, Math.min(index - 1, retained.length - 1))] ??
        homeTab
      if (!retained.some((tab) => tab.name === current)) {
        await router.push(target.path)
        if (router.currentRoute.value.name !== target.name) return
      }
      this.tabs = retained
      useKeepAliveStore().retainKeepAlive(retained.map((tab) => tab.name))
    },
    /** 关闭一个可关闭标签。 */
    closeTab(name: string) {
      return this.closeTabs([name])
    },
    /** 关闭目标左侧标签；当前页被关闭时使用同一邻页规则。 */
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
    /** 关闭其他标签，保留目标和固定首页。 */
    closeOtherTabs(name: string) {
      return this.closeTabs(
        this.tabs.filter((tab) => tab.name !== name).map((tab) => tab.name),
        name,
      )
    },
    /** 关闭全部可关闭标签并回到首页。 */
    closeAllTabsExceptHome() {
      return this.closeTabs(this.tabs.map((tab) => tab.name))
    },
    /** 菜单变化后清理被撤销的路由标签，并同步仍可访问页面的标题。 */
    retainRoutes() {
      this.tabs = this.tabs
        .filter((tab) => router.hasRoute(tab.name))
        .map((tab) => {
          const route = router.resolve(tab.path)
          return { ...tab, title: route.meta.title || tab.title, icon: route.meta.icon || '' }
        })
      useKeepAliveStore().clearKeepAlive()
    },
  },
})
