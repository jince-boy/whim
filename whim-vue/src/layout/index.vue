<script setup lang="ts">
import { useRoute } from 'vue-router'
import LayoutHeader from './components/header/index.vue'
import LayoutSidebar from './components/sidebar/index.vue'
import LayoutTabbar from './components/tabbar/index.vue'
import LayoutFooter from './components/footer/index.vue'
import AppMain from './components/appMain/index.vue'

defineOptions({ name: 'AppLayout' })

const route = useRoute()
const collapsed = ref(false)
const mobileMenuOpen = ref(false)
const appMainRef = ref<InstanceType<typeof AppMain> | null>(null)

/** 路由变化后关闭移动端导航。 */
watch(
  () => route.fullPath,
  () => {
    mobileMenuOpen.value = false
  },
)
</script>

<template>
  <n-layout has-sider position="absolute">
    <n-layout-sider
      v-model:collapsed="collapsed"
      class="desktop-sider"
      bordered
      :width="240"
      :collapsed-width="64"
      collapse-mode="width"
      show-trigger="arrow-circle"
      :native-scrollbar="false"
    >
      <layout-sidebar :collapsed="collapsed" />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered position="absolute" class="layout-header">
        <layout-header @open-menu="mobileMenuOpen = true" />
      </n-layout-header>

      <n-layout-content
        position="absolute"
        class="layout-content"
        embedded
        :native-scrollbar="false"
      >
        <layout-tabbar @refresh="appMainRef?.refresh()" />
        <app-main ref="appMainRef" />
      </n-layout-content>

      <n-layout-footer bordered position="absolute" class="layout-footer">
        <layout-footer />
      </n-layout-footer>
    </n-layout>

    <n-drawer v-model:show="mobileMenuOpen" placement="left" :width="240">
      <n-drawer-content :show-header="false" body-content-style="padding: 0">
        <layout-sidebar @navigate="mobileMenuOpen = false" />
      </n-drawer-content>
    </n-drawer>
  </n-layout>
</template>

<style scoped>
.layout-header {
  height: 50px;
}

.layout-content {
  top: 50px;
  bottom: 50px;
}

.layout-footer {
  height: 50px;
}

@media (max-width: 768px) {
  .desktop-sider {
    display: none;
  }
}
</style>
