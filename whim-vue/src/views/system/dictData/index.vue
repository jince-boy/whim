<script setup lang="ts">
import { useDictDataRedirect } from './hooks/useDictDataRedirect'

useDictDataRedirect()
</script>

<template>
  <n-spin description="正在打开字典管理" />
</template>
