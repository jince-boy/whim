<script setup lang="ts">
import type { RoleFormProps } from './hooks/types'
import { useRoleForm } from './hooks/useRoleForm'

const props = defineProps<RoleFormProps>()
const { formRef, formModel, rules, isEdit, statusOptions } = useRoleForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left" @submit.prevent>
    <n-grid :cols="2" :x-gap="16" item-responsive>
      <n-form-item-gi label="角色名称" path="roleName" span="2 560:1">
        <n-input v-model:value="formModel.roleName" :maxlength="32" placeholder="请输入角色名称" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="角色编码" path="roleCode" span="2 560:1">
        <n-input v-model:value="formModel.roleCode" :maxlength="128" placeholder="请输入唯一的角色编码" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="显示排序" path="sort" span="2">
        <n-input-number v-model:value="formModel.sort" :min="0" :max="2147483647" :precision="0" style="width: 100%" />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isEdit" label="状态" path="status" span="2">
        <n-radio-group v-model:value="formModel.status">
          <n-space><n-radio v-for="option in statusOptions" :key="option.value" :value="option.value" :label="option.label" /></n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" span="2">
        <n-input v-model:value="formModel.remark" type="textarea" :maxlength="255" show-count :autosize="{ minRows: 3, maxRows: 5 }" placeholder="请输入备注" />
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>
