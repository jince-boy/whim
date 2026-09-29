<script setup lang="ts">
import { useRouter } from 'vue-router'

defineOptions({ name: 'NotFoundPage' })

const router = useRouter()
</script>

<template>
  <n-flex justify="center" align="center" style="min-height: 100vh">
    <n-result status="404" title="页面不存在" description="当前地址没有对应的页面">
      <template #footer>
        <n-button type="primary" @click="router.push('/')">返回工作台</n-button>
      </template>
    </n-result>
  </n-flex>
</template>
