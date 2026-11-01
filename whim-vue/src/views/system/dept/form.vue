<script setup lang="ts">
import type { DeptFormProps } from './hooks/types'
import { useDeptForm } from './hooks/useDeptForm'

const props = defineProps<DeptFormProps>()
const { formRef, formModel, rules, statusOptions, parentOptions, loading, error, reload, leaderOptions, leaderLoading, leaderError, loadLeaders, searchLeaders, scrollLeaders, changeLeader } = useDeptForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-alert v-if="error" type="error" style="margin-bottom: 16px">
      {{ error }} <n-button text type="info" @click="reload">重新加载部门</n-button>
    </n-alert>
    <n-form-item label="上级部门" path="parentId">
      <n-tree-select v-model:value="formModel.parentId" :options="parentOptions" :loading="loading" :disabled="loading || !!error" filterable default-expand-all placeholder="请选择上级部门" />
    </n-form-item>
    <n-grid :cols="24" :x-gap="16" item-responsive>
      <n-form-item-gi label="部门名称" path="deptName" span="24 560:12">
        <n-input v-model:value="formModel.deptName" :maxlength="64" placeholder="请输入部门名称" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="部门编码" path="deptCode" span="24 560:12">
        <n-input v-model:value="formModel.deptCode" :maxlength="64" placeholder="请输入唯一的部门编码" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="显示排序" path="sort" span="24">
        <n-input-number v-model:value="formModel.sort" :min="0" :max="2147483647" :precision="0" style="width: 100%" />
      </n-form-item-gi>
      <n-form-item-gi label="状态" path="status" span="24">
        <n-radio-group v-model:value="formModel.status" aria-label="部门状态">
          <n-space>
            <n-radio v-for="option in statusOptions" :key="option.value" :value="option.value" :label="option.label" />
          </n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi v-if="props.initialModel" label="部门负责人" path="leaderUserId" span="24">
        <n-space vertical :size="6" style="width: 100%">
        <n-select :value="formModel.leaderUserId" :options="leaderOptions" :loading="leaderLoading" filterable remote clearable placeholder="搜索本部门用户的姓名或用户名" @search="searchLeaders" @scroll="scrollLeaders" @update:value="changeLeader">
          <template #empty>暂无匹配的本部门正常用户</template>
        </n-select>
          <n-text v-if="leaderError" type="error">{{ leaderError }} <n-button text type="info" @click="loadLeaders()">重新加载</n-button></n-text>
          <n-text v-else depth="3">仅可选择主部门为当前部门的正常用户。</n-text>
        </n-space>
      </n-form-item-gi>
      <n-gi v-else :span="24" style="margin-bottom: 18px">
        <n-text depth="3">创建后，将用户归属到此部门，再编辑部门选择负责人。</n-text>
      </n-gi>
      <n-form-item-gi label="联系电话" path="phone" span="24 560:12">
        <n-input v-model:value="formModel.phone" :maxlength="20" placeholder="请输入联系电话" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="邮箱" path="email" span="24 560:12">
        <n-input v-model:value="formModel.email" :maxlength="64" placeholder="请输入联系邮箱" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" span="24">
        <n-input v-model:value="formModel.remark" type="textarea" :maxlength="255" show-count :autosize="{ minRows: 3, maxRows: 5 }" placeholder="请输入备注" />
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>
