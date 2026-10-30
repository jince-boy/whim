<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon'
import DictDataPanel from './components/DictDataPanel.vue'
import { useDictType } from './hooks/useDictType'

defineOptions({ name: 'SysDictType' })
const { createIcon } = useIcon()
const {
  registerTable, form, selectedType, showData, themeVars,
  canViewTypes, canResetCache, canExport, resettingCache, exporting,
  openAddDialog, removeDictTypes, handleExport, handleResetDictCache,
} = useDictType()
</script>

<template>
  <WhimTable
    v-if="canViewTypes"
    @register="registerTable"
    @add="openAddDialog"
    @delete="removeDictTypes"
  >
    <template #search>
      <n-form-item-gi label="字典名称" path="name">
        <n-input v-model:value="form.name" placeholder="请输入字典名称" :maxlength="128" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="类型编码" path="type">
        <n-input v-model:value="form.type" placeholder="请输入类型编码" :maxlength="128" clearable />
      </n-form-item-gi>
    </template>
    <template #toolbarExtra>
      <n-button
        v-if="canExport"
        type="info"
        secondary
        :loading="exporting"
        :render-icon="createIcon('download')"
        @click="handleExport"
      >导出类型</n-button>
      <n-button
        v-if="canResetCache"
        secondary
        :loading="resettingCache"
        :render-icon="createIcon('database')"
        @click="handleResetDictCache"
      >刷新缓存</n-button>
    </template>
    <template #empty>
      <n-empty description="暂无字典类型，请新增或调整查询条件" />
    </template>
  </WhimTable>
  <n-empty v-else description="暂无字典类型查询权限" />

  <n-drawer
    v-model:show="showData"
    width="min(1180px, calc(100vw - 48px))"
    placement="right"
    :mask-closable="true"
  >
    <n-drawer-content
      :closable="false"
      :body-content-style="{ padding: '10px', backgroundColor: themeVars.bodyColor }"
    >
      <template #header>
        <n-flex v-if="selectedType" align="center" :size="10">
          <n-text>{{ selectedType.name }}</n-text>
          <n-text depth="3">{{ selectedType.type }}</n-text>
        </n-flex>
      </template>
      <DictDataPanel
        v-if="selectedType"
        :key="selectedType.id + ':' + selectedType.type"
        :type="selectedType.type"
        :name="selectedType.name"
      />
    </n-drawer-content>
  </n-drawer>
</template>
