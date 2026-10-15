import { useKeepAliveStore } from '@/stores/modules/keepAlive.ts'
import { useThemeStore } from '@/stores/modules/theme'
import { nextTick, ref, watch, useTemplateRef } from 'vue'
import { useRoute } from 'vue-router'

/** 管理菜单页面缓存和刷新，不把路由业务逻辑放在布局模板中。 */
export function useAppMain() {
  const appMainRef = useTemplateRef<HTMLElement>('appMainRef')

  const pageVisible = ref(true)
  const refreshing = ref(false)
  const route = useRoute()

  const keepAliveStore = useKeepAliveStore()
  const themeStore = useThemeStore()

  // 关闭全局缓存时清理旧实例；开启及路由变化时按菜单配置缓存当前页面。
  watch(
    [() => themeStore.getCacheTabs, () => route.name, () => route.meta.keepAlive],
    ([enabled, name, keepAlive]) => {
      if (!enabled) {
        keepAliveStore.clearKeepAlive()
      } else if (typeof name === 'string') {
        if (keepAlive) keepAliveStore.addKeepAlive(name)
        else keepAliveStore.removeKeepAlive(name)
      }
    },
    { immediate: true },
  )

  /** 先清理当前实例，再重新挂载；其他标签的缓存保持不变。 */
  async function refresh(): Promise<void> {
    if (refreshing.value) return
    refreshing.value = true
    const name = typeof route.name === 'string' ? route.name : undefined
    try {
      if (name) keepAliveStore.removeKeepAlive(name)
      // 先让 KeepAlive 将当前实例标记为不缓存，再卸载，避免留下旧实例。
      await nextTick()
      pageVisible.value = false
      await nextTick()
      if (themeStore.getCacheTabs && name && route.name === name && route.meta.keepAlive) {
        keepAliveStore.addKeepAlive(name)
      }
      pageVisible.value = true
      await nextTick()
    } finally {
      refreshing.value = false
    }
  }

  return { appMainRef, pageVisible, refreshing, themeStore, keepAliveStore, refresh }
}
