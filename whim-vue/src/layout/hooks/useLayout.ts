import { computed, ref, useTemplateRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import screenfull from 'screenfull'
import AppMain from '@/layout/components/appMain/index.vue'
import { useThemeStore } from '@/stores/modules/theme'
import { usePermissionStore } from '@/stores/modules/permission'
import { findMenuPath, getVisibleMenus, type SafeMenuOption } from '@/utils/menu'
/** 统一布局切换、当前菜单定位和页面刷新，标签由路由守卫维护。 */
export function useLayout() {
  const themeStore = useThemeStore()
  const permissionStore = usePermissionStore()
  const route = useRoute()
  const message = useMessage()
  const appMainRef = useTemplateRef<InstanceType<typeof AppMain>>('appMainRef')
  const sidebarExpandedWidth = 240
  const sidebarCollapsedWidth = 64
  const collapsed = ref(false)
  const activeRootKey = ref<string | null>(null)
  const layoutMode = computed(() => themeStore.config.layoutMode)
  const selectedKey = computed(
    () => route.meta.menuKey ?? (typeof route.name === 'string' ? route.name : route.path),
  )
  const visibleMenus = computed(() => getVisibleMenus(permissionStore.menus))
  const activeRoot = computed(() =>
    visibleMenus.value.find((menu) => menu.key === activeRootKey.value),
  )
  const sidebarMenus = computed<SafeMenuOption[]>(() =>
    layoutMode.value === 'mixed' ? (activeRoot.value?.children ?? []) : visibleMenus.value,
  )
  const showSidebar = computed(
    () =>
      layoutMode.value === 'side' ||
      (layoutMode.value === 'mixed' && sidebarMenus.value.length > 0),
  )
  const currentSidebarWidth = computed(() =>
    collapsed.value ? sidebarCollapsedWidth : sidebarExpandedWidth,
  )
  const sidebarWidth = computed(() =>
    showSidebar.value ? `${currentSidebarWidth.value}px` : '0px',
  )
  const topMenus = computed<SafeMenuOption[]>(() =>
    layoutMode.value === 'mixed'
      ? visibleMenus.value.map((menu) => ({
          ...menu,
          // 一级目录在顶部作为分组入口，叶子菜单保留原有导航链接。
          label: menu.children?.length ? menu.name : menu.label,
          children: undefined,
        }))
      : visibleMenus.value,
  )

  // 路由、权限菜单和布局变化时定位所属一级目录，兼容隐藏详情页及刷新直达。
  watch(
    [() => route.fullPath, () => permissionStore.menus, layoutMode],
    () => {
      const routeKeys = [
        selectedKey.value,
        ...route.matched
          .slice()
          .reverse()
          .map((record) => (typeof record.name === 'string' ? record.name : record.path)),
      ]
      let root: SafeMenuOption | undefined
      for (const key of routeKeys) {
        root = findMenuPath(permissionStore.menus, key)[0]
        if (root) break
      }
      const visibleRoot =
        visibleMenus.value.find((menu) => menu.key === root?.key) ?? visibleMenus.value[0]
      activeRootKey.value = typeof visibleRoot?.key === 'string' ? visibleRoot.key : null
    },
    { immediate: true },
  )

  /** 混合模式切换一级目录，叶子菜单继续由菜单链接导航。 */
  function handleTopMenuSelect(key: string): void {
    if (layoutMode.value !== 'mixed') return
    const menu = visibleMenus.value.find((item) => item.key === key)
    if (menu && !menu.isExternal) activeRootKey.value = key
  }

  /** 切换主内容区全屏，并处理浏览器拒绝请求的情况。 */
  async function toggleFullScreen(): Promise<void> {
    const element = appMainRef.value?.appMainRef
    if (!element || !screenfull.isEnabled) return
    try {
      await screenfull.toggle(element)
    } catch {
      message.error('切换内容全屏失败')
    }
  }

  /** 刷新当前页面组件。 */
  function refreshAppMain(): void {
    appMainRef.value?.refresh()
  }
  return {
    themeStore,
    homePath: computed(() => permissionStore.homePath),
    layoutMode,
    collapsed,
    sidebarWidth,
    showSidebar,
    sidebarExpandedWidth,
    sidebarCollapsedWidth,
    currentSidebarWidth,
    sidebarMenus,
    activeRootKey,
    selectedKey,
    topMenus,
    handleTopMenuSelect,
    toggleFullScreen,
    refreshAppMain,
  }
}
