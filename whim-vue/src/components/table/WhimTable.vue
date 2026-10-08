<script setup lang="ts">
import { useIcon } from '@/components/icon/useIcon.ts'
import type { DataTableRowKey } from 'naive-ui'
import {
  type BaseTableEmits,
  type BaseTableProps,
  useWhimTable,
} from '@/components/table/useWhimTable.ts'

defineOptions({
  name: 'WhimTable',
})

const props = withDefaults(defineProps<BaseTableProps>(), {
  addButtonShow: true,
  editButtonShow: true,
  deleteButtonShow: true,
  exportButtonShow: true,
  stripedButtonShow: true,
  borderButtonShow: true,
})
const emit = defineEmits<BaseTableEmits>()
const page = defineModel<number>('page', { default: 1 })
const pageSize = defineModel<number>('pageSize', { default: 10 })
const checkedRowKeys = defineModel<DataTableRowKey[]>('checkedRowKeys', { default: () => [] })
const expandedRowKeys = defineModel<DataTableRowKey[]>('expandedRowKeys')
const { createIcon } = useIcon()
const {
  tableRef,
  showStriped,
  showBorder,
  selectedRowKeys,
  isEditEnabled,
  isDeleteEnabled,
  paginationConfig,
  handleExpandChange,
  getRowKey,
  onUpdateCheckedRowKeys,
  handlePageChange,
  handlePageSizeChange,
  handleShowBorder,
  handleShowStriped,
  selectableColumns,
  checkedColumnKeys,
  visibleColumns,
} = useWhimTable(props, { page, pageSize, checkedRowKeys, expandedRowKeys })
</script>

<template>
  <n-card :bordered="false">
    <n-space vertical>
      <!-- 头部 -->
      <n-flex justify="space-between" align="center">
        <!-- 左侧按钮 -->
        <n-space>
          <slot name="action-buttons-left"></slot>
          <n-button
            v-permission="props.addButtonPermission"
            v-if="addButtonShow"
            type="primary"
            :render-icon="createIcon('zengjia')"
            @click="emit('add')"
          >
            新增
          </n-button>
          <n-button
            v-permission="props.editButtonPermission"
            v-if="editButtonShow"
            type="info"
            :render-icon="createIcon('xiugai')"
            @click="emit('edit', selectedRowKeys[0])"
            :disabled="!isEditEnabled"
          >
            修改
          </n-button>
          <n-button
            v-permission="props.deleteButtonPermission"
            v-if="deleteButtonShow"
            type="error"
            :render-icon="createIcon('shanchu')"
            @click="emit('delete', selectedRowKeys)"
            :disabled="!isDeleteEnabled"
          >
            删除
          </n-button>
          <n-button
            v-permission="props.exportButtonPermission"
            v-if="exportButtonShow"
            type="warning"
            :render-icon="createIcon('daochu')"
            @click="emit('export')"
          >
            导出
          </n-button>
          <!-- 右侧插槽（比如刷新按钮等） -->
          <slot name="action-buttons-right"></slot>
        </n-space>
        <n-space align="center">
          <n-flex align="center" v-if="borderButtonShow"
            ><n-text>边框</n-text><n-switch size="small" @update:value="handleShowBorder"
          /></n-flex>
          <n-divider vertical v-if="borderButtonShow" />
          <n-flex align="center" v-if="stripedButtonShow"
            ><n-text>斑马线</n-text><n-switch size="small" @update:value="handleShowStriped"
          /></n-flex>
          <n-divider vertical v-if="stripedButtonShow" />
          <n-tooltip trigger="hover">
            <template #trigger>
              <n-button quaternary circle :focusable="false" @click="emit('refresh')">
                <template #icon>
                  <n-icon :component="createIcon('refresh-2')" />
                </template>
              </n-button>
            </template>
            刷新
          </n-tooltip>
          <n-popover trigger="click" placement="bottom-end">
            <template #trigger>
              <n-tooltip trigger="hover">
                <template #trigger>
                  <n-button quaternary circle :focusable="false">
                    <template #icon>
                      <n-icon :component="createIcon('setting')" />
                    </template>
                  </n-button>
                </template>
                列设置
              </n-tooltip>
            </template>
            <template #header>
              <n-text strong depth="1"> 显示/隐藏列 </n-text>
            </template>
            <n-checkbox-group v-model:value="checkedColumnKeys" size="small">
              <n-space vertical>
                <n-checkbox
                  v-for="col in selectableColumns"
                  :key="col.key"
                  :value="col.key"
                  :label="col.title"
                />
              </n-space>
            </n-checkbox-group>
          </n-popover>
        </n-space>
      </n-flex>
      <!-- 表格 -->
      <n-data-table
        ref="tableRef"
        :columns="visibleColumns"
        :data="props.data"
        :loading="props.loading"
        :remote="true"
        :pagination="paginationConfig"
        :row-key="getRowKey"
        :bordered="showBorder"
        :single-line="!showBorder"
        :striped="showStriped"
        :checked-row-keys="selectedRowKeys"
        :default-expanded-row-keys="defaultExpandedRowKeys"
        :default-expand-all="props.defaultExpandAll"
        :expanded-row-keys="expandedRowKeys"
        @update:expanded-row-keys="handleExpandChange"
        @update:checked-row-keys="onUpdateCheckedRowKeys"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-space>
  </n-card>
</template>

<style scoped lang="scss">
:deep(.n-data-table-wrapper) {
  border-color: var(--n-merged-border-color);
}
:deep(.n-data-table-td) {
  border-color: var(--n-merged-border-color);
}
</style>
