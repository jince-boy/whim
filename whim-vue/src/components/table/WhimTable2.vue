<script setup lang="ts">
import type {
  ButtonType,
  WhimDataTableEmits,
  WhimDataTableProps,
} from '@/components/table/useWhimTable2.ts'
import { useWhimTable } from '@/components/table/useWhimTable2.ts'
import { useIcon } from '@/components/icon/useIcon.ts'
import type { DataTableRowKey } from 'naive-ui'

defineOptions({
  name: 'WhimTable',
  inheritAttrs: false,
})

const props = withDefaults(defineProps<WhimDataTableProps>(), {
  visibleButtons: () => ['add', 'edit', 'delete', 'export'] as ButtonType[],
  visibleBorderSwitch: true,
  visibleStripedSwitch: true,
})

const emit = defineEmits<WhimDataTableEmits>()

const page = defineModel<number>('page', { default: 1 })
const pageSize = defineModel<number>('pageSize', { default: 10 })
const checkedRowKeys = defineModel<DataTableRowKey[]>('checkedRowKeys', { default: () => [] })
const expandedRowKeys = defineModel<DataTableRowKey[]>('expandedRowKeys')
const bordered = defineModel<boolean>('bordered', { default: false })
const striped = defineModel<boolean>('striped', { default: false })
const {
  tableRef,
  tableProps,
  paginationConfig,
  handleCheck,
  tableColumns,
  selectTableColumns,
  checkedColumnKeys,
  handlePageChange,
  handlePageSizeChange,
} = useWhimTable(props, { page, pageSize, checkedRowKeys, expandedRowKeys, bordered, striped })
const { createIcon } = useIcon()
</script>

<template>
  <n-card :bordered="false">
    <n-space vertical>
      <n-flex justify="space-between" align="center">
        <n-space>
          <!-- 左侧按钮插槽 -->
          <slot name="action-buttons-left"></slot>
          <n-button
            v-permission="tableProps.buttonPermission?.add"
            v-role="tableProps.buttonRole?.add"
            v-if="tableProps.visibleButtons!.includes('add')"
            type="primary"
            :render-icon="createIcon('zengjia')"
            @click="emit('add')"
          >
            新增
          </n-button>
          <n-button
            v-permission="tableProps.buttonPermission?.edit"
            v-role="tableProps.buttonRole?.edit"
            v-if="tableProps.visibleButtons!.includes('edit')"
            type="info"
            :disabled="checkedRowKeys.length !== 1"
            :render-icon="createIcon('xiugai')"
            @click="emit('edit', checkedRowKeys[0])"
          >
            修改
          </n-button>
          <n-button
            v-permission="tableProps.buttonPermission?.delete"
            v-role="tableProps.buttonRole?.delete"
            v-if="tableProps.visibleButtons!.includes('delete')"
            type="error"
            :disabled="checkedRowKeys.length === 0"
            :render-icon="createIcon('shanchu')"
            @click="emit('delete', checkedRowKeys)"
          >
            删除
          </n-button>
          <n-button
            v-permission="tableProps.buttonPermission?.export"
            v-role="tableProps.buttonRole?.export"
            v-if="tableProps.visibleButtons!.includes('export')"
            type="warning"
            :render-icon="createIcon('daochu')"
            @click="emit('export', checkedRowKeys)"
          >
            导出
          </n-button>
          <!-- 右侧按钮插槽 -->
          <slot name="action-buttons-right"></slot>
        </n-space>
        <n-space align="center">
          <n-flex align="center" v-if="tableProps.visibleBorderSwitch"
            ><n-text>边框</n-text><n-switch size="small" v-model:value="bordered"
          /></n-flex>
          <n-divider vertical v-if="tableProps.visibleBorderSwitch" />
          <n-flex align="center" v-if="tableProps.visibleStripedSwitch"
            ><n-text>斑马线</n-text><n-switch size="small" v-model:value="striped"
          /></n-flex>
          <n-divider vertical v-if="tableProps.visibleStripedSwitch" />
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
                  v-for="col in selectTableColumns"
                  :key="col.key"
                  :value="col.key"
                  :label="col.title"
                />
              </n-space>
            </n-checkbox-group>
          </n-popover>
        </n-space>
      </n-flex>
      <n-data-table
        ref="tableRef"
        v-bind="{ remote: true, size: 'small', ...$attrs, ...tableProps }"
        :pagination="paginationConfig"
        :columns="tableColumns"
        :bordered="bordered"
        :single-line="!bordered"
        :striped="striped"
        :checked-row-keys="checkedRowKeys"
        v-model:expanded-row-keys="expandedRowKeys"
        @update:checked-row-keys="handleCheck"
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
