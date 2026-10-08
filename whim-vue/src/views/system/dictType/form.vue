<script setup lang="ts">
import type { DictTypeFormProps } from '@/views/system/dictType/hooks/types.ts'
import { useDictTypeForm } from '@/views/system/dictType/hooks/useDictTypeForm.ts'
import { dictTypeRules } from '@/views/system/dictType/hooks/rule.ts'

const props = defineProps<DictTypeFormProps>()
const { formRef, formModel } = useDictTypeForm(props)

defineExpose({
  formRef,
  formModel,
})
</script>

<template>
  <n-form
    ref="formRef"
    label-width="auto"
    label-placement="left"
    :model="formModel"
    :rules="dictTypeRules"
    require-mark-placement="left"
  >
    <n-form-item label="字典名称" path="name">
      <n-input v-model:value="formModel.name" placeholder="请输入字典名称" />
    </n-form-item>
    <n-form-item label="字典类型" path="type">
      <n-input v-model:value="formModel.type" placeholder="请输入字典类型" />
    </n-form-item>
    <n-form-item label="备注">
      <n-input v-model:value="formModel.remark" type="textarea" placeholder="请输入字典备注" />
    </n-form-item>
  </n-form>
</template>

<style scoped lang="scss"></style>
