<script setup lang="ts">
import systemSetting from '@/config/SystemSetting.ts'
import logoUrl from '@/assets/images/logo.png'
import { computed, useTemplateRef, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import type { MenuInst } from 'naive-ui'
import { useThemeStore } from '@/stores/modules/theme.ts'
import LayoutUserMenu from '@/layout/components/userMenu/index.vue'
import type { SafeMenuOption } from '@/utils/menu'

defineOptions({
  name: 'LayoutSidebar',
})
const props = withDefaults(
  defineProps<{
    collapsed?: boolean
    menus: SafeMenuOption[]
    showLogo?: boolean
  }>(),
  {
    collapsed: false,
    showLogo: true,
  },
)
const route = useRoute()
const themeStore = useThemeStore()

const menuInstRef = useTemplateRef<MenuInst>('menuInstRef')
// 菜单由路由决定选中项，点击菜单时由已有 RouterLink 完成导航。
const selectedKey = computed(() => (typeof route.name === 'string' ? route.name : route.path))

// 等待菜单渲染后展开当前路由的父菜单，兼顾菜单加载和侧栏重新展开。
watch(
  [selectedKey, () => props.collapsed, () => props.menus, menuInstRef],
  ([key, collapsed]) => {
    if (!collapsed) {
      menuInstRef.value?.showOption(key)
    }
  },
  {
    immediate: true,
    flush: 'post',
  },
)
</script>

<template>
  <n-layout
    class="sidebar"
    :class="{
      'sidebar--with-logo': props.showLogo && themeStore.getShowLogo,
      'sidebar--inverted': themeStore.getMenuInverted,
    }"
  >
    <n-layout-header
      v-if="props.showLogo && themeStore.getShowLogo"
      bordered
      position="absolute"
      :inverted="themeStore.getMenuInverted"
      class="sidebar-bar"
    >
      <RouterLink to="/index">
        <n-flex justify="center" align="center" :wrap="false" :size="12" class="sidebar-row">
          <n-image
            class="sidebar-logo"
            :src="logoUrl"
            :width="28"
            :height="28"
            :img-props="{ alt: systemSetting.title }"
            object-fit="contain"
            preview-disabled
          />
          <n-text v-if="!props.collapsed" strong class="sidebar-title">
            {{ systemSetting.title }}
          </n-text>
        </n-flex>
      </RouterLink>
    </n-layout-header>
    <n-layout-content position="absolute" :native-scrollbar="false" class="sidebar-menu">
      <n-menu
        ref="menuInstRef"
        :collapsed="props.collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="16"
        :icon-size="16"
        accordion
        :options="props.menus"
        :value="selectedKey"
        :inverted="themeStore.getMenuInverted"
      />
    </n-layout-content>
    <n-layout-footer
      bordered
      position="absolute"
      :inverted="themeStore.getMenuInverted"
      class="sidebar-bar"
    >
      <layout-user-menu :collapsed="props.collapsed" />
    </n-layout-footer>
  </n-layout>
</template>

<style scoped lang="scss">
.sidebar,
.sidebar-menu {
  background: transparent;
}

.sidebar,
.sidebar-row {
  height: 100%;
}

.sidebar-bar {
  height: 50px;
}

.sidebar-menu {
  top: 0;
  bottom: 50px;
}

.sidebar--with-logo .sidebar-menu {
  top: 50px;
}

.sidebar-title {
  font-size: 16px;
  color: inherit;
}

.sidebar--inverted .sidebar-logo :deep(img) {
  filter: brightness(0) invert(1);
}
</style>
