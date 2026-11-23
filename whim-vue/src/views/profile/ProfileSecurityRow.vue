<script setup lang="ts">
defineProps<{ title: string; description: string }>()
</script>

<template>
  <div class="security-row">
    <div class="security-copy">
      <h3>{{ title }}</h3>
      <n-text depth="3">{{ description }}</n-text>
    </div>
    <slot />
  </div>
</template>

<style scoped>
.security-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px 16px;
}
.security-copy h3 {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
}
.security-copy .n-text {
  display: block;
  font-size: 12px;
  line-height: 20px;
}
</style>
