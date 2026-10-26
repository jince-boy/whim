<script setup lang="ts">
import { useIcon } from '@/components/icon/useIcon'
import DictDataPanel from './components/DictDataPanel.vue'
import { useDictType } from './hooks/useDictType'

defineOptions({ name: 'SysDictType' })
const { createIcon } = useIcon()
const {
  types, filteredTypes, typeOptions, keyword, selectedId, selectedType, loading, error,
  managingTypes, checkedTypeIds, managementColumns, themeVars,
  canViewTypes, canAdd, canDelete, canViewData, canResetCache, canExport,
  resettingCache, exporting,
  selectType, reloadTypes, openAddDialog, removeDictTypes,
  handleExport, handleResetDictCache,
} = useDictType()
</script>

<template>
  <div
    class="dict-management"
    :style="{
      '--dict-border': themeVars.borderColor,
      '--dict-surface': themeVars.cardColor,
      '--dict-muted': themeVars.textColor3,
    }"
  >
    <div class="dict-workspace">
      <n-card class="dict-directory" size="small" :content-style="{ padding: '16px 12px 12px' }">
        <div class="dict-directory__heading">
          <h2>字典类型 <n-text depth="3" class="dict-directory__count">{{ types.length }}</n-text></h2>
          <n-button
            v-if="canAdd"
            text
            type="primary"
            aria-label="新增字典类型"
            title="新增字典类型"
            :render-icon="createIcon('plus', 18)"
            @click="openAddDialog"
          />
        </div>
        <div class="dict-directory__desktop">
          <n-input
            v-model:value="keyword"
            placeholder="搜索名称或编码"
            aria-label="搜索字典类型名称或编码"
            clearable
            :maxlength="128"
          >
            <template #prefix><component :is="createIcon('search', 16)" /></template>
          </n-input>
          <n-spin :show="loading" class="dict-directory__list">
            <n-empty v-if="!canViewTypes" description="暂无类型查询权限" class="dict-directory__empty" />
            <div v-else-if="error && !types.length" class="dict-directory__empty">
              <n-empty description="类型加载失败">
                <template #extra><n-button size="small" @click="reloadTypes()">重试</n-button></template>
              </n-empty>
            </div>
            <n-scrollbar v-else-if="filteredTypes.length" style="max-height: max(320px, calc(100dvh - 370px))">
              <nav class="dict-directory__items" aria-label="字典类型目录">
                <n-button
                  v-for="item in filteredTypes"
                  :key="item.id"
                  class="dict-directory__item"
                  :type="selectedId === item.id ? 'primary' : 'default'"
                  :secondary="selectedId === item.id"
                  :quaternary="selectedId !== item.id"
                  :aria-pressed="selectedId === item.id"
                  :title="item.name + ' · ' + item.type"
                  @click="selectType(item.id)"
                >
                  <span class="dict-directory__item-content">
                    <span class="dict-directory__item-name">{{ item.name }}</span>
                    <span class="dict-directory__item-code">{{ item.type }}</span>
                  </span>
                  <component :is="createIcon('chevron-right', 14)" v-if="selectedId === item.id" />
                </n-button>
              </nav>
            </n-scrollbar>
            <n-empty
              v-else-if="!loading"
              :description="keyword.trim() ? '没有匹配的类型' : '暂无字典类型'"
              class="dict-directory__empty"
            >
              <template v-if="keyword.trim()" #extra>
                <n-button text type="primary" @click="keyword = ''">清空搜索</n-button>
              </template>
            </n-empty>
          </n-spin>
        </div>
        <div class="dict-directory__mobile">
          <n-select
            :value="selectedId"
            :options="typeOptions"
            :loading="loading"
            filterable
            placeholder="搜索并选择字典类型"
            aria-label="选择字典类型"
            @update:value="selectType"
          />
        </div>
        <footer class="dict-directory__footer">
          <n-text depth="3">{{ keyword.trim() ? filteredTypes.length + ' / ' : '' }}{{ types.length }} 个类型</n-text>
          <n-button text :disabled="!canViewTypes" @click="managingTypes = true">管理类型</n-button>
        </footer>
      </n-card>

      <section class="dict-content" aria-label="字典数据工作区">
        <n-alert v-if="error" type="error" class="dict-content__error" :show-icon="false">
          <n-flex align="center" justify="space-between">
            <span>字典类型加载失败，请刷新重试</span>
            <n-button size="small" :loading="loading" @click="reloadTypes()">重新加载</n-button>
          </n-flex>
        </n-alert>
        <template v-if="selectedType">
          <DictDataPanel
            v-if="canViewData"
            :key="selectedType.id + ':' + selectedType.type"
            :type="selectedType.type"
            :name="selectedType.name"
          />
          <div v-else class="dict-content__empty">
            <n-empty description="暂无字典数据查询权限" />
          </div>
        </template>
        <div v-else class="dict-content__empty">
          <n-spin v-if="loading" description="正在加载字典类型" />
          <n-empty v-else :description="types.length ? '请选择字典类型' : '从一个字典类型开始'">
            <template #icon><component :is="createIcon('book-open', 42)" /></template>
            <template #extra>
              <n-space vertical align="center" :size="20">
                <n-text depth="3">创建类型后，在这里集中维护它的标签、值和展示样式</n-text>
                <n-button v-if="canAdd" type="primary" :render-icon="createIcon('plus')" @click="openAddDialog">新增字典类型</n-button>
              </n-space>
            </template>
          </n-empty>
        </div>
      </section>
    </div>

    <n-drawer v-model:show="managingTypes" width="min(820px, 100vw)" placement="right">
      <n-drawer-content title="管理字典类型" closable>
        <n-space vertical :size="20">
          <n-text depth="3">管理类型信息；选择“查看数据”即可回到对应的数据工作区。</n-text>
          <n-input v-model:value="keyword" placeholder="搜索类型名称或编码" aria-label="筛选管理列表" clearable :maxlength="128">
            <template #prefix><component :is="createIcon('search', 16)" /></template>
          </n-input>
          <n-flex justify="space-between" align="center">
            <n-flex :size="8">
              <n-button v-if="canAdd" type="primary" :render-icon="createIcon('plus')" @click="openAddDialog">新增类型</n-button>
              <n-button v-if="canDelete" type="error" secondary :render-icon="createIcon('trash-2')" :disabled="!checkedTypeIds.length" @click="removeDictTypes(checkedTypeIds)">删除选中{{ checkedTypeIds.length ? '（' + checkedTypeIds.length + '）' : '' }}</n-button>
            </n-flex>
            <n-flex :size="8">
              <n-button v-if="canExport" type="info" secondary :loading="exporting" :render-icon="createIcon('download')" @click="handleExport()">导出全部类型</n-button>
              <n-button v-if="canResetCache" :loading="resettingCache" :render-icon="createIcon('database')" @click="handleResetDictCache">刷新缓存</n-button>
              <n-button aria-label="刷新类型目录" :loading="loading" :render-icon="createIcon('refresh-cw')" @click="reloadTypes()" />
            </n-flex>
          </n-flex>
          <n-alert v-if="error" type="error">类型加载失败，请刷新重试</n-alert>
          <n-data-table
            v-model:checked-row-keys="checkedTypeIds"
            :columns="managementColumns"
            :data="filteredTypes"
            :loading="loading"
            :row-key="(row: { id: string }) => row.id"
            :scroll-x="642"
            :pagination="{ pageSize: 10 }"
          />
        </n-space>
        <template #footer><n-text depth="3">共 {{ types.length }} 个字典类型</n-text></template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.dict-management {
  /* 目录项与默认尺寸的数据行共用高度。 */
  --dict-row-height: 53px;
  min-width: 0;
}
.dict-workspace {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  align-items: stretch;
}
.dict-directory {
  min-width: 0;
  border-radius: 8px;
}
.dict-directory :deep(> .n-card-content) {
  display: flex;
  flex-direction: column;
}
.dict-directory__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 4px 16px;
}
.dict-directory__heading h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}
.dict-directory__count {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 400;
}
.dict-directory__desktop {
  flex: 1;
}
.dict-directory__list {
  margin-top: 12px;
  min-height: 360px;
}
.dict-directory__items {
  display: flex;
  flex-direction: column;
}
.dict-directory__item {
  width: 100%;
  height: var(--dict-row-height);
  padding: 6px 12px;
}
.dict-directory__item :deep(.n-button__content) {
  width: 100%;
  min-width: 0;
  justify-content: space-between;
  gap: 8px;
}
.dict-directory__item-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  min-width: 0;
}
.dict-directory__item-name,
.dict-directory__item-code {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dict-directory__item-name {
  font-size: 14px;
  font-weight: 500;
  line-height: 16px;
}
.dict-directory__item-code {
  font-size: 11px;
  line-height: 14px;
  color: var(--dict-muted);
  font-family: var(--app-font-family-mono);
}
.dict-directory__empty {
  padding: 64px 0;
}
.dict-directory__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 14px 4px 2px;
  margin-top: 16px;
  border-top: 1px solid var(--dict-border);
  font-size: 12px;
}
.dict-directory__footer .n-button {
  font-size: 12px;
}
.dict-directory__mobile {
  display: none;
}
.dict-content {
  min-width: 0;
  min-height: 580px;
  background: var(--dict-surface);
  border: 1px solid var(--dict-border);
  border-radius: 8px;
  overflow: hidden;
}
.dict-content__error {
  margin: 16px;
}
.dict-content__empty {
  min-height: 530px;
  padding: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}
.dict-content__empty :deep(.n-empty__icon) {
  width: 42px;
  height: 42px;
  margin-bottom: 20px;
}
.dict-content__empty :deep(.n-empty__description) {
  font-size: 16px;
}
@media (max-width: 900px) {
  .dict-workspace {
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
  }
  .dict-directory__desktop {
    display: none;
  }
  .dict-directory__mobile {
    display: block;
  }
  .dict-directory__heading {
    margin-bottom: 12px;
  }
  .dict-directory__footer {
    margin-top: 12px;
    padding-top: 12px;
  }
  .dict-content {
    min-height: 420px;
  }
  .dict-content__empty {
    min-height: 340px;
    padding: 24px;
  }
}
</style>
