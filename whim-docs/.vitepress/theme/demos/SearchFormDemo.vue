<script setup lang="ts">
import { ref } from 'vue'
import { NFormItemGi, NInput, NSelect } from 'naive-ui'
import { SearchForm, useSearchForm } from '@/components/form'

const [registerForm, form] = useSearchForm({
  defaultValues: { name: '', status: null as number | null },
  grid: { collapsed: false },
})
const model = form.model
const submitted = ref(form.getValues())
const options = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

/** 将提交或重置后的独立查询快照展示在示例中。 */
function handleSearch() {
  submitted.value = form.getValues()
}
</script>

<template>
  <SearchForm @register="registerForm" @submit="handleSearch" @reset="handleSearch">
    <NFormItemGi label="名称" path="name"
      ><NInput v-model:value="model.name" clearable
    /></NFormItemGi>
    <NFormItemGi label="状态" path="status"
      ><NSelect v-model:value="model.status" :options="options" clearable
    /></NFormItemGi>
  </SearchForm>
  <p class="demo-description">下面显示已提交条件；修改输入框后，点击搜索才会更新。</p>
  <pre class="demo-snapshot">{{ JSON.stringify(submitted, null, 2) }}</pre>
</template>
