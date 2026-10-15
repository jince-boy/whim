<script setup lang="ts">
defineOptions({ name: 'UnavailablePage' })
</script>
<template>
  <n-result status="warning" title="页面暂不可用" description="请联系管理员确认该菜单是否已开放。">
    <template #footer
      ><router-link to="/"><n-button>返回首页</n-button></router-link></template
    >
  </n-result>
</template>
