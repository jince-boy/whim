<script setup lang="ts">
import { NInput, NProgress } from 'naive-ui'
import type { PasswordInputProps } from './types'
import { usePasswordStrength } from './hooks/usePasswordStrength'

defineOptions({ name: 'WhimPassword', inheritAttrs: false })
const props = defineProps<PasswordInputProps>()
const password = defineModel<string>('value', { required: true })
const { strengthId, inputProps, label, color, level, textColor } = usePasswordStrength(password, props)
</script>

<template>
  <div class="whim-password">
    <NInput
      v-model:value="password"
      v-bind="$attrs"
      type="password"
      show-password-on="click"
      :input-props="inputProps"
    />
    <div
      class="whim-password__strength"
      role="meter"
      aria-label="密码强度"
      :aria-valuemin="0"
      :aria-valuemax="5"
      :aria-valuenow="level"
      :aria-valuetext="label"
    >
      <NProgress type="line" :percentage="level * 20" :show-indicator="false" :height="4" :color="color" aria-hidden="true" />
    </div>
    <p :id="strengthId" class="whim-password__label" :style="{ color: textColor }" aria-live="polite">{{ label }}</p>
  </div>
</template>

<style scoped>
.whim-password {
  width: 100%;
  min-width: 0;
}

.whim-password__strength {
  margin-top: 8px;
}

.whim-password__label {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 18px;
}
</style>

