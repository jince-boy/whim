<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useDictType } from '@/views/system/dictType/hooks/useDictType.ts'
import { useIcon } from '@/components/icon/useIcon.ts'

defineOptions({
  name: 'SysDictType',
})
const { createIcon } = useIcon()
const {
  registerTable,
  form,
  openAddDialog,
  openEditDialog,
  removeDictTypeBatch,
  handleResetDictCache,
} = useDictType()
</script>

<template>
  <n-space vertical>
    <WhimTable
      @register="registerTable"
      @add="openAddDialog"
      @edit="openEditDialog"
      @delete="removeDictTypeBatch"
    >
      <template #search>
        <n-form-item-gi label="字典名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入字典名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="字典类型" path="type">
          <n-input v-model:value="form.type" placeholder="请输入字典类型" clearable />
        </n-form-item-gi>
      </template>
      <template #toolbarExtra>
        <n-button
          v-permission="['system:dictType:reset']"
          type="error"
          :render-icon="createIcon('refresh-2')"
          @click="handleResetDictCache"
          >刷新缓存</n-button
        >
      </template>
    </WhimTable>
  </n-space>
</template>

<style scoped lang="scss"></style>
