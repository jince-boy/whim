<script setup lang="ts">
import systemSetting from '@/config/SystemSetting'

defineOptions({ name: 'HomePage' })
</script>

<template>
  <n-el class="app-wrapper">
    <n-card :title="systemSetting.title">{{ systemSetting.description }}</n-card>
  </n-el>
</template>
