<script setup lang="ts">
import type { HomePageRow } from './hooks/types'
import { homePageRules } from './hooks/rule'
import { useHomePageForm } from './hooks/useHomePageForm'

const props = defineProps<{ initialModel?: HomePageRow }>()
const { formRef, formModel, targetOptions, loading, typeOptions, statusOptions,
  changeTargetType, loadTargets, scrollTargets } = useHomePageForm(props.initialModel)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="homePageRules" label-placement="top" require-mark-placement="left">
    <n-form-item label="关联类型" path="targetType">
      <n-select :value="formModel.targetType" :options="typeOptions" @update:value="changeTargetType" />
    </n-form-item>
    <n-form-item v-if="formModel.targetType !== 4" label="关联对象" path="targetId">
      <n-select v-model:value="formModel.targetId" :options="targetOptions" :loading="loading" remote filterable clearable
        placeholder="输入名称搜索" @search="loadTargets" @update:show="(show: boolean) => show && loadTargets('')" @scroll="scrollTargets" />
    </n-form-item>
    <n-form-item label="首页路由" path="path">
      <n-input v-model:value="formModel.path" :maxlength="255" placeholder="例如 /finance/dashboard" />
    </n-form-item>
    <n-form-item label="页面组件" path="component">
      <n-input v-model:value="formModel.component" :maxlength="255" placeholder="相对于 src/views 的路径，例如 finance/Dashboard" />
    </n-form-item>
    <n-form-item v-if="formModel.targetType === 2" label="角色优先级" path="priority">
      <n-input-number v-model:value="formModel.priority" :min="0" :max="2147483647" :precision="0" placeholder="数值越小越优先" style="width: 100%" />
      <template #feedback>同时匹配多个角色时，数值小的优先。数值相同按配置 ID 升序匹配。</template>
    </n-form-item>
    <n-form-item label="状态" path="status">
      <n-radio-group v-model:value="formModel.status" aria-label="首页状态">
        <n-space><n-radio v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" /></n-space>
      </n-radio-group>
    </n-form-item>
  </n-form>
</template>
