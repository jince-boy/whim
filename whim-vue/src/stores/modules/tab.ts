import { defineStore } from 'pinia'
import { joinPaths } from '@/utils/menu'
import { constantRouter } from '@/router'
import type { TabState } from '@/stores/type.ts'
import type { RouteRecordRaw } from 'vue-router'

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
        // 如果标签页已存在，只需将其设置为聚焦
        this.tabs = this.tabs.map((t) => ({
          ...t,
          focused: t.name === tab.name,
        }))
      }
    },
    closeTab(tabName: string) {
      const index = this.tabs.findIndex((t) => t.name === tabName)
      if (index === -1) return

      const closingFocused = this.tabs[index].focused
      this.tabs.splice(index, 1)

      if (closingFocused && this.tabs.length) {
        // 优先聚焦前一个tab，没有则聚焦第一个
        const newFocusIndex = index - 1 >= 0 ? index - 1 : 0
        this.tabs.forEach((t, i) => (t.focused = i === newFocusIndex))
      }
    },
    closeLeftTabs(tabName: string) {
      const idx = this.tabs.findIndex((t) => t.name === tabName)
      if (idx <= 0) return
      const focusedName = this.tabs.find((t) => t.focused)?.name
      this.tabs = this.tabs.filter(
        (t, i) => i >= idx || !t.closeable || t.name === focusedName
      )
    },
    closeRightTabs(tabName: string) {
      const idx = this.tabs.findIndex((t) => t.name === tabName)
      if (idx === -1 || idx === this.tabs.length - 1) return
      const focusedName = this.tabs.find((t) => t.focused)?.name
      this.tabs = this.tabs.filter(
        (t, i) => i <= idx || !t.closeable || t.name === focusedName
      )
    },
    closeOtherTabs(tabName: string) {
      const focusedName = this.tabs.find((t) => t.focused)?.name
      this.tabs = this.tabs.filter(
        (t) =>
          !t.closeable || t.name === tabName || t.name === focusedName
      )
    },

    closeAllTabsExceptHome() {
      const homeTab = this.tabs.find((t) => !t.closeable)
      if (!homeTab) return
      this.tabs = [{ ...homeTab, focused: true }]
    },
  },
})
