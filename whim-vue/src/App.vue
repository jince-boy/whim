<script setup lang="ts">
import { darkTheme, dateZhCN, zhCN, type GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const fontOverrides = {
  fontFamily: 'var(--app-font-sans)',
  fontFamilyMono: 'var(--app-font-mono)',
  fontWeightStrong: '600',
}
const lightThemeOverrides: GlobalThemeOverrides = {
  common: fontOverrides,
  Layout: { colorEmbedded: '#f0f2f5', footerColor: '#fff' },
}
const darkThemeOverrides: GlobalThemeOverrides = {
  common: fontOverrides,
}
</script>

<template>
  <n-config-provider
    :theme="themeStore.isDark ? darkTheme : null"
    :theme-overrides="themeStore.isDark ? darkThemeOverrides : lightThemeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <n-global-style />
    <n-loading-bar-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-message-provider>
            <n-modal-provider>
              <router-view />
            </n-modal-provider>
          </n-message-provider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>
