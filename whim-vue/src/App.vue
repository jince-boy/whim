<script setup lang="ts">
import { useThemeStore } from '@/stores/modules/theme.ts'
import { onBeforeUnmount, onMounted } from 'vue'
import { zhCN, dateZhCN } from 'naive-ui'
import hljs from 'highlight.js/lib/core'
import json from 'highlight.js/lib/languages/json'

const themeStore = useThemeStore()
const systemTheme = window.matchMedia('(prefers-color-scheme: dark)')

/** 在系统外观变化时同步实际主题。 */
function handleSystemThemeChange(event: MediaQueryListEvent): void {
  themeStore.syncSystemTheme(event.matches)
}

onMounted(() => {
  themeStore.syncSystemTheme(systemTheme.matches)
  systemTheme.addEventListener('change', handleSystemThemeChange)
})
onBeforeUnmount(() => {
  systemTheme.removeEventListener('change', handleSystemThemeChange)
})

hljs.registerLanguage('json', json)
</script>

<template>
  <n-config-provider
    :locale="zhCN"
    :date-locale="dateZhCN"
    :theme="themeStore.getTheme"
    :theme-overrides="themeStore.getThemeOverrides"
    :hljs="hljs"
  >
    <!--加载条-->
    <n-loading-bar-provider>
      <!--信息-->
      <n-message-provider>
        <!--通知-->
        <n-notification-provider>
          <n-modal-provider>
            <!--对话框-->
            <n-dialog-provider>
              <router-view />
            </n-dialog-provider>
          </n-modal-provider>
        </n-notification-provider>
      </n-message-provider>
    </n-loading-bar-provider>
    <!--全局样式-->
    <n-global-style />
  </n-config-provider>
</template>

<style scoped></style>
