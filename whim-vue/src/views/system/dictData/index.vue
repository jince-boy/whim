<script setup lang="ts">
import { type DictDataProps, useDictData } from '@/views/system/dictData/hooks/useDictData.ts'
import SearchForm from '@/components/form/SearchForm.vue'
import WhimTable from '@/components/table/WhimTable.vue'
import { useIcon } from '@/components/icon/useIcon.ts'

defineOptions({
  name: 'SysDictData',
})

const props = defineProps<DictDataProps>()
const { createIcon } = useIcon()
const {
  form,
  handleReset,
  pagination,
  tableColumns,
  tableData,
  loading,
  getDictDataPage,
  openAddDialog,
  openEditDialog,
  removeDictDataBatch,
  backDictTypePage,
  exportExcel,
} = useDictData(props)
</script>

<template>
  <n-space vertical>
    <SearchForm
      :model="form"
      :show-feedback="false"
      label-placement="left"
      @submit="getDictDataPage()"
      @reset="handleReset"
    >
      <template #form>
        <n-form-item-gi label="字典标签" path="form.label">
          <n-input v-model:value="form.label" placeholder="请输入字典标签" clearable />
        </n-form-item-gi>
      </template>
    </SearchForm>
    <WhimTable
      :columns="tableColumns"
      :data="tableData"
      :pagination="pagination"
      v-model:page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :loading="loading"
      :add-button-permission="['system:dictData:add']"
      :edit-button-permission="['system:dictData:edit']"
      :delete-button-permission="['system:dictData:delete']"
      :export-button-permission="['system:dictData:export']"
      @refresh="getDictDataPage"
      @update:page="getDictDataPage"
      @update:page-size="getDictDataPage"
      @add="openAddDialog"
      @edit="openEditDialog"
      @delete="removeDictDataBatch"
      @export="exportExcel"
    >
      <template #action-buttons-right>
        <n-button type="error" @click="backDictTypePage" :render-icon="createIcon('fanhui')">
          返回
        </n-button>
      </template>
    </WhimTable>
  </n-space>
</template>

<style scoped lang="scss"></style>
