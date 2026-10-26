import { watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTabStore } from '@/stores/modules/tab'

/** 保留旧字典数据链接的入口，统一进入类型目录工作区。 */
export function useDictDataRedirect() {
  const route = useRoute()
  const router = useRouter()
  const tabStore = useTabStore()
  const legacyRouteName = route.name
  watch(
    () => route.fullPath,
    async () => {
      if (route.name !== legacyRouteName) return
      const failure = await router.replace({
        path: '/system/dictType',
        query: { ...route.query, type: String(route.params.type || '') || undefined },
      })
      if (!failure && route.path === '/system/dictType' && typeof legacyRouteName === 'string')
        await tabStore.closeTab(legacyRouteName)
    },
    { immediate: true },
  )
}
