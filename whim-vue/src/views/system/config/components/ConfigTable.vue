<script setup lang="ts">
import { WhimTable } from '@/components/table'
import Icon from '@/components/icon/Icon.vue'
import { useConfig } from '../hooks/useConfig'

const props = defineProps<{ builtIn: boolean }>()
const { registerTable, form, searchFields, openAddDialog } = useConfig(props.builtIn)
</script>

<template>
  <WhimTable @register="registerTable" @add="openAddDialog">
    <template #search>
      <n-form-item-gi v-for="field in searchFields" :key="field.key" :label="field.label" :path="field.key">
        <n-input v-model:value="form[field.key]" :placeholder="'请输入' + field.label" :maxlength="field.maxLength" clearable />
      </n-form-item-gi>
    </template>
    <template v-if="props.builtIn" #toolbarBefore>
      <n-flex align="center" :size="6"><Icon name="lock-keyhole" /><n-text depth="3">系统内置配置，仅可修改配置值</n-text></n-flex>
    </template>
    <template #empty><n-empty description="暂无符合条件的配置" /></template>
  </WhimTable>
</template>
