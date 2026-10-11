<script setup lang="ts">
import { useKeepAliveStore } from '@/stores/modules/keepAlive.ts'
import { useThemeStore } from '@/stores/modules/theme'
import { nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const appMainRef = useTemplateRef<HTMLElement>('appMainRef')

const pageVisible = ref(true)
const refreshing = ref(false)
const route = useRoute()

const keepAliveStore = useKeepAliveStore()
const themeStore = useThemeStore()

// 关闭全局缓存时清理旧实例；开启及路由变化时按菜单配置缓存当前页面。
watch(
  [() => themeStore.getCacheTabs, () => route.name, () => route.meta.keepAlive],
  ([enabled, name, keepAlive]) => {
    if (!enabled) {
      keepAliveStore.clearKeepAlive()
    } else if (typeof name === 'string') {
      if (keepAlive) keepAliveStore.addKeepAlive(name)
      else keepAliveStore.removeKeepAlive(name)
    }
  },
  { immediate: true },
)

/** 先清理当前实例，再重新挂载；其他标签的缓存保持不变。 */
const refresh = async (): Promise<void> => {
  if (refreshing.value) return
  refreshing.value = true
  const name = typeof route.name === 'string' ? route.name : undefined
  try {
    if (name) keepAliveStore.removeKeepAlive(name)
    // 先让 KeepAlive 将当前实例标记为不缓存，再卸载，避免留下旧实例。
    await nextTick()
    pageVisible.value = false
    await nextTick()
    if (themeStore.getCacheTabs && name && route.name === name && route.meta.keepAlive) {
      keepAliveStore.addKeepAlive(name)
    }
    pageVisible.value = true
    await nextTick()
  } finally {
    refreshing.value = false
  }
}
defineExpose({
  appMainRef,
  refresh,
})
</script>

<template>
  <div ref="appMainRef" class="appMain">
    <router-view v-slot="{ Component, route }">
      <transition
        :name="themeStore.config.pageTransition"
        :css="!refreshing && themeStore.config.pageTransition !== 'none'"
        :mode="refreshing || themeStore.config.pageTransition === 'none' ? undefined : 'out-in'"
      >
        <KeepAlive :include="themeStore.getCacheTabs ? keepAliveStore.keepAliveComponents : []">
          <component v-if="pageVisible" :is="Component" :key="route.name ?? route.path" />
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
  transition:
    opacity 0.24s ease,
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
