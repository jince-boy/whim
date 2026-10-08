<script setup lang="ts">
import { dictDataRules } from '@/views/system/dictData/hooks/rule.ts'
import type { DictDataFormProps } from '@/views/system/dictData/hooks/types.ts'
import { useDictDataForm } from '@/views/system/dictData/hooks/useDictDataForm.ts'

const props = defineProps<DictDataFormProps>()
const { formRef, formModel, options } = useDictDataForm(props)

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
    :rules="dictDataRules"
    require-mark-placement="left"
  >
    <n-form-item label="字典类型" path="dictType">
      <n-input v-model:value="formModel.dictType" disabled placeholder="请输入字典类型" />
    </n-form-item>
    <n-form-item label="数据标签" path="label">
      <n-input v-model:value="formModel.label" placeholder="请输入数据标签" />
    </n-form-item>
    <n-form-item label="数据键值" path="value">
      <n-input v-model:value="formModel.value" placeholder="请输入数据键值" />
    </n-form-item>
    <n-form-item label="回显样式" path="listClass">
      <n-select
        v-model:value="formModel.listClass"
        :options="options"
        placeholder="请选择回显样式"
      />
    </n-form-item>
    <n-form-item label="排序" path="sort">
      <n-input-number
        style="width: 100%"
        v-model:value="formModel.sort"
        clearable
        placeholder="请输入排序"
      />
    </n-form-item>
    <n-form-item label="备注">
      <n-input v-model:value="formModel.remark" type="textarea" placeholder="请输入字典备注" />
    </n-form-item>
  </n-form>
</template>

<style scoped lang="scss"></style>
