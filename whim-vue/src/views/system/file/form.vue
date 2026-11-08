<script setup lang="ts">
import type { StorageFormProps } from './hooks/types'
import { useStorageForm } from './hooks/useStorageForm'
const props = defineProps<StorageFormProps>()
const { formRef, formModel, platformOptions, isLocal, rules } = useStorageForm(props)
defineExpose({ formRef, formModel })
</script>
<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-alert v-if="props.initialModel" type="info" :show-icon="false" class="form-hint">已有文件时可修改名称和凭据；更换存储位置请新增配置。</n-alert>
    <n-form-item label="配置名称" path="name"><n-input v-model:value="formModel.name" :maxlength="64" placeholder="例如：业务附件" /></n-form-item>
    <n-form-item label="存储类型" path="platform"><n-select v-model:value="formModel.platform" :options="platformOptions" /></n-form-item>
    <n-form-item :label="isLocal ? '本地目录' : '对象前缀'" path="basePath"><n-input v-model:value="formModel.basePath" :maxlength="512" :placeholder="isLocal ? '后端服务器上的目录，例如 /app/data/files' : '可选，例如 uploads，不带首尾斜杠'" /></n-form-item>
    <template v-if="!isLocal">
      <n-form-item label="服务地址" path="endpoint"><n-input v-model:value="formModel.endpoint" :maxlength="512" placeholder="https://存储服务地址" /></n-form-item>
      <n-grid :cols="2" :x-gap="16">
        <n-form-item-gi label="存储桶 Bucket" path="bucket"><n-input v-model:value="formModel.bucket" :maxlength="128" placeholder="已创建的存储桶" /></n-form-item-gi>
        <n-form-item-gi label="地域 Region" path="region"><n-input v-model:value="formModel.region" :maxlength="64" placeholder="OSS 必填，例如 cn-hangzhou" /></n-form-item-gi>
      </n-grid>
      <n-form-item label="Access Key" path="accessKey"><n-input v-model:value="formModel.accessKey" :maxlength="256" autocomplete="off" /></n-form-item>
      <n-form-item label="Secret Key" path="secretKey"><n-input v-model:value="formModel.secretKey" type="password" show-password-on="click" :maxlength="1024" autocomplete="new-password" :placeholder="props.initialModel ? '留空保留现有密钥' : '请输入存储密钥'" /></n-form-item>
    </template>
  </n-form>
</template>
<style scoped>.form-hint { margin-bottom: 16px; }</style>
