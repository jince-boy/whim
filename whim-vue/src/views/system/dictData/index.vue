<script setup lang="ts">
import { type DictDataProps, useDictData } from '@/views/system/dictData/hooks/useDictData.ts'
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon.ts'

defineOptions({
  name: 'SysDictData',
})

const props = defineProps<DictDataProps>()
const { createIcon } = useIcon()
const {
  registerTable,
  form,
  openAddDialog,
  openEditDialog,
  removeDictDataBatch,
  backDictTypePage,
  exportExcel,
} = useDictData(props)
</script>

<template>
  <n-space vertical>
    <WhimTable
      @register="registerTable"
      @add="openAddDialog"
      @edit="openEditDialog"
      @delete="removeDictDataBatch"
      @export="exportExcel"
    >
      <template #search>
        <n-form-item-gi label="字典标签" path="label">
          <n-input v-model:value="form.label" placeholder="请输入字典标签" clearable />
        </n-form-item-gi>
      </template>
      <template #toolbarExtra>
        <n-button type="error" @click="backDictTypePage" :render-icon="createIcon('fanhui')">
          返回
        </n-button>
      </template>
    </WhimTable>
  </n-space>
</template>

<style scoped lang="scss"></style>
