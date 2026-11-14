<script setup lang="ts">
import type { DataScopeFormProps } from './hooks/types'
import { useDataScopeForm } from './hooks/useDataScopeForm'

const props = defineProps<DataScopeFormProps>()
const { formRef, formModel, rules, scopeOptions, departmentOptions, loading, error, canLoad, reload } = useDataScopeForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" :disabled="props.readonly" label-placement="top" require-mark-placement="left" @submit.prevent>
    <n-form-item label="数据范围" path="dataScope">
      <n-select v-model:value="formModel.dataScope" :options="scopeOptions" placeholder="请选择数据范围" />
    </n-form-item>
    <template v-if="formModel.dataScope === 2">
      <n-alert v-if="error" type="error" class="scope-hint">{{ error }} <n-button text type="info" @click="reload">重新加载</n-button></n-alert>
      <n-form-item label="自定义部门" path="deptIds">
        <n-tree-select v-model:value="formModel.deptIds" :options="departmentOptions" multiple checkable :cascade="false" :loading="loading" :disabled="props.readonly || !canLoad || loading || !!error" filterable clearable placeholder="请选择部门" />
      </n-form-item>
      <n-text depth="3">部门独立选择，不自动包含下级部门。仅生效中的部门及有效祖先链可保存。</n-text>
    </template>
  </n-form>
</template>

<style scoped>
.scope-hint {
  margin-bottom: 20px;
}
</style>
