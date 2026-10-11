<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { darkTheme, useMessage, type DropdownOption } from 'naive-ui'
import screenfull from 'screenfull'
import { useIcon } from '@/components/icon/useIcon.ts'
import SearchComponent from '@/components/search/index.vue'
import ThemeComponent from '@/components/theme/index.vue'
import { usePermissionStore } from '@/stores/modules/permission'
import { findMenuPath, getVisibleMenus, type SafeMenuOption } from '@/utils/menu'

defineOptions({
  name: 'LayoutHeader',
})

const props = withDefaults(defineProps<{ inverted?: boolean; brandWidth?: number }>(), {
  inverted: false,
})

const { createIcon } = useIcon()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const message = useMessage()
const searchShowModal = ref(false)
const themeShowModal = ref(false)
const fullscreenEnabled = screenfull.isEnabled
const isFullscreen = ref(fullscreenEnabled && screenfull.isFullscreen)

// 使用菜单构建时相同的 key 规则，兼容未命名路由。
const breadcrumbs = computed<SafeMenuOption[]>(() =>
  findMenuPath(permissionStore.menus, typeof route.name === 'string' ? route.name : route.path).map(
    (menu) => ({
      ...menu,
      children: menu.children ? getVisibleMenus(menu.children) : undefined,
    }),
  ),
)

/** 通过下拉菜单选中项导航，支持整行点击和键盘选择。 */
async function handleMenuSelect(_key: string | number, option: DropdownOption): Promise<void> {
  const path = option.path
  if (typeof path !== 'string' || !path) return
  if (option.isExternal) {
    window.open(path, '_blank', 'noopener,noreferrer')
  } else {
    await router.push(path)
  }
}

/** 同步浏览器全屏状态，包括按 Esc 退出和主内容区全屏。 */
function syncFullscreen(): void {
  isFullscreen.value = screenfull.isFullscreen
}

/** 切换页面全屏，失败时给出提示。 */
async function toggleFullscreen(): Promise<void> {
  if (!fullscreenEnabled) return
  try {
    await screenfull.toggle()
  } catch {
    message.error('切换全屏失败')
  }
}

// 组件挂载后监听全屏变化，卸载时移除监听。
onMounted(() => {
  if (fullscreenEnabled) {
    screenfull.on('change', syncFullscreen)
    syncFullscreen()
  }
})
onBeforeUnmount(() => {
  if (fullscreenEnabled) screenfull.off('change', syncFullscreen)
})
</script>

<template>
  <n-el class="header" :class="{ 'header--fixed-brand': props.brandWidth !== undefined }">
    <n-config-provider abstract :theme="props.inverted ? darkTheme : undefined">
      <n-flex
        justify="space-between"
        align="center"
        :wrap="false"
        :size="props.brandWidth === undefined ? 16 : 0"
        class="header-box"
      >
        <div
          v-if="$slots.brand"
          class="header-brand"
          :style="props.brandWidth === undefined ? undefined : { width: `${props.brandWidth}px` }"
        >
          <slot name="brand" />
        </div>
        <div class="header-navigation">
          <slot name="navigation">
            <n-breadcrumb class="header-breadcrumb">
              <n-breadcrumb-item
                v-for="item in breadcrumbs"
                :key="item.key"
                :clickable="!!item.children?.length"
              >
                <n-dropdown
                  :disabled="!item.children?.length"
                  :options="item.children ?? []"
                  :render-icon="() => null"
                  label-field="name"
                  placement="bottom-start"
                  @select="handleMenuSelect"
                >
                  <span>{{ item.name }}</span>
                </n-dropdown>
              </n-breadcrumb-item>
            </n-breadcrumb>
          </slot>
        </div>
        <n-flex align="center" :wrap="false" :size="4" class="header-actions">
          <n-tooltip placement="bottom" trigger="hover" :delay="300">
            <template #trigger>
              <n-button
                quaternary
                size="small"
                class="header-action"
                aria-label="搜索"
                @click="searchShowModal = true"
              >
                <template #icon>
                  <n-icon :component="createIcon('search', 18)" />
                </template>
              </n-button>
            </template>
            <span>搜索</span>
          </n-tooltip>
          <n-tooltip placement="bottom" trigger="hover" :delay="300">
            <template #trigger>
              <n-button
                quaternary
                size="small"
                class="header-action"
                :disabled="!fullscreenEnabled"
                :aria-label="isFullscreen ? '退出全屏' : '全屏'"
                @click="toggleFullscreen"
              >
                <template #icon>
                  <n-icon v-if="isFullscreen" :component="createIcon('quanpingsuoxiao', 18)" />
                  <n-icon v-else :component="createIcon('full-screen', 18)" />
                </template>
              </n-button>
            </template>
            <span>{{ isFullscreen ? '退出全屏' : '全屏' }}</span>
          </n-tooltip>
          <n-tooltip placement="bottom" trigger="hover" :delay="300">
            <template #trigger>
              <n-button
                quaternary
                size="small"
                class="header-action"
                aria-label="主题配置"
                @click="themeShowModal = true"
              >
                <template #icon>
                  <n-icon :component="createIcon('zhuti', 18)" />
                </template>
              </n-button>
            </template>
            <span>主题配置</span>
          </n-tooltip>
          <n-popover trigger="click" placement="bottom-end" :show-arrow="false" :width="240">
            <template #trigger>
              <n-button quaternary size="small" class="header-action" aria-label="通知">
                <template #icon>
                  <n-icon :component="createIcon('notify', 18)" />
                </template>
              </n-button>
            </template>
            <template #header>
              <n-text strong>通知</n-text>
            </template>
            <n-empty description="暂无通知" size="small" />
          </n-popover>
          <slot name="user" />
        </n-flex>
      </n-flex>
    </n-config-provider>
    <search-component v-model="searchShowModal"></search-component>
    <theme-component v-model="themeShowModal"></theme-component>
  </n-el>
</template>

<style scoped lang="scss">
.header {
  padding: 0 16px;
  height: 100%;
}

.header-box {
  height: 100%;
}

.header--fixed-brand {
  padding-left: 0;
}

.header-brand {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  height: 100%;
  overflow: hidden;
}

.header-navigation {
  display: flex;
  align-items: center;
  flex: 1;
  height: 100%;
  min-width: 0;
  overflow: hidden;
}

.header-breadcrumb {
  white-space: nowrap;
}

.header-actions {
  flex-shrink: 0;
}

.header-action {
  width: 32px;
  height: 32px;
}
</style>
