import { computed, nextTick, ref, useTemplateRef, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { ScrollbarInst, DropdownOption } from 'naive-ui'
import { useThemeVars } from 'naive-ui'
import { useIcon } from '@/components/icon/useIcon'
import { useTabStore } from '@/stores/modules/tab'
import type { TabState } from '@/stores/type'
import { useThemeStore } from '@/stores/modules/theme'
/** 标签操作交由store，右键菜单和滚动只维护本组件的交互状态。 */
export function useTabBar(emit: ((event: 'fullScreen') => void) & ((event: 'refresh') => void)) {
  const { createIcon } = useIcon()
  const themeStore = useThemeStore()
  const themeVars = useThemeVars()
  const tabContentBackground = computed(
    () => themeStore.getThemeOverrides.Layout?.colorEmbedded ?? themeVars.value.bodyColor,
  )
  const router = useRouter()
  const tabStore = useTabStore()
  const clickedTab = ref<TabState | null>(null)

  const scrollbar = useTemplateRef<ScrollbarInst>('scrollbar')

  const showDropdown = ref(false)
  const x = ref(0)
  const y = ref(0)

  const dropdownOptions = computed<DropdownOption[]>(() => {
    const isSelf = clickedTab.value?.focused
    return [
      {
        label: '内容全屏',
        key: 'fullscreen',
        icon: createIcon('full-screen'),
        disabled: !isSelf,
      },
      {
        type: 'divider',
        key: 'd1',
      },
      {
        label: '刷新当前',
        key: 'refreshCurrent',
        icon: createIcon('refresh-2'),
        disabled: !isSelf,
      },
      {
        label: '关闭当前',
        key: 'closeCurrent',
        icon: createIcon('add-3'),
        disabled: !clickedTab.value?.closeable,
      },
      {
        label: '关闭其他',
        key: 'closeOther',
        icon: createIcon('guanbiqita'),
      },
      {
        label: '关闭左侧',
        key: 'closeLeft',
        icon: createIcon('d-left'),
      },
      {
        label: '关闭右侧',
        key: 'closeRight',
        icon: createIcon('d-right'),
      },
      {
        label: '关闭全部',
        key: 'closeAll',
        icon: createIcon('closure'),
      },
    ]
  })

  /**
   * 右击按钮弹出菜单
   */
  const handleContextMenu = (e: MouseEvent, tab: TabState) => {
    e.preventDefault()
    clickedTab.value = tab
    showDropdown.value = false
    nextTick().then(() => {
      showDropdown.value = true
      x.value = e.clientX
      y.value = e.clientY
    })
  }

  /**
   * 弹出菜单之后点击区域外部时
   */
  const onClickOutside = () => {
    showDropdown.value = false
  }

  /**
   * 菜单项点击事件
   * @param key
   */
  const handleSelect = async (key: string | number) => {
    showDropdown.value = false
    const clicked = clickedTab.value
    if (!clicked) return
    switch (key) {
      case 'fullscreen':
        emit('fullScreen')
        break
      case 'refreshCurrent':
        emit('refresh')
        break
      case 'closeCurrent':
        await tabStore.closeTab(clicked.name)
        break
      case 'closeOther':
        await tabStore.closeOtherTabs(clicked.name)
        break
      case 'closeLeft':
        await tabStore.closeLeftTabs(clicked.name)
        break
      case 'closeRight':
        await tabStore.closeRightTabs(clicked.name)
        break
      case 'closeAll':
        await tabStore.closeAllTabsExceptHome()
        break
    }
    clickedTab.value = null
  }

  /**
   * 点击标签页
   * @param path
   */
  const handleTabClick = (path: string) => {
    router.push(path)
  }

  /**
   * 关闭标签页的方法
   * @param tabName
   */
  const handleCloseTab = async (tabName: string) => {
    await tabStore.closeTab(tabName)
  }

  /**
   * 滚动标签页
   * @param type
   */
  const handleScroll = (type: string) => {
    if (type === 'left') {
      scrollbar.value?.scrollBy({
        left: -200,
        behavior: 'smooth',
      })
    }
    if (type === 'right') {
      scrollbar.value?.scrollBy({
        left: 200,
        behavior: 'smooth',
      })
    }
  }

  const route = useRoute()
  watch(
    () => route.fullPath,
    () => {
      showDropdown.value = false
      clickedTab.value = null
    },
    { immediate: true },
  )
  return {
    createIcon,
    themeStore,
    tabContentBackground,
    tabStore,
    showDropdown,
    x,
    y,
    dropdownOptions,
    handleContextMenu,
    onClickOutside,
    handleSelect,
    handleTabClick,
    handleCloseTab,
    handleScroll,
  }
}
