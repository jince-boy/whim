<script setup lang="ts">
import { reactive, useTemplateRef } from 'vue'
import { NForm, NFormItem, NInput, type FormInst, type FormRules } from 'naive-ui'

const props = defineProps<{ initialModel: { name: string; remark: string } }>()
const formModel = reactive({ ...props.initialModel })
const formRef = useTemplateRef<FormInst>('formRef')
const rules: FormRules = { name: { required: true, message: '请输入名称', trigger: 'blur' } }
defineExpose({ formRef, formModel })
</script>

<template>
  <NForm ref="formRef" :model="formModel" :rules="rules" label-placement="top">
    <NFormItem label="名称" path="name"><NInput v-model:value="formModel.name" /></NFormItem>
    <NFormItem label="备注" path="remark"
      ><NInput v-model:value="formModel.remark" type="textarea"
    /></NFormItem>
  </NForm>
</template>
