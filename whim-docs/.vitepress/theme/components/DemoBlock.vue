<script setup lang="ts">
import { NCard, NTag } from 'naive-ui'
const props = withDefaults(defineProps<{ title?: string; description?: string }>(), {
  title: '交互演示',
})
</script>

<template>
  <NCard class="demo-block" :bordered="true">
    <div class="demo-heading">
      <strong>{{ props.title }}</strong
      ><NTag size="small" :bordered="false" type="success">真实组件</NTag>
    </div>
    <p v-if="props.description" class="demo-description">{{ props.description }}</p>
    <ClientOnly
      ><slot /><template #fallback
        ><p class="demo-description">交互示例将在页面加载后显示。</p></template
      ></ClientOnly
    >
  </NCard>
</template>
