<script setup lang="ts">
import { useKeepAliveStore } from '@/stores/modules/keepAlive.ts'

const appMainRef = useTemplateRef<HTMLElement>('appMainRef')

const appMainKey = ref(0)

const keepAliveStore = useKeepAliveStore()

const refresh = () => {
  appMainKey.value++
}
defineExpose({
  appMainRef,
  refresh,
})
</script>

<template>
  <div ref="appMainRef" class="appMain">
    <router-view v-slot="{ Component }">
      <transition name="page" mode="out-in">
        <KeepAlive :include="keepAliveStore.keepAliveComponents">
          <component :is="Component" :key="appMainKey" />
        </KeepAlive>
      </transition>
    </router-view>
  </div>
</template>

<style scoped lang="scss">
.appMain {
  padding: 12px;
}

/* 在 style 标签中定义动画组合 */
.page-leave-active {
  animation: shrinkOut 0.4s;
}

.page-enter-active {
  animation: slideInRight 0.4s;
}

@keyframes shrinkOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.9);
  }
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
