<script setup lang="ts">
defineOptions({ name: 'DashboardPage' })

const appName = import.meta.env.VITE_APP_NAME
</script>

<template>
  <n-space vertical :size="20">
    <n-card>
      <n-space vertical :size="8">
        <n-h2 style="margin: 0">欢迎使用 {{ appName }}</n-h2>
        <n-text depth="3">在这里开始管理你的业务。</n-text>
      </n-space>
    </n-card>

    <n-card title="工作台概览">
      <n-empty description="暂无工作台内容" />
    </n-card>
  </n-space>
</template>
