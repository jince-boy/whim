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
    addKeepAlive(component: string) {
      this.keepAliveSet.add(component)
    },
    removeKeepAlive(component: string) {
      this.keepAliveSet.delete(component)
    },
    clearKeepAlive() {
      this.keepAliveSet.clear()
    },
  },
})
