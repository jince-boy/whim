<script setup lang="ts">
import { useIcon } from '@/components/icon/useIcon.ts'
import { usePermissionStore } from '@/stores/modules/permission.ts'
import type { SafeMenuOption } from '@/utils/menu'
import Result from '@/components/search/result.vue'

defineOptions({
  name: 'searchComponent',
})
const searchShowModal = defineModel<boolean>({ required: true })

const { createIcon } = useIcon()

const permissionStore = usePermissionStore()
const value = ref('')
const searchResults = ref<{ text: string; path?: string }[]>([])

const handleInput = () => {
  const keyword = value.value.trim()
  if (!keyword) {
    searchResults.value = []
    return
  }
  const results: { text: string; path?: string }[] = []
  const dfs = (node: SafeMenuOption, pathStack: string[]) => {
    const currentName = node.name || ''
    const currentPath = node.path
    const newPathStack = [...pathStack, currentName]
    // 只记录有 path 的匹配项
    if (currentName.includes(keyword) && currentPath) {
      results.push({
        text: newPathStack.join(' > '),
        path: currentPath,
      })
    }
    // 递归子节点
    if (node.children && node.children.length) {
      for (const child of node.children) {
        dfs(child, newPathStack)
      }
    }
  }
  for (const menu of permissionStore.menus) {
    dfs(menu, [])
  }
  searchResults.value = results
}
</script>

<template>
  <n-modal
    v-model:show="searchShowModal"
    size="huge"
    :bordered="false"
    style="position: fixed; top: 100px; left: 0; right: 0"
  >
    <n-card
      style="width: 600px"
      size="huge"
      :bordered="false"
      role="dialog"
      aria-modal="true"
      footer-style="margin:0;padding:0;"
    >
      <div>
        <n-input
          v-model:value="value"
          type="text"
          placeholder="请输入关键词搜索"
          @input="handleInput"
        >
          <template #prefix>
            <n-icon :component="createIcon('search')" />
          </template>
        </n-input>
        <div class="result">
          <result
            :results="searchResults"
            v-if="searchResults.length > 0"
            @close="() => (searchShowModal = false)"
          />
          <n-empty v-else description="无数据" />
        </div>
      </div>
      <template #footer>
        <n-space class="search-footer" align="center">
          <div class="key"><i class="iconfont icon-c181huiche"></i><span>确认</span></div>
          <div class="key">
            <i class="iconfont icon-arrow-up"></i>
            <i class="iconfont icon-arrow-down"></i>
            <span>切换</span>
          </div>
          <div class="key"><i class="iconfont icon-ESC"></i><span>关闭</span></div>
        </n-space>
      </template>
    </n-card>
  </n-modal>
</template>

<style scoped lang="scss">
.result {
  margin-top: 20px;
}
.search-footer {
  height: 44px;
  padding: 0 40px;
  border-top: 1px solid var(--n-border-color);
}
.key {
  .iconfont {
    margin: 0 5px;
    padding: 2px;
    box-shadow:
      inset 0 -2px #cdcde6,
      inset 0 0 1px 1px #fff,
      0 1px 2px 1px #1e235a66;
    vertical-align: middle;
  }
}
</style>
