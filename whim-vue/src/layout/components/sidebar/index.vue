<script setup lang="ts">
import systemSetting from '@/config/SystemSetting.ts'
import { RouterLink } from 'vue-router'
import { usePermissionStore } from '@/stores/modules/permission.ts'
import { useRoute } from 'vue-router'
import type { MenuInst } from 'naive-ui'
import { useThemeStore } from '@/stores/modules/theme.ts'

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

const menuInstRef = useTemplateRef<MenuInst>('menuInstRef')
// 当前选中的菜单key
const selectedKey = ref<string>(route.name as string)

watch(
  () => route.name,
  (newName) => {
    selectedKey.value = newName as string
    menuInstRef.value?.showOption(newName as string)
  },
  {
    immediate: true,
  },
)
</script>

<template>
  <router-link to="/index" v-if="themeStore.getShowLogo">
    <n-flex class="logo" justify="center" align="center" :wrap="false">
      <img src="../../../assets/images/logo.png" alt="logo" />
      <span v-if="!props.collapsed">{{ systemSetting.title }}</span>
    </n-flex>
  </router-link>
  <n-menu
    ref="menuInstRef"
    :collapsed-width="64"
    :collapsed-icon-size="16"
    :icon-size="16"
    :accordion="true"
    :options="permissionStore.getMenus"
    v-model:value="selectedKey"
    :inverted="themeStore.getMenuInverted"
  />
</template>

<style scoped lang="scss">
.n-flex {
  height: 100%;
}

.logo {
  height: 50px;
  border-bottom: 1px solid var(--divider-color);
  white-space: nowrap;
  overflow: hidden;
  transition:
    background-color 0.3s var(--n-bezier),
    box-shadow 0.3s var(--n-bezier),
    border-color 0.3s var(--n-bezier);

  img {
    //width: 30px;
    height: 20px;
  }

  span {
    font-size: 18px;
    //font-weight: 500;
  }
}

.n-menu {
  user-select: none;
}
</style>
