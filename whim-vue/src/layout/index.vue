<script setup lang="ts">
import { computed, ref, useTemplateRef, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import LayoutSidebar from '@/layout/components/sidebar/index.vue'
import LayoutTabBar from '@/layout/components/tabbar/index.vue'
import AppMain from '@/layout/components/appMain/index.vue'
import LayoutHeader from '@/layout/components/header/index.vue'
import LayoutFooter from '@/layout/components/footer/index.vue'
import screenfull from 'screenfull'
import { useThemeStore } from '@/stores/modules/theme'
import { usePermissionStore } from '@/stores/modules/permission'
import LayoutUserMenu from '@/layout/components/userMenu/index.vue'
import { useTabStore } from '@/stores/modules/tab'
import type { TabState } from '@/stores/type'
import { findMenuPath, getVisibleMenus, type SafeMenuOption } from '@/utils/menu'
import systemSetting from '@/config/SystemSetting'
import logoUrl from '@/assets/images/logo.png'

defineOptions({ name: 'LayoutIndex' })

const themeStore = useThemeStore()
const permissionStore = usePermissionStore()
const tabStore = useTabStore()
const route = useRoute()
const message = useMessage()
const appMainRef = useTemplateRef<InstanceType<typeof AppMain>>('appMainRef')
const sidebarExpandedWidth = 240
const sidebarCollapsedWidth = 64
const collapsed = ref(false)
const activeRootKey = ref<string | null>(null)
const layoutMode = computed(() => themeStore.config.layoutMode)
const selectedKey = computed(() => (typeof route.name === 'string' ? route.name : route.path))
const visibleMenus = computed(() => getVisibleMenus(permissionStore.menus))
const activeRoot = computed(() =>
  visibleMenus.value.find((menu) => menu.key === activeRootKey.value),
)
const sidebarMenus = computed<SafeMenuOption[]>(() =>
  layoutMode.value === 'mixed' ? (activeRoot.value?.children ?? []) : visibleMenus.value,
)
const showSidebar = computed(
  () =>
    layoutMode.value === 'side' || (layoutMode.value === 'mixed' && sidebarMenus.value.length > 0),
)
const currentSidebarWidth = computed(() =>
  collapsed.value ? sidebarCollapsedWidth : sidebarExpandedWidth,
)
const sidebarWidth = computed(() => (showSidebar.value ? `${currentSidebarWidth.value}px` : '0px'))
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

// 标签由布局维护，隐藏标签栏时仍同步路由地址和页面状态。
watch(
  () => route.fullPath,
  () => {
    if (!route.meta.title || typeof route.name !== 'string') return
    tabStore.addTab({
      title: route.meta.title,
      path: route.fullPath,
      name: route.name,
      icon: route.meta.icon,
      closeable: true,
    } as TabState)
  },
  { immediate: true },
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
</script>

<template>
  <n-layout has-sider class="layout-shell">
    <n-el
      class="layout"
      :class="`layout--${layoutMode}`"
      :style="{ '--sidebar-width': sidebarWidth }"
    >
      <n-watermark
        v-if="themeStore.getShowWatermark"
        :content="themeStore.getWatermarkText"
        cross
        fullscreen
        :font-size="18"
        :line-height="16"
        :width="300"
        :height="300"
        :x-offset="12"
        :y-offset="60"
        :rotate="-15"
      />
      <n-layout-sider
        v-if="showSidebar"
        v-model:collapsed="collapsed"
        class="layout-sidebar"
        :width="sidebarExpandedWidth"
        bordered
        show-trigger="arrow-circle"
        :collapsed-width="sidebarCollapsedWidth"
        collapse-mode="width"
        :inverted="themeStore.getMenuInverted"
      >
        <layout-sidebar
          :collapsed="collapsed"
          :menus="sidebarMenus"
          :show-logo="layoutMode === 'side'"
        />
      </n-layout-sider>
      <n-layout-header
        class="layout-header"
        :class="{ 'layout-header--inverted': layoutMode !== 'side' && themeStore.getMenuInverted }"
        bordered
        :inverted="layoutMode !== 'side' && themeStore.getMenuInverted"
      >
        <layout-header
          :inverted="layoutMode !== 'side' && themeStore.getMenuInverted"
          :brand-width="
            layoutMode === 'mixed' && themeStore.getShowLogo ? currentSidebarWidth : undefined
          "
        >
          <template v-if="layoutMode !== 'side' && themeStore.getShowLogo" #brand>
            <RouterLink to="/index" class="layout-brand" :title="systemSetting.title">
              <n-image
                :src="logoUrl"
                :width="28"
                :height="28"
                :img-props="{ alt: systemSetting.title }"
                object-fit="contain"
                preview-disabled
              />
              <n-text v-if="layoutMode !== 'mixed' || !collapsed" strong class="layout-title">{{
                systemSetting.title
              }}</n-text>
            </RouterLink>
          </template>
          <template v-if="layoutMode !== 'side'" #navigation>
            <n-menu
              class="layout-top-menu"
              mode="horizontal"
              responsive
              :icon-size="16"
              :collapsed-icon-size="16"
              :theme-overrides="{ itemHeight: '100%' }"
              :options="topMenus"
              :inverted="themeStore.getMenuInverted"
              :value="layoutMode === 'mixed' ? activeRootKey : selectedKey"
              @update:value="handleTopMenuSelect"
            />
          </template>
          <template v-if="layoutMode === 'top' || !showSidebar" #user>
            <layout-user-menu compact />
          </template>
        </layout-header>
      </n-layout-header>
      <layout-tab-bar
        v-if="themeStore.getShowTabs"
        class="layout-tabbar"
        @full-screen="toggleFullScreen"
        @refresh="refreshAppMain"
      />
      <n-layout-content class="layout-content" :native-scrollbar="false" embedded>
        <app-main ref="appMainRef" />
      </n-layout-content>
      <n-layout-footer class="layout-footer" bordered>
        <layout-footer />
      </n-layout-footer>
    </n-el>
  </n-layout>
</template>

<style scoped lang="scss">
.layout-shell {
  height: 100vh;
  height: 100dvh;
}

.layout {
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  grid-template-rows: 50px auto minmax(0, 1fr) 50px;
  grid-template-areas:
    'header header'
    'sidebar tabbar'
    'sidebar content'
    'sidebar footer';
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.layout--side {
  grid-template-areas:
    'sidebar header'
    'sidebar tabbar'
    'sidebar content'
    'sidebar footer';
}

.layout-sidebar {
  grid-area: sidebar;
  height: 100%;
}

.layout-header {
  grid-area: header;
  min-width: 0;
}

.layout-content {
  grid-area: content;
  min-height: 0;
  min-width: 0;
}

.layout-tabbar {
  grid-area: tabbar;
  min-width: 0;
}

.layout-footer {
  grid-area: footer;
  min-width: 0;
}

.layout-header :deep(.layout-top-menu) {
  height: 100%;
}

.layout-header :deep(.layout-top-menu .v-overflow),
.layout-header :deep(.layout-top-menu .n-submenu) {
  height: 100%;
}

.layout-header :deep(.layout-top-menu .n-menu-item-content) {
  // 与底部的 2px 状态边框对称，保持内容在 Header 中垂直居中。
  padding-top: 2px;
}

.layout-header :deep(.layout-top-menu .n-menu-item-content-header) {
  line-height: 1;
}

.layout-header :deep(.layout-top-menu .n-menu-item-content__icon .n-icon) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.layout-header :deep(.layout-top-menu .n-menu-item-content__icon .iconfont) {
  display: block;
  line-height: 1;
}

.layout-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.layout-title {
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout--mixed .layout-brand {
  justify-content: center;
  width: 100%;
  min-width: 0;
  padding: 0 12px;
}

.layout-brand :deep(.n-image) {
  flex-shrink: 0;
}

.layout-header--inverted .layout-brand :deep(img) {
  filter: brightness(0) invert(1);
}

@media (max-width: 900px) {
  .layout--top .layout-title {
    display: none;
  }
}
</style>
