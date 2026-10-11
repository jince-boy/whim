<script setup lang="ts">
import { useThemeStore } from '@/stores/modules/theme'
import { useThemeVars } from 'naive-ui'
import type { LayoutMode } from '@/stores/type'
import { isPageTransitionMode, pageTransitionOptions } from '@/config/PageTransition'
import { isTabStyle, tabStyleOptions } from '@/config/TabStyle'

defineOptions({ name: 'ThemeComponent' })

const themeStore = useThemeStore()
const themeVars = useThemeVars()
const themeShowModal = defineModel<boolean>({ required: true })
const layoutOptions: { value: LayoutMode; label: string; description: string }[] = [
  { value: 'side', label: '左侧菜单', description: '完整菜单显示在左侧' },
  { value: 'mixed', label: '混合菜单', description: '顶部一级菜单，左侧子菜单' },
  { value: 'top', label: '顶部菜单', description: '完整菜单显示在顶部' },
]

/** 将主题选择交给 store，保留跟随系统模式。 */
function handleThemeChange(value: string | number): void {
  if (value === 'light' || value === 'dark' || value === 'system') {
    themeStore.setThemeMode(value)
  }
}

/** 校验组件返回的标签风格。 */
function handleTabStyleChange(value: string): void {
  if (isTabStyle(value)) themeStore.setTabStyle(value)
}

/** 校验并保存用户选择的页面切换动画。 */
function handlePageTransitionChange(value: string): void {
  if (isPageTransitionMode(value)) themeStore.setPageTransition(value)
}
</script>

<template>
  <n-drawer
    v-model:show="themeShowModal"
    :width="360"
    :style="{
      '--setting-primary': themeVars.primaryColor,
      '--setting-border': themeVars.borderColor,
      '--setting-fill': themeVars.actionColor,
    }"
  >
    <n-drawer-content title="主题配置" closable>
      <n-divider>布局方式</n-divider>
      <div class="layout-options" role="group" aria-label="布局方式">
        <button
          v-for="option in layoutOptions"
          :key="option.value"
          type="button"
          class="layout-option"
          :class="{ 'layout-option--active': themeStore.config.layoutMode === option.value }"
          :aria-pressed="themeStore.config.layoutMode === option.value"
          :title="option.description"
          @click="themeStore.setLayoutMode(option.value)"
        >
          <span
            class="layout-preview"
            :class="`layout-preview--${option.value}`"
            aria-hidden="true"
          >
            <span class="preview-header" />
            <span v-if="option.value !== 'top'" class="preview-sidebar" />
            <span class="preview-content" />
          </span>
          <span>{{ option.label }}</span>
        </button>
      </div>
      <n-divider>主题模式</n-divider>
      <n-tabs
        type="segment"
        animated
        :value="themeStore.config.theme"
        @update:value="handleThemeChange"
      >
        <n-tab name="light">浅色</n-tab>
        <n-tab name="dark">深色</n-tab>
        <n-tab name="system">跟随系统</n-tab>
      </n-tabs>
      <n-divider>主题颜色</n-divider>
      <n-space vertical size="large">
        <n-color-picker
          :value="themeStore.config.primaryColor"
          :show-alpha="false"
          :modes="['hex']"
          @update:value="themeStore.setThemeColor"
        />
        <n-flex justify="space-between" align="center">
          <span>暗色菜单</span>
          <n-switch
            :value="themeStore.getMenuInverted"
            @update:value="themeStore.setMenuInverted"
          />
        </n-flex>
      </n-space>
      <n-divider>页面功能</n-divider>
      <n-space vertical size="large">
        <n-flex align="center" justify="space-between">
          <span>页面切换动画</span>
          <n-select
            size="small"
            class="setting-input"
            aria-label="页面切换动画"
            :value="themeStore.config.pageTransition"
            :options="pageTransitionOptions"
            @update:value="handlePageTransitionChange"
          />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>显示 Logo</span>
          <n-switch :value="themeStore.getShowLogo" @update:value="themeStore.setShowLogo" />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>显示标签</span>
          <n-switch :value="themeStore.getShowTabs" @update:value="themeStore.setShowTabs" />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <n-tooltip>
            <template #trigger><span>标签页缓存</span></template>
            开启后按菜单设置保留页面状态；关闭会清除已缓存页面。
          </n-tooltip>
          <n-switch
            aria-label="标签页缓存"
            :value="themeStore.getCacheTabs"
            @update:value="themeStore.setCacheTabs"
          />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>显示标签图标</span>
          <n-switch
            :value="themeStore.getShowTabIcon"
            :disabled="!themeStore.getShowTabs"
            @update:value="themeStore.setShowTabIcon"
          />
        </n-flex>
        <n-flex align="center" justify="space-between">
          <span>标签风格</span>
          <n-select
            size="small"
            class="setting-input"
            aria-label="标签风格"
            :value="themeStore.getTabStyle"
            :options="tabStyleOptions"
            :disabled="!themeStore.getShowTabs"
            @update:value="handleTabStyleChange"
          />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>显示水印</span>
          <n-switch
            :value="themeStore.getShowWatermark"
            @update:value="themeStore.setShowWatermark"
          />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>水印文字</span>
          <n-input
            size="small"
            class="setting-input"
            :value="themeStore.getWatermarkText"
            :disabled="!themeStore.getShowWatermark"
            :maxlength="100"
            type="text"
            placeholder="水印文字"
            @update:value="themeStore.setWatermarkText"
          />
        </n-flex>
      </n-space>
      <template #footer>
        <n-button type="primary" block @click="themeStore.resetToDefault">重置主题和布局</n-button>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped lang="scss">
.layout-options {
  display: grid;
  grid-template-columns: repeat(3, 76px);
  justify-content: center;
  gap: 16px;
}

.layout-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px 6px;
  border: 1px solid var(--setting-border);
  border-radius: 6px;
  background: transparent;
  color: inherit;
  font-size: 12px;
  cursor: pointer;

  &:hover {
    border-color: var(--setting-primary);
  }

  &--active {
    border-color: var(--setting-primary);
    box-shadow: inset 0 0 0 1px var(--setting-primary);
  }

  &:focus-visible {
    outline: 2px solid var(--setting-primary);
    outline-offset: 2px;
  }
}

.layout-preview {
  display: grid;
  grid-template-columns: 14px 1fr;
  grid-template-rows: 9px 1fr;
  grid-template-areas: 'sidebar header' 'sidebar content';
  gap: 2px;
  width: 52px;
  height: 32px;

  &--mixed {
    grid-template-areas: 'header header' 'sidebar content';
  }

  &--top {
    grid-template-columns: 1fr;
    grid-template-areas: 'header' 'content';
  }
}

.preview-header,
.preview-sidebar {
  border-radius: 2px;
  background: var(--setting-primary);
}

.preview-header {
  grid-area: header;
  opacity: 0.65;
}

.preview-sidebar {
  grid-area: sidebar;
}

.preview-content {
  grid-area: content;
  border-radius: 2px;
  background: var(--setting-fill);
}

.setting-input {
  width: 200px;
}
</style>
