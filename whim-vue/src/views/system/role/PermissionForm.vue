<script setup lang="ts">
import type { PermissionFormProps } from './hooks/types'
import { usePermissionForm } from './hooks/usePermissionForm'

const props = defineProps<PermissionFormProps>()
const { formRef, formModel, pattern, options, rules, allSelected, partlySelected, selectAll } = usePermissionForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" :disabled="props.readonly" label-placement="top" @submit.prevent>
    <div class="permission-controls">
      <n-input v-model:value="pattern" :disabled="false" placeholder="搜索菜单或按钮" clearable />
      <n-checkbox :checked="allSelected" :indeterminate="partlySelected" :disabled="props.readonly" @update:checked="selectAll">全选</n-checkbox>
    </div>
    <n-form-item path="permissionIds" :show-label="false">
      <div class="permission-tree">
        <n-tree v-model:checked-keys="formModel.permissionIds" :data="options" :pattern="pattern" checkable :cascade="false" :selectable="false" :disabled="props.readonly" block-line default-expand-all />
        <n-empty v-if="!options.length" description="暂无菜单权限" />
      </div>
    </n-form-item>
  </n-form>
</template>

<style scoped>
.permission-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
.permission-controls > .n-input {
  flex: 1;
  min-width: 0;
}
.permission-controls > .n-checkbox {
  flex-shrink: 0;
}
.permission-tree {
  width: 100%;
  max-height: 50dvh;
  overflow: auto;
}
</style>
