<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon'
import { useDictData } from '@/views/system/dictData/hooks/useDictData'
import type { DictDataProps } from '@/views/system/dictData/hooks/types'

const props = defineProps<DictDataProps>()
const { createIcon } = useIcon()
const { registerTable, form, canAdd, canExport, exporting, openAddDialog, removeDictData, handleExport } = useDictData(props)
</script>

<template>
  <div class="dict-data-panel">
    <WhimTable @register="registerTable" @delete="removeDictData">
      <template #search>
        <n-form-item-gi label="字典标签" path="label">
          <n-input v-model:value="form.label" placeholder="输入完整字典标签" :maxlength="128" clearable />
        </n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-button v-if="canAdd" type="primary" :render-icon="createIcon('plus')" @click="openAddDialog">新增数据</n-button>
      </template>
      <template #toolbarExtra>
        <n-button v-if="canExport" type="info" secondary :loading="exporting" :render-icon="createIcon('download')" @click="handleExport">导出数据</n-button>
      </template>
      <template #empty><n-empty description="暂无字典数据，请新增或调整查询条件" /></template>
    </WhimTable>
  </div>
</template>

<style scoped>
.dict-data-panel {
  min-width: 0;
  padding-bottom: 8px;
}
.dict-data-panel :deep(.n-data-table-td) {
  box-sizing: border-box;
  height: var(--dict-row-height);
}
.dict-data-panel :deep(.whim-table:not(:fullscreen)) {
  gap: 0;
}
.dict-data-panel :deep(.whim-table:not(:fullscreen) .search-form__card),
.dict-data-panel :deep(.whim-table:not(:fullscreen) .whim-table__card) {
  border: 0;
  border-radius: 0;
}
.dict-data-panel
  :deep(.whim-table:not(:fullscreen) .whim-table__card > .n-card-header) {
  background: transparent;
  padding-top: 4px;
}
</style>
