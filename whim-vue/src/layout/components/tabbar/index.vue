<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import type { ScrollbarInst } from 'naive-ui'
import { ChevronBackOutline, ChevronForwardOutline, RefreshOutline } from '@vicons/ionicons5'

defineOptions({ name: 'LayoutTabbar' })

interface OpenTab {
  path: string
  title: string
}

defineEmits<{ refresh: [] }>()

const route = useRoute()
const router = useRouter()
const scrollbar = ref<ScrollbarInst | null>(null)
const tabs = ref<OpenTab[]>([{ path: '/', title: '工作台' }])

/** 访问新页面时将其加入页签。 */
watch(
  () => route.path,
  (path) => {
    if (!tabs.value.some((tab) => tab.path === path)) {
      tabs.value.push({ path, title: String(route.meta.title ?? '未命名页面') })
    }
  },
  { immediate: true },
)

/** 切换到选中的页签。 */
function selectTab(path: string): void {
  void router.push(path)
}

/** 关闭页签；关闭当前页时切换到相邻页签。 */
function closeTab(path: string): void {
  const index = tabs.value.findIndex((tab) => tab.path === path)
  if (index <= 0) return

  tabs.value.splice(index, 1)
  if (route.path === path) {
    void router.push(tabs.value[Math.max(0, index - 1)]?.path ?? '/')
  }
}

/** 沿指定方向滚动页签。 */
function scrollTabs(direction: -1 | 1): void {
  scrollbar.value?.scrollBy({ left: direction * 180, behavior: 'smooth' })
}
</script>

<template>
  <n-el class="tabbar">
    <n-flex class="tabbar-content" align="center" :wrap="false">
      <n-button class="tabbar-control" quaternary aria-label="向左滚动页签" @click="scrollTabs(-1)">
        <template #icon
          ><n-icon><chevron-back-outline /></n-icon
        ></template>
      </n-button>

      <n-scrollbar ref="scrollbar" class="tabbar-scroll" x-scrollable trigger="none">
        <n-flex class="tabbar-tabs" align="center" :wrap="false">
          <n-tag
            v-for="tab in tabs"
            :key="tab.path"
            :type="route.path === tab.path ? 'primary' : 'default'"
            :bordered="false"
            :closable="tab.path !== '/'"
            role="tab"
            :aria-selected="route.path === tab.path"
            tabindex="0"
            @click="selectTab(tab.path)"
            @keydown.enter="selectTab(tab.path)"
            @keydown.space.prevent="selectTab(tab.path)"
            @close="closeTab(tab.path)"
          >
            {{ tab.title }}
          </n-tag>
        </n-flex>
      </n-scrollbar>

      <n-button class="tabbar-control" quaternary aria-label="向右滚动页签" @click="scrollTabs(1)">
        <template #icon
          ><n-icon><chevron-forward-outline /></n-icon
        ></template>
      </n-button>
      <n-button
        class="tabbar-control"
        quaternary
        aria-label="刷新当前页面"
        @click="$emit('refresh')"
      >
        <template #icon
          ><n-icon><refresh-outline /></n-icon
        ></template>
      </n-button>
    </n-flex>
  </n-el>
</template>

<style scoped>
.tabbar {
  height: 36px;
  border-bottom: 1px solid var(--divider-color);
  background-color: var(--card-color);
}

.tabbar-content,
.tabbar-control,
.tabbar-scroll,
.tabbar-tabs {
  height: 100%;
}

.tabbar-control {
  flex: none;
  border-radius: 0;
}

.tabbar-scroll {
  min-width: 0;
  flex: 1;
}

.tabbar-tabs {
  width: max-content;
  padding: 0 8px;
}
</style>
