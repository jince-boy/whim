<script setup lang="ts">
import type { SafeMenuOption } from '@/utils/menu'
import { usePermissionStore } from '@/stores/modules/permission.ts'
import { useIcon } from '@/components/icon/useIcon.ts'
import screenfull from 'screenfull'
import SearchComponent from '@/components/search/index.vue'
import ThemeComponent from '@/components/theme/index.vue'
import { useLogout } from './hooks/useLogout'

defineOptions({
  name: 'LayoutHeader',
})

const { createIcon } = useIcon()
const route = useRoute()
const permissionStore = usePermissionStore()
const { loggingOut, handleLogout } = useLogout()

const searchShowModal = ref(false)
const handleSearchClick = () => {
  searchShowModal.value = true
}

const themeShowModal = ref(false)

const handleThemeClick = () => {
  themeShowModal.value = true
}

const breadCrumbData = computed(() => {
  return findPathInMenu(permissionStore.getMenus, route.name as string)
})

const findPathInMenu = (menus: SafeMenuOption[], routeName: string): SafeMenuOption[] | null => {
  // 深度优先搜索（DFS）
  const dfs = (
    nodes: SafeMenuOption[],
    targetName: string,
    currentPath: SafeMenuOption[],
  ): SafeMenuOption[] | null => {
    for (const node of nodes) {
      const newPath = [...currentPath, node]
      // 关键匹配逻辑：菜单节点的key === 路由的name
      if (node.key === targetName) {
        return newPath
      }
      // 递归搜索子节点
      if (node.children) {
        const result = dfs(node.children, targetName, newPath)
        if (result) return result
      }
    }
    return null // 未找到
  }

  return dfs(menus, routeName, [])
}
</script>

<template>
  <n-el class="header">
    <n-flex justify="space-between" align="center" class="header-box">
      <n-breadcrumb>
        <n-breadcrumb-item v-for="item in breadCrumbData" :key="item.key">
          <!-- 有子菜单时显示下拉菜单 -->
          <n-dropdown
            v-if="item.children?.length"
            :options="item.children"
            placement="bottom-start"
          >
            <span>{{ item.name }}</span>
          </n-dropdown>
          <!-- 没有子菜单时直接显示 -->
          <template v-else>
            <span>{{ item.name }}</span>
          </template>
        </n-breadcrumb-item>
      </n-breadcrumb>
      <n-space>
        <n-tooltip placement="bottom" trigger="hover">
          <template #trigger>
            <n-button tertiary circle @click="handleSearchClick">
              <template #icon>
                <n-icon :component="createIcon('search')" />
              </template>
            </n-button>
          </template>
          <span>搜索</span>
        </n-tooltip>
        <n-tooltip placement="bottom" trigger="hover">
          <template #trigger>
            <n-button tertiary circle @click="screenfull.toggle()">
              <template #icon>
                <n-icon v-if="screenfull.isFullscreen" :component="createIcon('quanpingsuoxiao')" />
                <n-icon v-else :component="createIcon('full-screen')" />
              </template>
            </n-button>
          </template>
          <span>全屏</span>
        </n-tooltip>
        <n-tooltip placement="bottom" trigger="hover">
          <template #trigger>
            <n-button tertiary circle @click="handleThemeClick">
              <template #icon>
                <n-icon :component="createIcon('zhuti')" />
              </template>
            </n-button>
          </template>
          <span>主题配置</span>
        </n-tooltip>
        <n-popover trigger="hover" raw :show-arrow="false">
          <template #trigger>
            <n-button tertiary circle>
              <template #icon>
                <n-icon :component="createIcon('notify')" />
              </template>
            </n-button>
          </template>
          <div>通知内容</div>
        </n-popover>
        <n-button quaternary :loading="loggingOut" :disabled="loggingOut" @click="handleLogout">
          退出登录
        </n-button>
      </n-space>
      <search-component v-model="searchShowModal"></search-component>
      <theme-component v-model="themeShowModal"></theme-component>
    </n-flex>
  </n-el>
</template>

<style scoped lang="scss">
.header {
  padding: 0 20px;
  height: 100%;
}

.header-box {
  height: 100%;
}
</style>
