import { defineStore } from 'pinia'

const THEME_STORAGE_KEY = 'whim.theme'

/** 管理用户的界面主题偏好。 */
export const useThemeStore = defineStore('theme', () => {
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY)
  const isDark = ref(
    savedTheme === 'dark' ||
      (savedTheme !== 'light' && window.matchMedia('(prefers-color-scheme: dark)').matches),
  )

  /** 切换主题并保存用户选择。 */
  function toggleTheme(): void {
    isDark.value = !isDark.value
    localStorage.setItem(THEME_STORAGE_KEY, isDark.value ? 'dark' : 'light')
  }

  return { isDark, toggleTheme }
})
