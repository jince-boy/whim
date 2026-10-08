<script setup lang="ts">
import { useThemeStore } from '@/stores/modules/theme.ts'
import { NThemeEditor, zhCN, dateZhCN } from 'naive-ui'
import hljs from 'highlight.js/lib/core'
import json from 'highlight.js/lib/languages/json'

const themeStore = useThemeStore()

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
    <n-theme-editor>
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
    </n-theme-editor>
  </n-config-provider>
</template>

<style scoped></style>
