<script setup lang="ts">
import WhimPassword from '@/components/password/WhimPassword.vue'
import type { UserFormProps } from './hooks/types'
import { useUserForm } from './hooks/useUserForm'

const props = defineProps<UserFormProps>()
const {
  formRef, formModel, rules, isEdit, isSelf, organizationReadonly, selfEditNotice, statusOptions, genderOptions,
  departmentOptions, departmentLoading, departmentError, canLoadDepartments, reloadDepartments,
  canLoadPosts, postOptions, postLoading, postError, searchPosts, loadMorePosts, retryPosts,
} = useUserForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-alert v-if="departmentError" type="error" style="margin-bottom: 16px">
      {{ departmentError }} <n-button text type="info" @click="reloadDepartments">重新加载部门</n-button>
    </n-alert>
    <n-alert v-if="isSelf" type="info" style="margin-bottom: 16px">{{ selfEditNotice }}</n-alert>
    <n-grid :cols="24" :x-gap="16" item-responsive>
      <n-form-item-gi label="用户名" path="username" span="24 560:12">
        <n-input v-model:value="formModel.username" :disabled="isEdit" :maxlength="32" :input-props="{ autocomplete: 'off', name: 'managedUsername' }" placeholder="3～32位字母、数字或下划线" />
      </n-form-item-gi>
      <n-form-item-gi label="姓名" path="name" span="24 560:12">
        <n-input v-model:value="formModel.name" :maxlength="24" :input-props="{ autocomplete: 'off', name: 'managedName' }" placeholder="请输入姓名" clearable />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isEdit" label="初始密码" path="password" span="24 560:12">
        <WhimPassword v-model:value="formModel.password" :user-inputs="[formModel.username, formModel.name, formModel.email ?? '']" :input-props="{ name: 'managedPassword' }" :maxlength="72" placeholder="请输入8～72位密码" />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isEdit" label="确认密码" path="confirmPassword" span="24 560:12">
        <n-input v-model:value="formModel.confirmPassword" type="password" show-password-on="click" :input-props="{ autocomplete: 'new-password', name: 'managedPasswordConfirmation' }" :maxlength="72" placeholder="请再次输入密码" />
      </n-form-item-gi>
      <n-form-item-gi label="所属部门" path="deptId" span="24 560:12">
        <n-tree-select v-model:value="formModel.deptId" :options="departmentOptions" :loading="departmentLoading" :disabled="organizationReadonly || !canLoadDepartments || departmentLoading || !!departmentError" filterable clearable default-expand-all placeholder="未分配部门" />
      </n-form-item-gi>
      <n-form-item-gi label="岗位" path="postIds" span="24 560:12">
        <n-select v-model:value="formModel.postIds" multiple filterable remote clearable :options="postOptions" :loading="postLoading" :disabled="organizationReadonly || !canLoadPosts" :placeholder="canLoadPosts ? '请选择岗位，可搜索' : '暂无岗位查询权限'" @search="searchPosts" @scroll="loadMorePosts" />
      </n-form-item-gi>
      <n-gi v-if="postError" span="24">
        <n-alert type="error" style="margin-bottom: 16px">{{ postError }} <n-button text type="info" @click="retryPosts">重试</n-button></n-alert>
      </n-gi>
      <n-form-item-gi label="联系电话" path="phone" span="24 560:12">
        <n-input v-model:value="formModel.phone" :maxlength="20" placeholder="请输入联系电话" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="邮箱" path="email" span="24 560:12">
        <n-input v-model:value="formModel.email" :maxlength="64" placeholder="请输入邮箱" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="性别" path="gender" span="24 560:12">
        <n-radio-group v-model:value="formModel.gender" aria-label="性别">
          <n-space><n-radio v-for="option in genderOptions" :key="option.value" :value="option.value" :label="option.label" /></n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="状态" path="status" span="24 560:12">
        <n-radio-group v-model:value="formModel.status" :disabled="isSelf" aria-label="用户状态">
          <n-space><n-radio v-for="option in statusOptions" :key="option.value" :value="option.value" :label="option.label" /></n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" span="24">
        <n-input v-model:value="formModel.remark" type="textarea" :maxlength="255" show-count :autosize="{ minRows: 3, maxRows: 5 }" placeholder="请输入备注" />
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>

