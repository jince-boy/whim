<script setup lang="ts">
import systemSetting from '@/config/SystemSetting.ts'
import logoUrl from '@/assets/images/logo.png'
import { computed, useTemplateRef, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/modules/permission.ts'
import type { MenuInst } from 'naive-ui'
import { useThemeStore } from '@/stores/modules/theme.ts'
import { useUserStore } from '@/stores/modules/user.ts'

defineOptions({
  name: 'LayoutSidebar',
})
const props = withDefaults(
  defineProps<{
    collapsed?: boolean
  }>(),
  {
    collapsed: false,
  },
)
const route = useRoute()
const permissionStore = usePermissionStore()
const themeStore = useThemeStore()
const userStore = useUserStore()

const menuInstRef = useTemplateRef<MenuInst>('menuInstRef')
// 菜单由路由决定选中项，点击菜单时由已有 RouterLink 完成导航。
const selectedKey = computed(() => (typeof route.name === 'string' ? route.name : route.path))
const username = computed(() => userStore.user?.username || '用户')
const avatarText = computed(() => username.value.slice(0, 1).toUpperCase())

// 等待菜单渲染后展开当前路由的父菜单，兼顾菜单加载和侧栏重新展开。
watch(
  [selectedKey, () => props.collapsed, () => permissionStore.getMenus, menuInstRef],
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
  <n-layout class="sidebar" :class="{ 'sidebar--with-logo': themeStore.getShowLogo }">
    <n-layout-header
      v-if="themeStore.getShowLogo"
      bordered
      position="absolute"
      :inverted="themeStore.getMenuInverted"
      class="sidebar-bar"
    >
      <RouterLink to="/index">
        <n-flex justify="center" align="center" :wrap="false" :size="12" class="sidebar-row">
          <n-image
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
        :options="permissionStore.getMenus"
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
      <n-flex
        align="center"
        :justify="props.collapsed ? 'center' : 'flex-start'"
        :wrap="false"
        :size="12"
        :title="username"
        class="sidebar-row sidebar-user"
      >
        <n-avatar
          round
          :size="32"
          :src="userStore.user?.avatar ?? undefined"
          :img-props="{ alt: username }"
          object-fit="cover"
        >
          <template v-if="!userStore.user?.avatar" #default>{{ avatarText }}</template>
          <template #fallback>{{ avatarText }}</template>
        </n-avatar>
        <n-ellipsis v-if="!props.collapsed" :tooltip="false" class="sidebar-username">
          {{ username }}
        </n-ellipsis>
      </n-flex>
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

.sidebar-user {
  padding: 0 16px;
}

.sidebar-username {
  flex: 1;
  min-width: 0;
}
</style>
