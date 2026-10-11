export const useKeepAliveStore = defineStore('keepAlive', {
  state: () => ({
    keepAliveSet: new Set<string>(), // 使用 Set 自动去重
  }),
  getters: {
    keepAliveComponents(state): string[] {
      return Array.from(state.keepAliveSet) // 对外转换成数组
    },
  },
  actions: {
    /** 将页面加入缓存名单。 */
    addKeepAlive(component: string) {
      this.keepAliveSet.add(component)
    },
    /** 移除页面缓存，由 KeepAlive 清理对应实例。 */
    removeKeepAlive(component: string) {
      this.keepAliveSet.delete(component)
    },
    /** 仅保留仍有标签的页面缓存，支持批量关闭。 */
    retainKeepAlive(components: string[]) {
      const retained = new Set(components)
      this.keepAliveSet = new Set([...this.keepAliveSet].filter((name) => retained.has(name)))
    },
    /** 清理全部页面缓存。 */
    clearKeepAlive() {
      this.keepAliveSet.clear()
    },
  },
})
