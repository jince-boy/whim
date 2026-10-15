<script setup lang="ts">
import { useDefaultHome } from './hooks/useDefaultHome'

defineOptions({ name: 'DefaultHome' })
const { toggleRoute, count } = useDefaultHome()
</script>

<template>
  <n-el class="app-wrapper">
    <n-button type="primary" @click="toggleRoute">按钮</n-button>
    <p>Current component: A</p>
    <span>count: {{ count }}</span>
    <button @click="count++">+</button>
  </n-el>
</template>

<style scoped lang="scss"></style>
