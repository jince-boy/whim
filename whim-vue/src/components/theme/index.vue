<script setup lang="ts">
import { useThemeStore } from '@/stores/modules/theme.ts'
import { lightTheme, useThemeVars } from 'naive-ui'
defineOptions({
  name: 'themeComponent',
})

const themeStore = useThemeStore()
const themeVars = useThemeVars()
const themeShowModal = defineModel<boolean>({ required: true })

const options = [
  {
    label: '按钮风格',
    value: 'button',
  },
  {
    label: '标签风格',
    value: 'tag',
  },
]

const handleTabChange = (value: string | number) => {
  switch (value) {
    case 'light':
      themeStore.setLightTheme()
      break
    case 'dark':
      themeStore.setDarkTheme()
      break
    case 'system':
      themeStore.setSystemTheme()
      break
  }
}
</script>

<template>
  <n-drawer v-model:show="themeShowModal" :width="360">
    <n-drawer-content title="主题配置" closable>
      <n-divider>主题模式</n-divider>
      <n-tabs
        type="segment"
        animated
        :value="themeStore.getTheme === lightTheme ? 'light' : 'dark'"
        @update:value="handleTabChange"
      >
        <n-tab name="light">
          <i class="iconfont icon-taiyang1"></i>
        </n-tab>
        <n-tab name="dark">
          <i class="iconfont icon-yueliang"></i>
        </n-tab>
      </n-tabs>
      <n-divider>主题颜色</n-divider>
      <n-space vertical size="large">
        <n-flex>
          <n-color-picker
            default-value="#18A058FF"
            :value="themeVars.primaryColor"
            @update:value="themeStore.setThemeColor"
            :show-alpha="false"
          />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>暗色菜单</span>
          <n-switch @update:value="themeStore.setMenuInverted" />
        </n-flex>
      </n-space>
      <n-divider>页面功能</n-divider>
      <n-space vertical size="large">
        <n-flex justify="space-between" align="center">
          <span>显示Logo</span>
          <n-switch :value="themeStore.getShowLogo" @update:value="themeStore.setShowLogo" />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>显示标签</span>
          <n-switch :value="themeStore.getShowTabs" @update:value="themeStore.setShowTabs" />
        </n-flex>
        <n-flex justify="space-between" align="center">
          <span>显示标签图标</span>
          <n-switch :value="themeStore.getShowTabIcon" @update:value="themeStore.setShowTabIcon" />
        </n-flex>
        <n-space align="center" justify="space-between">
          <span>标签风格</span>
          <n-select
            size="small"
            style="width: 200px"
            :value="themeStore.getTabStyle"
            :options="options"
            @update:value="themeStore.setTabStyle"
          />
        </n-space>
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
            style="width: 200px"
            :value="themeStore.getWatermarkText"
            @update:value="themeStore.setWatermarkText"
            type="text"
            placeholder="水印文字"
          />
        </n-flex>
      </n-space>
      <n-button
        type="primary"
        size="small"
        style="width: 100%; margin-top: 50px"
        @click="themeStore.resetToDefault"
        >重置主题</n-button
      >
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped lang="scss"></style>
