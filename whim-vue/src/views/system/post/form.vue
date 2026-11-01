<script setup lang="ts">
import type { PostFormProps } from './hooks/types'
import { usePostForm } from './hooks/usePostForm'

const props = defineProps<PostFormProps>()
const { formRef, formModel, rules, statusOptions, departmentOptions, loading, error, canLoadDepartments, reload } = usePostForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-alert v-if="error" type="error" style="margin-bottom: 16px">
      {{ error }} <n-button text type="info" @click="reload">重新加载部门</n-button>
    </n-alert>
    <n-grid :cols="24" :x-gap="16" item-responsive>
      <n-form-item-gi label="岗位名称" path="postName" span="24 560:12">
        <n-input v-model:value="formModel.postName" :maxlength="64" placeholder="请输入岗位名称" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="岗位编码" path="postCode" span="24 560:12">
        <n-input v-model:value="formModel.postCode" :maxlength="64" placeholder="请输入唯一的岗位编码" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="所属部门" path="deptId" span="24">
        <n-tree-select v-model:value="formModel.deptId" :options="departmentOptions" :loading="loading" :disabled="!canLoadDepartments || loading || !!error" filterable clearable default-expand-all :placeholder="canLoadDepartments ? '请选择所属部门，留空表示全组织岗位' : '暂无部门查询权限'" />
      </n-form-item-gi>
      <n-form-item-gi label="显示排序" path="sort" span="24">
        <n-input-number v-model:value="formModel.sort" :min="0" :max="2147483647" :precision="0" style="width: 100%" />
      </n-form-item-gi>
      <n-form-item-gi label="状态" path="status" span="24">
        <n-radio-group v-model:value="formModel.status" aria-label="岗位状态">
          <n-space>
            <n-radio v-for="option in statusOptions" :key="option.value" :value="option.value" :label="option.label" />
          </n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" span="24">
        <n-input v-model:value="formModel.remark" type="textarea" :maxlength="255" show-count :autosize="{ minRows: 3, maxRows: 5 }" placeholder="请输入备注" />
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>
