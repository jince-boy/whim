<script setup lang="ts">
import type { RoleFormProps } from './hooks/types'
import { useRoleForm } from './hooks/useRoleForm'

const props = defineProps<RoleFormProps>()
const { formRef, formModel, rules, options } = useRoleForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top">
    <n-alert type="info" style="margin-bottom: 16px">角色决定功能权限与数据范围。灰色角色仅供查看；角色变更后，该用户需要重新登录。</n-alert>
    <n-form-item label="用户角色" path="roleIds">
      <n-checkbox-group v-model:value="formModel.roleIds">
        <n-space vertical>
          <n-checkbox v-for="option in options" :key="option.value" :value="option.value" :disabled="option.disabled" :label="option.label" />
        </n-space>
        <n-empty v-if="!options.length" description="暂无可分配角色" />
      </n-checkbox-group>
    </n-form-item>
    <n-text depth="3">不分配角色时，用户不拥有角色授予的权限。</n-text>
  </n-form>
</template>

