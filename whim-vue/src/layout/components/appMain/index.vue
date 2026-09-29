<script setup lang="ts">
defineOptions({ name: 'LayoutAppMain' })

const refreshKey = ref(0)

/** 重新挂载当前页面。 */
function refresh(): void {
  refreshKey.value += 1
}

defineExpose({ refresh })
</script>

<template>
  <main class="app-main">
    <router-view v-slot="{ Component, route }">
      <transition name="page" mode="out-in">
        <component :is="Component" :key="`${route.path}:${refreshKey}`" />
      </transition>
    </router-view>
  </main>
</template>

<style scoped>
.app-main {
  padding: 12px;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 160ms ease;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .page-enter-active,
  .page-leave-active {
    transition: none;
  }
}
</style>
