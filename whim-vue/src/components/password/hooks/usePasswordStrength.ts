import { computed, ref, useId, watch, type Ref } from 'vue'
import { useThemeVars } from 'naive-ui'
import type { ZxcvbnFactory } from '@zxcvbn-ts/core'
import type { PasswordInputProps } from '../types'

/** 在浏览器内评估密码，评分库按需加载，过期任务不回写当前输入。 */
export function usePasswordStrength(password: Ref<string>, props: PasswordInputProps) {
  const theme = useThemeVars()
  const strengthId = useId()
  const score = ref<number | null>(null)
  const loading = ref(false)
  const failed = ref(false)
  let estimator: Promise<ZxcvbnFactory> | undefined

  watch(
    [password, () => props.userInputs],
    ([value, userInputs], _, onCleanup) => {
      let cancelled = false
      score.value = null
      failed.value = false
      loading.value = !!value
      if (!value) return

      // 合并连续输入；评分与词典只在本地使用，不发送或记录密码。
      const timer = setTimeout(async () => {
        try {
          estimator ??= Promise.all([
            import('@zxcvbn-ts/core'),
            import('@zxcvbn-ts/language-common'),
            import('@zxcvbn-ts/language-en'),
          ]).then(([core, common, english]) => new core.ZxcvbnFactory({
            translations: english.translations,
            graphs: common.adjacencyGraphs,
            dictionary: { ...common.dictionary, ...english.dictionary },
          }))
          const instance = await estimator
          if (cancelled) return
          score.value = instance.check(value, userInputs).score
        } catch {
          estimator = undefined
          if (!cancelled) failed.value = true
        } finally {
          if (!cancelled) loading.value = false
        }
      }, 150)
      onCleanup(() => {
        cancelled = true
        clearTimeout(timer)
      })
    },
    { immediate: true, deep: true },
  )

  const label = computed(() => {
    if (loading.value) return '正在评估密码强度'
    if (failed.value) return '暂时无法评估密码强度'
    if (score.value === null) return '请输入密码以查看强度'
    return `密码强度：${['很弱', '弱', '中等', '强', '很强'][score.value]}`
  })
  const color = computed(() => {
    if (score.value === null) return theme.value.textColorDisabled
    if (score.value <= 1) return theme.value.errorColor
    if (score.value === 2) return theme.value.warningColor
    return theme.value.successColor
  })
  const inputProps = computed(() => ({
    autocomplete: 'new-password',
    ...props.inputProps,
    'aria-describedby': [props.inputProps?.['aria-describedby'], strengthId].filter(Boolean).join(' '),
  }))

  return {
    strengthId, inputProps, label, color,
    level: computed(() => score.value === null ? 0 : score.value + 1),
    textColor: computed(() => theme.value.textColor3),
  }
}

