<script setup lang="ts">
import LayoutSidebar from '@/layout/components/sidebar/index.vue'
import tarBar from '@/layout/components/tabbar/index.vue'
import appMain from '@/layout/components/appMain/index.vue'
import LayoutHeader from '@/layout/components/header/index.vue'
import LayoutFooter from '@/layout/components/footer/index.vue'
import screenfull from 'screenfull'
import { useThemeStore } from '@/stores/modules/theme.ts'

defineOptions({
  name: 'LayoutIndex',
})

const themeStore = useThemeStore()

// tab-bar 调用内容全屏的方法
const appMainRef = useTemplateRef<InstanceType<typeof appMain>>('appMainRef')

const toggleFullScreen = () => {
  const el = appMainRef.value?.appMainRef
  if (el && screenfull.isEnabled) {
    screenfull.toggle(el)
  }
}

// 刷新组件
const refreshAppMain = () => {
  appMainRef.value?.refresh()
}
// 折叠状态
const collapsed = ref(false)
// 监听折叠状态
const handleCollapsed = (val: boolean) => {
  collapsed.value = val
}
</script>

<template>
  <n-el>
    <n-watermark
      v-if="themeStore.getShowWatermark"
      :content="themeStore.getWatermarkText"
      cross
      fullscreen
      :font-size="18"
      :line-height="16"
      :width="300"
      :height="300"
      :x-offset="12"
      :y-offset="60"
      :rotate="-15"
    />
    <n-layout has-sider position="absolute">
      <n-layout-sider
        :width="240"
        bordered
        :native-scrollbar="false"
        show-trigger="arrow-circle"
        :collapsed-width="64"
        collapse-mode="width"
        @update:collapsed="handleCollapsed"
        :inverted="themeStore.getMenuInverted"
      >
        <layout-sidebar :collapsed="collapsed"></layout-sidebar>
      </n-layout-sider>
      <n-layout>
        <n-layout-header bordered style="height: 50px" position="absolute">
          <layout-header></layout-header>
        </n-layout-header>
        <n-layout-content
          position="absolute"
          style="top: 50px; bottom: 50px"
          :native-scrollbar="false"
          embedded
        >
          <tar-bar
            @full-screen="toggleFullScreen"
            @refresh="refreshAppMain"
            v-if="themeStore.getShowTabs"
          ></tar-bar>
          <app-main ref="appMainRef"></app-main>
        </n-layout-content>
        <n-layout-footer bordered position="absolute" style="height: 50px">
          <layout-footer></layout-footer>
        </n-layout-footer>
      </n-layout>
    </n-layout>
  </n-el>
</template>

<style scoped lang="scss"></style>
