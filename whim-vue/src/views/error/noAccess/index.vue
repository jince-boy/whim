<script setup lang="ts">
defineOptions({ name: 'NoAccessPage' })
</script>

<template>
  <n-result status="403" title="暂无可访问页面" description="请联系管理员分配可访问的菜单。">
    <template #footer>
      <router-link to="/profile"><n-button>个人信息设置</n-button></router-link>
    </template>
  </n-result>
</template>
