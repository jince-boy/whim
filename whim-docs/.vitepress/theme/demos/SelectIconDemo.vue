<script setup lang="ts">
import { ref } from 'vue'
import { NFlex } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'
import WhimSelectIcon from '@/components/icon/WhimSelectIcon.vue'

const icon = ref<string | null>('search')
</script>

<template>
  <WhimSelectIcon v-model="icon" />
  <NFlex align="center" style="margin-top: 20px">
    <Icon :name="icon" :size="28" color="var(--docs-brand)" />
    <span>当前值：{{ icon ?? '未选择' }}</span>
  </NFlex>
</template>
