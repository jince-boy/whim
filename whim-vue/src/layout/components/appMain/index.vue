<script setup lang="ts">
import {useAppMain} from './hooks/useAppMain'

const {appMainRef, pageVisible, refreshing, themeStore, keepAliveStore, refresh} = useAppMain()
defineExpose({appMainRef, refresh})
</script>

<template>
  <div ref="appMainRef" class="appMain">
    <router-view v-slot="{ Component, route }">
      <transition :name="themeStore.config.pageTransition" :css="!refreshing && themeStore.config.pageTransition !== 'none'" :mode="refreshing || themeStore.config.pageTransition === 'none' ? undefined : 'out-in'">
        <KeepAlive :include="themeStore.getCacheTabs ? keepAliveStore.keepAliveComponents : []">
          <component v-if="pageVisible" :is="Component" :key="route.name ?? route.path"/>
        </KeepAlive>
      </transition>
    </router-view>
  </div>
</template>

<style scoped lang="scss">
.appMain {
  padding: 12px;
}

/* 保留原有缩小退出、右侧滑入的默认效果。 */
.slide-leave-active {
  animation: shrinkOut 0.4s;
}

.slide-enter-active {
  animation: slideInRight 0.4s;
}

.fade-enter-active,
.fade-leave-active,
.fadeSlide-enter-active,
.fadeSlide-leave-active,
.zoom-enter-active,
.zoom-leave-active {
  transition: opacity 0.24s ease,
  transform 0.24s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fadeSlide-enter-from {
  opacity: 0;
  transform: translateX(24px);
}

.fadeSlide-leave-to {
  opacity: 0;
  transform: translateX(-24px);
}

.zoom-enter-from,
.zoom-leave-to {
  opacity: 0;
  transform: scale(0.94);
}

@media (prefers-reduced-motion: reduce) {
  .slide-enter-active,
  .slide-leave-active,
  .fade-enter-active,
  .fade-leave-active,
  .fadeSlide-enter-active,
  .fadeSlide-leave-active,
  .zoom-enter-active,
  .zoom-leave-active {
    animation: none;
    transition: none;
  }
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
