<script setup lang="ts">
import { darkTheme } from 'naive-ui'
import SearchComponent from '@/components/search/index.vue'
import ThemeComponent from '@/components/theme/index.vue'
import { useHeader } from './hooks/useHeader'
defineOptions({ name: 'LayoutHeader' })
const props = withDefaults(defineProps<{ inverted?: boolean; brandWidth?: number }>(), {
  inverted: false,
})
const {
  createIcon,
  breadcrumbs,
  handleMenuSelect,
  searchShowModal,
  themeShowModal,
  fullscreenEnabled,
  isFullscreen,
  toggleFullscreen,
} = useHeader()
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
