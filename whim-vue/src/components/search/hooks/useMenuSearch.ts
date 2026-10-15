import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useIcon } from '@/components/icon/useIcon'
import { usePermissionStore } from '@/stores/modules/permission'
import { getVisibleMenus, type SafeMenuOption } from '@/utils/menu'
/** 搜索只包含当前可见且可访问的内部菜单，授权变化时立即刷新结果。 */
export function useMenuSearch() {
  const { createIcon } = useIcon()

  const permissionStore = usePermissionStore()
  const router = useRouter()
  const value = ref('')
  const searchResults = ref<{ text: string; path?: string }[]>([])

  const handleInput = () => {
    const keyword = value.value.trim()
    if (!keyword) {
      searchResults.value = []
      return
    }
    const results: { text: string; path?: string }[] = []
    const dfs = (node: SafeMenuOption, pathStack: string[]) => {
      if (node.disabled || node.isExternal) return
      const currentName = node.name || ''
      const currentPath =
        node.target && !node.directory ? router.resolve(node.target).fullPath : undefined
      const newPathStack = [...pathStack, currentName]
      // 只记录有 path 的匹配项
      if (currentName.includes(keyword) && currentPath) {
        results.push({
          text: newPathStack.join(' > '),
          path: currentPath,
        })
      }
      // 递归子节点
      if (node.children && node.children.length) {
        for (const child of node.children) {
          dfs(child, newPathStack)
        }
      }
    }
    for (const menu of getVisibleMenus(permissionStore.menus)) {
      dfs(menu, [])
    }
    searchResults.value = results
  }

  watch([value, () => permissionStore.menus], handleInput)
  return { createIcon, value, searchResults }
}
