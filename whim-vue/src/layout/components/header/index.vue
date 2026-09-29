<script setup lang="ts">
import { useRoute } from 'vue-router'
import {
  ContractOutline,
  ExpandOutline,
  MenuOutline,
  MoonOutline,
  SunnyOutline,
} from '@vicons/ionicons5'
import { useThemeStore } from '@/stores/theme'

defineOptions({ name: 'LayoutHeader' })

defineEmits<{ openMenu: [] }>()

const route = useRoute()
const themeStore = useThemeStore()
const message = useMessage()
const isFullscreen = ref(false)

/** 根据浏览器状态同步全屏按钮。 */
function syncFullscreenState(): void {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

/** 切换浏览器全屏状态。 */
async function toggleFullscreen(): Promise<void> {
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
    } else {
      await document.documentElement.requestFullscreen()
    }
  } catch {
    message.warning('无法切换全屏模式')
  }
}

/** 订阅浏览器全屏状态变化。 */
onMounted(() => document.addEventListener('fullscreenchange', syncFullscreenState))

/** 离开布局时移除全屏状态监听。 */
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', syncFullscreenState))
</script>

<template>
  <n-flex class="header" justify="space-between" align="center" :wrap="false">
    <n-flex align="center" :wrap="false">
      <n-button
        class="mobile-menu-button"
        tertiary
        circle
        aria-label="打开导航菜单"
        @click="$emit('openMenu')"
      >
        <template #icon
          ><n-icon><menu-outline /></n-icon
        ></template>
      </n-button>
      <n-breadcrumb>
        <n-breadcrumb-item
          v-for="record in route.matched.filter((item) => item.meta.title)"
          :key="record.path"
        >
          {{ record.meta.title }}
        </n-breadcrumb-item>
      </n-breadcrumb>
    </n-flex>

    <n-space :size="8">
      <n-tooltip trigger="hover" placement="bottom">
        <template #trigger>
          <n-button
            tertiary
            circle
            :aria-label="isFullscreen ? '退出全屏' : '进入全屏'"
            @click="toggleFullscreen"
          >
            <template #icon>
              <n-icon><contract-outline v-if="isFullscreen" /><expand-outline v-else /></n-icon>
            </template>
          </n-button>
        </template>
        {{ isFullscreen ? '退出全屏' : '全屏' }}
      </n-tooltip>
      <n-tooltip trigger="hover" placement="bottom">
        <template #trigger>
          <n-button
            tertiary
            circle
            :aria-label="themeStore.isDark ? '切换为浅色主题' : '切换为深色主题'"
            @click="themeStore.toggleTheme"
          >
            <template #icon>
              <n-icon><sunny-outline v-if="themeStore.isDark" /><moon-outline v-else /></n-icon>
            </template>
          </n-button>
        </template>
        {{ themeStore.isDark ? '浅色主题' : '深色主题' }}
      </n-tooltip>
    </n-space>
  </n-flex>
</template>

<style scoped>
.header {
  height: 100%;
  padding: 0 20px;
}

.mobile-menu-button {
  display: none;
}

@media (max-width: 768px) {
  .mobile-menu-button {
    display: inline-flex;
  }
}
</style>
