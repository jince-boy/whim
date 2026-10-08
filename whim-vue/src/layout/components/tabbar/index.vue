<script setup lang="ts">
import { useIcon } from '@/components/icon/useIcon.ts'
import type { ScrollbarInst, DropdownOption } from 'naive-ui'
import { useTabStore } from '@/stores/modules/tab.ts'
import type { TabState } from '@/stores/type.ts'
import { useThemeStore } from '@/stores/modules/theme.ts'

defineOptions({
  name: 'LayoutTabBar',
})

const emit = defineEmits<{
  fullScreen: []
  refresh: []
}>()
const { createIcon } = useIcon()
const themeStore = useThemeStore()
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
const handleSelect = (key: string | number) => {
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
      tabStore.closeTab(clicked.name)
      break
    case 'closeOther':
      tabStore.closeOtherTabs(clicked.name)
      break
    case 'closeLeft':
      tabStore.closeLeftTabs(clicked.name)
      break
    case 'closeRight':
      tabStore.closeRightTabs(clicked.name)
      break
    case 'closeAll':
      tabStore.closeAllTabsExceptHome()
      break
  }
  // 关闭后，确保聚焦的 tab 路由跳转
  const focusedTab = tabStore.getAllTabs.find((t) => t.focused)
  if (focusedTab && router.currentRoute.value.name !== focusedTab.name) {
    router.push(focusedTab.path)
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
const handleCloseTab = (tabName: string) => {
  // 先关闭标签页(更新状态)
  tabStore.closeTab(tabName)
  // 找到聚焦的标签页
  const focusedTab = tabStore.getAllTabs.find((tab) => tab.focused)
  if (focusedTab) {
    router.push(focusedTab.path)
  }
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

watch(
  () => router.currentRoute.value.fullPath, // 也可以用 route.path 或 route.name，主要是触发 watch
  () => {
    if (!router.currentRoute.value.meta?.title || !router.currentRoute.value.name) return
    const tab = {
      title: router.currentRoute.value.meta.title as string,
      path: router.currentRoute.value.fullPath,
      name: router.currentRoute.value.name as string,
      icon: router.currentRoute.value.meta.icon as string | undefined,
      closeable: true,
    } as TabState
    tabStore.addTab(tab)
  },
  { immediate: true },
)
</script>

<template>
  <n-el class="tab">
    <n-flex align="center" justify="space-between" :wrap="false" class="tab-wrapper">
      <n-button
        quaternary
        :focusable="false"
        :render-icon="createIcon('left')"
        class="tab-scroll tab-scroll-left"
        @click="handleScroll('left')"
      />
      <n-scrollbar trigger="none" ref="scrollbar" x-scrollable content-style="height:100%">
        <n-flex
          v-if="themeStore.getTabStyle === 'tag'"
          align="center"
          class="tab-box"
          :wrap="false"
        >
          <n-tag
            v-for="item in tabStore.getAllTabs"
            :key="item.name"
            :type="item.focused ? 'primary' : 'default'"
            :bordered="false"
            :closable="item.closeable"
            @click="handleTabClick(item.path)"
            @contextmenu="(e: MouseEvent) => handleContextMenu(e, item)"
            @close="handleCloseTab(item.name)"
          >
            {{ item.title }}
            <template #icon v-if="themeStore.getShowTabIcon">
              <n-icon size="18" :component="createIcon(item.icon)" />
            </template>
          </n-tag>
        </n-flex>
        <n-flex v-else align="center" class="tab-box" :wrap="false">
          <n-button
            :type="item.focused ? 'primary' : 'default'"
            size="small"
            :secondary="!item.focused"
            :focusable="false"
            icon-placement="left"
            v-for="item in tabStore.getAllTabs"
            :key="item.name"
            :render-icon="themeStore.getShowTabIcon ? createIcon(item.icon) : null"
            @click="handleTabClick(item.path)"
            @contextmenu="(e: MouseEvent) => handleContextMenu(e, item)"
          >
            {{ item.title }}
            <i
              class="iconfont icon-closure tab-close"
              v-if="item.closeable"
              @click.stop="handleCloseTab(item.name)"
            ></i>
          </n-button>
        </n-flex>
        <n-dropdown
          placement="bottom-start"
          trigger="manual"
          :x="x"
          :y="y"
          :options="dropdownOptions"
          :show="showDropdown"
          :on-clickoutside="onClickOutside"
          @select="handleSelect"
        />
      </n-scrollbar>
      <n-button
        quaternary
        :focusable="false"
        :render-icon="createIcon('right')"
        class="tab-scroll tab-scroll-right"
        @click="handleScroll('right')"
      />
    </n-flex>
  </n-el>
</template>

<style scoped lang="scss">
.tab {
  height: 36px;
  background-color: var(--card-color);
  border-bottom: 1px solid var(--divider-color);
  transition:
    color 0.3s var(--n-bezier),
    background-color 0.3s var(--n-bezier),
    box-shadow 0.3s var(--n-bezier),
    border-color 0.3s var(--n-bezier);

  .tab-wrapper {
    height: 100%;
  }

  .tab-scroll {
    border-radius: 0;
    height: 100%;
  }

  .tab-scroll-left {
    border-right: 1px solid var(--divider-color);
  }

  .tab-scroll-right {
    border-left: 1px solid var(--divider-color);
  }

  .tab-box {
    width: 100%;
    height: 100%;
  }

  :deep(.n-scrollbar-rail__scrollbar) {
    display: none;
  }

  .tab-close {
    margin-left: var(--n-icon-margin);
  }
}
</style>
