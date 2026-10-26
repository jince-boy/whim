<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon'
import { useDictData } from './hooks/useDictData'
import type { DictDataProps } from './hooks/types'

defineOptions({ name: 'SysDictData' })
const props = defineProps<DictDataProps>()
const { createIcon } = useIcon()
const {
  registerTable,
  form,
  type,
  canExport,
  exporting,
  openAddDialog,
  removeDictData,
  handleExport,
  backDictTypePage,
} = useDictData(props)
</script>

<template>
  <section class="dict-values" aria-label="字典值">
    <WhimTable
      v-if="type"
      @register="registerTable"
      @add="openAddDialog"
      @delete="removeDictData"
    >
      <template #search>
        <n-form-item-gi label="字典标签" path="label" span="1 400:2">
          <n-input
            v-model:value="form.label"
            placeholder="输入完整字典标签"
            :maxlength="128"
            clearable
          />
        </n-form-item-gi>
      </template>
      <template v-if="!props.embedded" #toolbarBefore>
        <n-button :render-icon="createIcon('arrow-left')" @click="backDictTypePage">
          返回字典类型
        </n-button>
      </template>
      <template #toolbarExtra>
        <n-button
          v-if="canExport"
          :loading="exporting"
          :render-icon="createIcon('download')"
          @click="handleExport"
        >
          导出
        </n-button>
      </template>
      <template #empty>
        <n-empty description="暂无字典值，请新增或调整查询条件" />
      </template>
    </WhimTable>
    <n-result
      v-else
      status="info"
      title="尚未选择字典类型"
      description="选择字典类型后即可维护对应的字典值"
    >
      <template #footer>
        <n-button :render-icon="createIcon('arrow-left')" @click="backDictTypePage">
          返回字典类型
        </n-button>
      </template>
    </n-result>
  </section>
</template>

<style scoped>
.dict-values {
  min-width: 0;
}
</style>
