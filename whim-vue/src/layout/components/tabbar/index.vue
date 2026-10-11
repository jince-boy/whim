<script setup lang="ts">
import { useIcon } from '@/components/icon/useIcon.ts'
import type { ScrollbarInst, DropdownOption } from 'naive-ui'
import { useThemeVars } from 'naive-ui'
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
</script>

<template>
  <n-el
    class="tab"
    :class="`tab--${themeStore.getTabStyle}`"
    :style="{
      '--tab-content-background': tabContentBackground,
    }"
  >
    <n-flex align="center" justify="space-between" :wrap="false" :size="0" class="tab-wrapper">
      <n-button
        quaternary
        :focusable="false"
        :render-icon="createIcon('left')"
        class="tab-scroll tab-scroll-left"
        aria-label="向左滚动标签"
        @click="handleScroll('left')"
      />
      <n-scrollbar
        trigger="none"
        ref="scrollbar"
        class="tab-scrollbar"
        x-scrollable
        content-style="height:100%"
      >
        <n-flex
          v-if="themeStore.getTabStyle === 'button'"
          align="center"
          class="tab-box"
          :wrap="false"
          role="navigation"
          aria-label="页面标签"
        >
          <n-button
            v-for="item in tabStore.getAllTabs"
            :key="item.name"
            class="button-tab"
            :type="item.focused ? 'primary' : 'default'"
            size="small"
            :secondary="!item.focused"
            :focusable="false"
            icon-placement="left"
            :render-icon="themeStore.getShowTabIcon ? createIcon(item.icon) : null"
            :aria-current="item.focused ? 'page' : undefined"
            @click="handleTabClick(item.path)"
            @contextmenu="(e: MouseEvent) => handleContextMenu(e, item)"
          >
            {{ item.title }}
            <i
              v-if="item.closeable"
              class="iconfont icon-closure button-tab-close"
              :title="`关闭${item.title}`"
              @click.stop="handleCloseTab(item.name)"
            ></i>
          </n-button>
        </n-flex>
        <div v-else class="tab-box" role="navigation" aria-label="页面标签">
          <div
            v-for="item in tabStore.getAllTabs"
            :key="item.name"
            class="tab-item"
            :class="{ 'tab-item--active': item.focused }"
            @contextmenu="(e: MouseEvent) => handleContextMenu(e, item)"
          >
            <button
              type="button"
              class="tab-link"
              :aria-current="item.focused ? 'page' : undefined"
              :title="item.title"
              @click="handleTabClick(item.path)"
            >
              <n-icon
                v-if="themeStore.getShowTabIcon && item.icon"
                :size="16"
                :component="createIcon(item.icon)"
              />
              <span class="tab-title">{{ item.title }}</span>
            </button>
            <button
              v-if="item.closeable"
              type="button"
              class="tab-close"
              :aria-label="`关闭${item.title}`"
              :title="`关闭${item.title}`"
              @click.stop="handleCloseTab(item.name)"
            >
              <i class="iconfont icon-closure" aria-hidden="true" />
            </button>
          </div>
        </div>
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
        aria-label="向右滚动标签"
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
    flex-shrink: 0;
  }

  .tab-scroll-left {
    border-right: 1px solid var(--divider-color);
  }

  .tab-scroll-right {
    border-left: 1px solid var(--divider-color);
  }

  .tab-box {
    display: flex;
    align-items: center;
    gap: 6px;
    width: max-content;
    min-width: 100%;
    height: 100%;
    padding: 0 8px;
    box-sizing: border-box;
  }

  :deep(.n-scrollbar-rail__scrollbar) {
    display: none;
  }
}

.tab-scrollbar {
  flex: 1;
  min-width: 0;
}

.tab-item {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  height: 28px;
  padding: 0 6px 0 10px;
  border: 1px solid transparent;
  color: var(--text-color-2);
  box-sizing: border-box;
  transition:
    color 0.2s,
    background-color 0.2s,
    border-color 0.2s;

  &:hover {
    color: var(--primary-color);
    background: var(--hover-color);
  }
}

.tab-link,
.tab-close {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;

  &:focus-visible {
    outline: 2px solid var(--primary-color);
    outline-offset: -2px;
    border-radius: 3px;
  }
}

.tab-link {
  gap: 6px;
  height: 100%;
}

.tab-title {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tab-close {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  margin-left: 4px;
  border-radius: 50%;

  &:hover {
    background: var(--action-color);
  }

  .iconfont {
    font-size: 12px;
    line-height: 1;
  }
}

.tab-link :deep(.n-icon) {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.tab-link :deep(.iconfont) {
  display: block;
  line-height: 1;
}

.tab--button .tab-box {
  padding: 0;
}

.button-tab {
  flex-shrink: 0;
}

.button-tab-close {
  margin-left: var(--n-icon-margin);
}

.tab--line .tab-box {
  gap: 0;
}

.tab--line .tab-item {
  position: relative;
  height: 100%;
  border: 0;
  border-bottom: 2px solid transparent;
  padding-top: 2px;
  padding-right: 12px;
  padding-left: 12px;

  & + .tab-item::before {
    position: absolute;
    top: 10px;
    bottom: 8px;
    left: 0;
    width: 1px;
    background: var(--divider-color);
    content: '';
  }

  &--active {
    border-bottom-color: var(--primary-color);
    color: var(--primary-color);
  }
}

.tab--browser {
  background: var(--action-color);
  border-bottom: 0;
}

.tab--browser .tab-box {
  align-items: flex-end;
  gap: 0;
  padding: 0 12px;
}

.tab--browser .tab-item {
  position: relative;
  z-index: 1;
  min-width: 110px;
  height: 32px;
  padding: 0 10px 0 14px;
  border: 0;
  border-radius: 8px 8px 0 0;

  .tab-link {
    flex: 1;
    justify-content: flex-start;
  }

  &::after {
    position: absolute;
    top: 8px;
    right: 0;
    bottom: 8px;
    width: 1px;
    background: var(--divider-color);
    content: '';
    pointer-events: none;
  }

  &:has(+ .tab-item--active)::after {
    display: none;
  }

  &--active {
    z-index: 2;
    background: var(--tab-content-background);
    color: var(--primary-color);

    // 两侧反向圆角让选中的标签与下方内容区自然衔接。
    &::before,
    &::after {
      position: absolute;
      top: auto;
      bottom: 0;
      width: 10px;
      height: 10px;
      background: transparent;
      content: '';
      pointer-events: none;
    }

    &::before {
      left: -10px;
      border-bottom-right-radius: 10px;
      box-shadow: 4px 4px 0 4px var(--tab-content-background);
    }

    &::after {
      right: -10px;
      border-bottom-left-radius: 10px;
      box-shadow: -4px 4px 0 4px var(--tab-content-background);
    }
  }
}
</style>
