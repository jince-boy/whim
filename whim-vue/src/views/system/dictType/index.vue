<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon'
import DictData from '@/views/system/dictData/index.vue'
import { useDictType } from './hooks/useDictType'

defineOptions({ name: 'SysDictType' })
const { createIcon } = useIcon()
const {
  registerTable,
  form,
  loading,
  error,
  selectedType,
  themeVars,
  canViewData,
  canResetCache,
  canExport,
  resettingCache,
  exporting,
  openAddDialog,
  removeDictTypeBatch,
  handleExport,
  handleResetDictCache,
} = useDictType()
</script>

<template>
  <div
    class="dict-management"
    :style="{
      '--dict-primary': themeVars.primaryColor,
      '--dict-header-background': themeVars.tableHeaderColor,
    }"
  >
    <section class="dict-types" aria-label="字典类型">
      <WhimTable
        @register="registerTable"
        @add="openAddDialog"
        @delete="removeDictTypeBatch"
      >
        <template #search>
          <n-form-item-gi label="字典名称" path="name">
            <n-input v-model:value="form.name" placeholder="输入完整字典名称" :maxlength="64" clearable />
          </n-form-item-gi>
          <n-form-item-gi label="类型编码" path="type">
            <n-input v-model:value="form.type" placeholder="输入完整类型编码" :maxlength="128" clearable />
          </n-form-item-gi>
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
          <n-button
            v-if="canResetCache"
            title="刷新字典缓存"
            aria-label="刷新字典缓存"
            style="width: 34px; padding: 0"
            :loading="resettingCache"
            :render-icon="createIcon('database')"
            @click="handleResetDictCache"
          />
        </template>
        <template #empty>
          <n-empty description="暂无字典类型，请新增或调整查询条件" />
        </template>
      </WhimTable>
    </section>

    <DictData
      v-if="selectedType && canViewData"
      :key="`${selectedType.id}:${selectedType.type}`"
      :type="selectedType.type"
      :name="selectedType.name"
      embedded
    />
    <n-card
      v-else
      class="dict-placeholder"
      title="字典值"
      size="small"
      :segmented="{ content: true }"
    >
      <n-spin :show="loading">
        <n-empty
          :description="
            !canViewData
              ? '暂无字典值查询权限'
              : error
                ? '字典类型加载失败，请在左侧重试'
                : loading
                  ? '正在加载字典类型'
                  : '请先在左侧选择或新增字典类型'
          "
        />
      </n-spin>
    </n-card>
  </div>
</template>

<style scoped>
.dict-management {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: start;
  gap: 16px;
}

.dict-types,
.dict-placeholder {
  min-width: 0;
}

.dict-types :deep(.dict-type-name),
.dict-types :deep(.dict-type-name .n-button__content) {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dict-types :deep(.dict-type-code) {
  font-family: var(--app-font-family-mono);
}

.dict-types :deep(.dict-type-active td) {
  background: color-mix(in srgb, var(--dict-primary) 9%, var(--n-td-color)) !important;
}

.dict-types :deep(.dict-type-active td:first-child) {
  box-shadow: inset 3px 0 var(--dict-primary);
}

.dict-placeholder {
  align-self: stretch;
  border-radius: 8px;
  overflow: hidden;
}

.dict-placeholder > :deep(.n-card-header) {
  padding: 16px;
  font-size: 14px;
  background: var(--dict-header-background);
}

.dict-placeholder > :deep(.n-card-content) {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

@media (max-width: 1199px) {
  .dict-management {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
