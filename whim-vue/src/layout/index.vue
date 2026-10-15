<script setup lang="ts">
import { RouterLink } from 'vue-router'
import LayoutSidebar from '@/layout/components/sidebar/index.vue'
import LayoutTabBar from '@/layout/components/tabbar/index.vue'
import AppMain from '@/layout/components/appMain/index.vue'
import LayoutHeader from '@/layout/components/header/index.vue'
import LayoutFooter from '@/layout/components/footer/index.vue'
import LayoutUserMenu from '@/layout/components/userMenu/index.vue'
import systemSetting from '@/config/SystemSetting'
import logoUrl from '@/assets/images/logo.png'
import { useLayout } from './hooks/useLayout'
defineOptions({ name: 'LayoutIndex' })
const {
  themeStore,
  homePath,
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
} = useLayout()
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
            <RouterLink :to="homePath" class="layout-brand" :title="systemSetting.title">
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
