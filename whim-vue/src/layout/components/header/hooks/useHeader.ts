import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, type DropdownOption } from 'naive-ui'
import screenfull from 'screenfull'
import { usePermissionStore } from '@/stores/modules/permission'
import type { SafeMenuOption } from '@/utils/menu'

/** 查找当前菜单及其父级，保持与侧栏相同的层级。 */
function findMenuPath(menus: SafeMenuOption[], key: string): SafeMenuOption[] {
  for (const menu of menus) {
    if (menu.key === key) return [menu]
    if (menu.children) {
      const childPath = findMenuPath(menu.children, key)
      if (childPath.length) return [menu, ...childPath]
    }
  }
  return []
}

/** 排除隐藏菜单，避免下拉菜单的键盘选择落到不可见项。 */
function getVisibleMenus(menus: SafeMenuOption[]): SafeMenuOption[] {
  return menus
    .filter((menu) => menu.show !== false)
    .map((menu) => ({
      ...menu,
      children: menu.children ? getVisibleMenus(menu.children) : undefined,
    }))
}

/** 管理 Header 的面包屑、弹窗和全屏状态。 */
export function useHeader() {
  const route = useRoute()
  const router = useRouter()
  const permissionStore = usePermissionStore()
  const message = useMessage()
  const searchShowModal = ref(false)
  const themeShowModal = ref(false)
  const fullscreenEnabled = screenfull.isEnabled
  const isFullscreen = ref(fullscreenEnabled && screenfull.isFullscreen)

  // 使用菜单构建时相同的 key 规则，兼容未命名路由。
  const breadcrumbs = computed<SafeMenuOption[]>(() =>
    findMenuPath(
      permissionStore.menus,
      typeof route.name === 'string' ? route.name : route.path,
    ).map((menu) => ({
      ...menu,
      children: menu.children ? getVisibleMenus(menu.children) : undefined,
    })),
  )

  /** 通过下拉菜单选中项导航，支持整行点击和键盘选择。 */
  async function handleMenuSelect(_key: string | number, option: DropdownOption): Promise<void> {
    const path = option.path
    if (typeof path !== 'string' || !path) return
    if (option.isExternal) {
      window.open(path, '_blank', 'noopener,noreferrer')
    } else {
      await router.push(path)
    }
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
    breadcrumbs,
    searchShowModal,
    themeShowModal,
    fullscreenEnabled,
    isFullscreen,
    handleMenuSelect,
    toggleFullscreen,
  }
}
