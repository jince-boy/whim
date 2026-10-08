<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
defineOptions({
  name: 'home',
})

const toggleRoute = () => {
  router.push('/system/user')
}
const count = ref(0)
</script>

<template>
  <n-el class="app-wrapper">
    <n-button type="primary" @click="toggleRoute">按钮</n-button>
    <p>Current component: A</p>
    <span>count: {{ count }}</span>
    <button @click="count++">+</button>
  </n-el>
</template>

<style scoped lang="scss">
</style>
