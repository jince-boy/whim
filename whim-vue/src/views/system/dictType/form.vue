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
    label-placement="top"
    :model="formModel"
    :rules="dictTypeRules"
    require-mark-placement="left"
  >
    <n-form-item label="字典名称" path="name">
      <n-input v-model:value="formModel.name" placeholder="请输入字典名称" :maxlength="64" />
    </n-form-item>
    <n-form-item label="类型编码" path="type">
      <n-input
        v-model:value="formModel.type"
        placeholder="以字母开头，可包含数字和下划线"
        :maxlength="128"
      />
    </n-form-item>
    <n-form-item label="备注" path="remark">
      <n-input
        v-model:value="formModel.remark"
        type="textarea"
        placeholder="说明这个字典的用途（选填）"
        :maxlength="255"
        :autosize="{ minRows: 3, maxRows: 5 }"
        show-count
      />
    </n-form-item>
  </n-form>
</template>
