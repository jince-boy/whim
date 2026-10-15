<script setup lang="ts">
import { NEmpty, NSelect } from 'naive-ui'
import { useSelectIcon, type SelectIconProps } from './hooks/useSelectIcon'

defineOptions({ name: 'WhimSelectIcon', inheritAttrs: false })

const props = withDefaults(defineProps<SelectIconProps>(), {
  placeholder: '请选择图标',
  searchInputPlaceholder: '搜索图标名称或类名',
})
const iconValue = defineModel<string | null>({ required: true })
const { show, options, placeholder, renderLabel, updateValue } = useSelectIcon(props, iconValue)
</script>

<template>
  <NSelect
    clearable
    v-bind="$attrs"
    v-model:show="show"
    :value="iconValue"
    :options="options"
    :placeholder="placeholder"
    :render-label="renderLabel"
    :multiple="false"
    filterable
    @update:value="updateValue"
  >
    <template #empty>
      <NEmpty size="small" description="没有匹配的图标" />
    </template>
  </NSelect>
</template>
