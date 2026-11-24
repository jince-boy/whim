<script setup lang="ts">
import type { SlowSqlSource } from '../hooks/types'

defineProps<{ source: SlowSqlSource | null }>()
</script>

<template>
  <dl v-if="source" class="sql-source">
    <dt>请求</dt><dd>{{ source.requestMapping ?? '无关联路由' }}</dd>
    <template v-if="source.controllerMethod">
      <dt>控制器</dt><dd>{{ source.controllerMethod }}</dd>
    </template>
    <dt>调用位置</dt><dd>{{ source.codeLocation ?? '未定位到项目代码' }}</dd>
    <template v-if="source.mapperMethod">
      <dt>Mapper</dt><dd>{{ source.mapperMethod }}</dd>
    </template>
  </dl>
  <n-text v-else depth="3">未记录独立来源</n-text>
</template>

<style scoped>
.sql-source { display: grid; grid-template-columns: max-content minmax(0, 1fr); gap: 3px 8px; margin: 0; }
.sql-source dt { color: var(--text-color-3); }
.sql-source dd { margin: 0; white-space: normal; overflow-wrap: anywhere; font-family: var(--app-font-family-mono); }
</style>
