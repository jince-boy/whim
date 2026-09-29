<script setup lang="ts">
import {h} from 'vue'
import {useRoute} from 'vue-router'
import {NIcon, type MenuOption} from 'naive-ui'
import {GridOutline} from '@vicons/ionicons5'
import logoUrl from '@/assets/logo.png'

defineOptions({name: 'LayoutSidebar'})

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false,
  },
})

const appName = import.meta.env.VITE_APP_NAME

/** 渲染工作台菜单图标。 */
function renderDashboardIcon() {
  return h(NIcon, null, {default: () => h(GridOutline)})
}

const menuOptions: MenuOption[] = [{label: '工作台', key: '/', icon: renderDashboardIcon}]
</script>

<template>
  <router-link class="logo-link" to="/">
    <n-flex class="logo" justify="center" align="center" :wrap="false">
      <img :src="logoUrl" :alt="appName"/>
      <n-text v-if="!collapsed" strong>{{ appName }}</n-text>
    </n-flex>
  </router-link>
  <n-divider style="margin: 0"/>
  <n-menu
    ref="menuInstRef"
    :collapsed-width="64"
    :collapsed-icon-size="16"
    :icon-size="16"
    :accordion="true"
    :options="menuOptions"
  />
</template>

<style scoped>
.logo-link {
  color: inherit;
  text-decoration: none;
}

.logo {
  height: 49px;
  overflow: hidden;
  white-space: nowrap;
}

.logo img {
  width: 30px;
  height: 30px;
}
</style>
