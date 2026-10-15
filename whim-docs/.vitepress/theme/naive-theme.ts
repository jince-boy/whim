import { computed } from 'vue'
import { useData } from 'vitepress'
import { darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import tinycolor from 'tinycolor2'
import {
  darkThemeOverrides,
  defaultPrimaryColor,
  lightThemeOverrides,
} from '../../../whim-vue/src/config/NaiveTheme'

/** 复用管理端的基础主题与主色派生规则，并跟随文档站的明暗模式。 */
export function useDocsTheme() {
  const { isDark } = useData()
  const theme = computed(() => (isDark.value ? darkTheme : null))
  const overrides = computed<GlobalThemeOverrides>(() => {
    const defaults = isDark.value ? darkThemeOverrides : lightThemeOverrides
    const primary = tinycolor(defaultPrimaryColor)
    const base = isDark.value ? primary.lighten(10) : primary
    const hover = isDark.value ? base.clone().lighten(8) : base.clone().darken(5)
    return {
      ...defaults,
      common: {
        ...defaults.common,
        primaryColor: base.toHexString(),
        primaryColorHover: hover.toHexString(),
        primaryColorPressed: base.clone().darken(10).toHexString(),
        primaryColorSuppl: hover.toHexString(),
      },
    }
  })
  return { theme, overrides, isDark }
}
