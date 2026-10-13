<script setup lang="ts">
import Result from '@/components/search/result.vue'
import { useMenuSearch } from './hooks/useMenuSearch'
defineOptions({ name: 'SearchComponent' })
const searchShowModal = defineModel<boolean>({ required: true })
const { createIcon, value, searchResults } = useMenuSearch()
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
        <n-input v-model:value="value" type="text" placeholder="请输入关键词搜索">
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
