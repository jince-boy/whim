import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, type DropdownOption } from 'naive-ui'
import screenfull from 'screenfull'
import { useIcon } from '@/components/icon/useIcon'
import { usePermissionStore } from '@/stores/modules/permission'
import { findMenuPath, getVisibleMenus, type SafeMenuOption } from '@/utils/menu'
/** 面包屑使用已解析路由的菜单祖先，工具栏状态独立于导航权限。 */
export function useHeader() {
  const { createIcon } = useIcon()
  const route = useRoute()
  const router = useRouter()
  const permissionStore = usePermissionStore()
  const message = useMessage()
  const searchShowModal = ref(false)
  const themeShowModal = ref(false)
  const fullscreenEnabled = screenfull.isEnabled
  const isFullscreen = ref(fullscreenEnabled && screenfull.isFullscreen)

  // 完整菜单树包含隐藏页面；沿用原来的祖先面包屑与子菜单下拉结构。
  const breadcrumbs = computed<SafeMenuOption[]>(() =>
    findMenuPath(
      permissionStore.menus,
      typeof route.name === 'string' ? route.name : route.path,
    ).map((menu) => ({
      ...menu,
      children: menu.children ? getVisibleMenus(menu.children) : undefined,
    })),
  )

  /** 沿用面包屑下拉菜单选择后的导航行为。 */
  async function handleMenuSelect(_key: string | number, option: DropdownOption): Promise<void> {
    if (typeof option.path !== 'string' || !option.path) return
    if (option.isExternal) window.open(option.path, '_blank', 'noopener,noreferrer')
    else await router.push(option.path)
  }

  /** 同步浏览器全屏状态，包括按 Esc 退出和主内容区全屏。 */
  function syncFullscreen(): void {
    isFullscreen.value = screenfull.isFullscreen
  }

  /** 切换页面全屏，失败时给出提示。 */
  async function toggleFullscreen(): Promise<void> {
    if (!fullscreenEnabled) return
    try {
      await screenfull.toggle()
    } catch {
      message.error('切换全屏失败')
    }
  }

  // 组件挂载后监听全屏变化，卸载时移除监听。
  onMounted(() => {
    if (fullscreenEnabled) {
      screenfull.on('change', syncFullscreen)
      syncFullscreen()
    }
  })
  onBeforeUnmount(() => {
    if (fullscreenEnabled) screenfull.off('change', syncFullscreen)
  })
  return {
    createIcon,
    breadcrumbs,
    handleMenuSelect,
    route,
    searchShowModal,
    themeShowModal,
    fullscreenEnabled,
    isFullscreen,
    toggleFullscreen,
  }
}
