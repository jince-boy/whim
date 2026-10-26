import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'
import type { DropdownOption } from 'naive-ui'
import { message } from '@/plugins'
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
  const searchShowModal = ref(false)
  const themeShowModal = ref(false)
  const fullscreenEnabled = screenfull.isEnabled
  const isFullscreen = ref(fullscreenEnabled && screenfull.isFullscreen)

  // 路由元数据保留隐藏页面与目录祖先，静态个人信息页也使用同一展示结构。
  const breadcrumbs = computed<SafeMenuOption[]>(() =>
    (route.meta.breadcrumbs ?? []).map((item) => {
      const menu = findMenuPath(permissionStore.menus, item.name ?? '').at(-1)
      return {
        ...menu,
        key: item.name ?? item.path ?? item.title,
        name: item.title,
        children: menu?.children ? getVisibleMenus(menu.children) : undefined,
      }
    }),
  )

  /** 菜单、搜索和面包屑共用解析后的目标，保留默认查询参数。 */
  async function handleMenuSelect(_key: string | number, option: DropdownOption): Promise<void> {
    if (option.disabled) return
    if (option.isExternal && typeof option.path === 'string')
      window.open(option.path, '_blank', 'noopener,noreferrer')
    else if (option.target) await router.push(option.target as RouteLocationRaw)
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
