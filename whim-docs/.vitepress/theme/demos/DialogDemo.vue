<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NFlex } from 'naive-ui'
import { useFormDialog } from '@/components/dialog'
import DemoForm from './DemoForm.vue'

const { openFormDialog, openConfirmDialog, openDeleteDialog } = useFormDialog()
const saved = ref<{ name: string; remark: string } | null>(null)

/** 打开带字段校验的真实表单弹窗，仅将结果保存在当前示例中。 */
function openForm() {
  openFormDialog({
    title: '编辑示例',
    formComponent: DemoForm,
    formProps: { initialModel: saved.value ?? { name: 'Whim 文档', remark: '' } },
    successMessage: '示例已保存到当前页面',
    /** 校验通过后保存独立的表单快照。 */
    onSubmit(model) {
      saved.value = model
      return true
    },
  })
}

/** 展示通用确认弹窗，不调用任何业务接口。 */
function openConfirm() {
  openConfirmDialog({
    title: '操作确认',
    content: '这是文档演示，不会修改业务数据。',
    onSubmit: () => true,
  })
}

/** 清除当前示例的本地结果。 */
function openDelete() {
  openDeleteDialog({
    content: '确定清除当前页面的示例结果吗？',
    /** 只清理当前组件持有的内存数据。 */
    onSubmit() {
      saved.value = null
      return true
    },
  })
}
</script>

<template>
  <NFlex
    ><NButton type="primary" @click="openForm">表单弹窗</NButton
    ><NButton @click="openConfirm">通用确认</NButton
    ><NButton @click="openDelete">删除确认</NButton></NFlex
  >
  <pre class="demo-snapshot">{{
    saved ? JSON.stringify(saved, null, 2) : '暂无保存结果。示例仅修改当前页面的内存数据。'
  }}</pre>
</template>
