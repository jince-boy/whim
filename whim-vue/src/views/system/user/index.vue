<script setup lang="ts">
defineOptions({
  name: 'SysUser',
})
const count = ref(0)
</script>

<template>
  <n-el class="app-wrapper">
    用户管理
    <n-button type="primary"> Primary</n-button>
    <p>Current component: A</p>
    <span>count: {{ count }}</span>
    <button @click="count++">+</button>
  </n-el>
</template>

<style scoped lang="scss"></style>
