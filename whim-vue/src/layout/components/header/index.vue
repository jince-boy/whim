<script setup lang="ts">
import { useIcon } from '@/components/icon/useIcon.ts'
import SearchComponent from '@/components/search/index.vue'
import ThemeComponent from '@/components/theme/index.vue'
import { useHeader } from './hooks/useHeader'

defineOptions({
  name: 'LayoutHeader',
})

const { createIcon } = useIcon()
const {
  breadcrumbs,
  searchShowModal,
  themeShowModal,
  fullscreenEnabled,
  isFullscreen,
  handleMenuSelect,
  toggleFullscreen,
} = useHeader()
</script>

<template>
  <n-el class="header">
    <n-flex justify="space-between" align="center" :wrap="false" class="header-box">
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
      </n-flex>
    </n-flex>
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

.header-breadcrumb {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.header-actions {
  flex-shrink: 0;
}

.header-action {
  width: 32px;
  height: 32px;
}
</style>
