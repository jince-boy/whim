import { defineStore } from 'pinia'
import { joinPaths } from '@/utils/menu'
import { constantRouter } from '@/router'
import type { TabState } from '@/stores/type.ts'
import type { RouteRecordRaw } from 'vue-router'
import { useKeepAliveStore } from './keepAlive'

export const useTabStore = defineStore('tab', {
  state: (): { tabs: TabState[] } => {
    // 显式声明返回类型
    const layoutRoute = constantRouter.find((route) => route.name === 'Layout') as RouteRecordRaw
    const homeRoute = layoutRoute?.children?.find(
      (child) => child.name === 'home',
    ) as RouteRecordRaw
    // 映射转换并添加closeable属性
    const tabs = [
      {
        title: homeRoute?.meta?.title,
        path: joinPaths(layoutRoute.path, homeRoute.path),
        icon: homeRoute?.meta?.icon,
        name: homeRoute?.name,
        closeable: false,
        focused: true,
      },
    ] as TabState[]
    return {
      tabs,
    }
  },
  getters: {
    // 获取所有tabs的getter
    getAllTabs: (state) => state.tabs,
  },
  actions: {
    /** 更新标签的最新地址和展示信息，并保留首页不可关闭的约定。 */
    addTab(tab: TabState) {
      // 接收不包含focused属性的对象
      // 检查是否已存在相同name的tab
      const existTab = this.tabs.find((item) => item.name === tab.name)
      if (!existTab) {
        // 添加新标签页，设置focused为true，其他标签页设置为false
        this.tabs = this.tabs.map((t) => ({ ...t, focused: false }))
        this.tabs.push({
          ...tab,
          focused: true, // 新添加的标签页设置为聚焦
        })
      } else {
        // 同一路由共享一个标签，参数变化时同步最新地址。
        this.tabs = this.tabs.map((t) => ({
          ...t,
          ...(t.name === tab.name ? { ...tab, closeable: t.closeable } : {}),
          focused: t.name === tab.name,
        }))
      }
    },
    /** 关闭指定标签并清理缓存，关闭当前页时聚焦相邻标签。 */
    closeTab(tabName: string) {
      const index = this.tabs.findIndex((t) => t.name === tabName)
      if (index === -1 || !this.tabs[index].closeable) return

      const closingFocused = this.tabs[index].focused
      this.tabs.splice(index, 1)

      if (closingFocused && this.tabs.length) {
        // 优先聚焦前一个tab，没有则聚焦第一个
        const newFocusIndex = index - 1 >= 0 ? index - 1 : 0
        this.tabs.forEach((t, i) => (t.focused = i === newFocusIndex))
      }
      useKeepAliveStore().retainKeepAlive(this.tabs.map((tab) => tab.name))
    },
    /** 关闭左侧可关闭标签，同时保留当前页并清理已关闭页面缓存。 */
    closeLeftTabs(tabName: string) {
      const idx = this.tabs.findIndex((t) => t.name === tabName)
      if (idx <= 0) return
      const focusedName = this.tabs.find((t) => t.focused)?.name
      this.tabs = this.tabs.filter((t, i) => i >= idx || !t.closeable || t.name === focusedName)
      useKeepAliveStore().retainKeepAlive(this.tabs.map((tab) => tab.name))
    },
    /** 关闭右侧可关闭标签，同时保留当前页并清理已关闭页面缓存。 */
    closeRightTabs(tabName: string) {
      const idx = this.tabs.findIndex((t) => t.name === tabName)
      if (idx === -1 || idx === this.tabs.length - 1) return
      const focusedName = this.tabs.find((t) => t.focused)?.name
      this.tabs = this.tabs.filter((t, i) => i <= idx || !t.closeable || t.name === focusedName)
      useKeepAliveStore().retainKeepAlive(this.tabs.map((tab) => tab.name))
    },
    /** 保留目标、当前和固定标签，清理其他页面及其缓存。 */
    closeOtherTabs(tabName: string) {
      const focusedName = this.tabs.find((t) => t.focused)?.name
      this.tabs = this.tabs.filter(
        (t) => !t.closeable || t.name === tabName || t.name === focusedName,
      )
      useKeepAliveStore().retainKeepAlive(this.tabs.map((tab) => tab.name))
    },

    /** 关闭全部可关闭标签并清理对应缓存。 */
    closeAllTabsExceptHome() {
      const homeTab = this.tabs.find((t) => !t.closeable)
      if (!homeTab) return
      this.tabs = [{ ...homeTab, focused: true }]
      useKeepAliveStore().retainKeepAlive(this.tabs.map((tab) => tab.name))
    },
  },
})
