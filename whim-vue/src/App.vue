<script setup lang="ts">
import { useThemeStore } from '@/stores/modules/theme.ts'
import { onBeforeUnmount, onMounted, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import systemSetting from '@/config/SystemSetting'
import { refreshSiteSettings } from '@/views/system/config/hooks/useSiteSettings'
import { zhCN, dateZhCN } from 'naive-ui'
import hljs from 'highlight.js/lib/core'
import json from 'highlight.js/lib/languages/json'

const themeStore = useThemeStore()
const route = useRoute()
watchEffect(() => {
  document.title = route.meta.title ? `${systemSetting.title}-${route.meta.title}` : systemSetting.title
})
const systemTheme = window.matchMedia('(prefers-color-scheme: dark)')

/** 在系统外观变化时同步实际主题。 */
function handleSystemThemeChange(event: MediaQueryListEvent): void {
  themeStore.syncSystemTheme(event.matches)
}

onMounted(() => {
  // 接口异常由公共请求层提示；部署时的标题和页脚仍可用于登录。
  void refreshSiteSettings().catch(() => {})
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
    <n-notification-provider>
      <n-modal-provider>
        <n-dialog-provider>
          <router-view />
        </n-dialog-provider>
      </n-modal-provider>
    </n-notification-provider>
    <n-global-style />
  </n-config-provider>
</template>
