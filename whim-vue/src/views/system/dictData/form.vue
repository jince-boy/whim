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
    label-placement="top"
    :model="formModel"
    :rules="dictDataRules"
    require-mark-placement="left"
  >
    <n-form-item label="所属类型" path="type">
      <n-input :value="formModel.type" readonly />
    </n-form-item>
    <n-form-item label="字典标签" path="label">
      <n-input v-model:value="formModel.label" placeholder="展示给用户的名称" :maxlength="128" />
    </n-form-item>
    <n-form-item label="字典值" path="value">
      <n-input v-model:value="formModel.value" placeholder="实际保存的值，保留前导零" :maxlength="128" />
    </n-form-item>
    <n-form-item label="展示样式" path="listClass">
      <n-flex class="dict-style-field" align="center" :wrap="false">
        <n-select
          v-model:value="formModel.listClass"
          :options="options"
          placeholder="请选择展示样式"
        />
        <n-tag class="dict-style-preview" :type="formModel.listClass" :bordered="false">
          {{ formModel.label || '标签预览' }}
        </n-tag>
      </n-flex>
    </n-form-item>
    <n-form-item label="排序" path="sort">
      <n-input-number
        style="width: 100%"
        v-model:value="formModel.sort"
        :min="0"
        :max="2147483647"
        :precision="0"
        placeholder="数值越小越靠前"
      />
    </n-form-item>
    <n-form-item label="备注" path="remark">
      <n-input
        v-model:value="formModel.remark"
        type="textarea"
        placeholder="补充说明（选填）"
        :maxlength="255"
        :autosize="{ minRows: 2, maxRows: 4 }"
        show-count
      />
    </n-form-item>
  </n-form>
</template>

<style scoped>
.dict-style-field {
  width: 100%;
}

.dict-style-field .n-select {
  flex: 1;
  min-width: 0;
}

.dict-style-preview :deep(.n-tag__content) {
  display: block;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
