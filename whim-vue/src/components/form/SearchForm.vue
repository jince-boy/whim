<script setup lang="ts">
import {
  type BaseFormEmits,
  type BaseFormProps,
  useBaseForm,
} from '@/components/form/useSearchForm.ts'

defineOptions({
  name: 'SearchForm',
})
/**
 * 使用泛型定义 Props 类型（默认值为 Record<string, unknown>）
 * 这样父组件传入时可自动推导出类型
 */
const props = defineProps<BaseFormProps<Record<string, unknown>>>()

/**
 * 定义组件事件
 * @event submit 表单提交时触发
 * @event reset 表单重置时触发
 */
const emit = defineEmits<BaseFormEmits<Record<string, unknown>>>()

const { formRef, formModel, submit, resetFields } = useBaseForm(props, emit)

/** 暴露方法给父组件调用 */
defineExpose({
  submit,
  resetFields,
})
</script>

<template>
  <n-card :bordered="false">
    <n-form
      ref="formRef"
      inline
      label-width="auto"
      :model="formModel"
      :rules="rules"
      :show-feedback="props.showFeedback"
      :label-placement="props.labelPlacement"
      label-align="left"
    >
      <n-grid :cols="5" x-gap="12" y-gap="12">
        <!-- 表单项插槽 -->
        <slot name="form"></slot>

        <!-- 操作按钮区 -->
        <slot name="action">
          <n-gi>
            <n-space>
              <n-button type="primary" @click="submit">搜索</n-button>
              <n-button @click="resetFields">重置</n-button>
            </n-space>
          </n-gi>
        </slot>
      </n-grid>
    </n-form>
  </n-card>
</template>

<style scoped lang="scss">
:deep(.n-input__placeholder) {
  line-height: 1.6;
}
</style>
