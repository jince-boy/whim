<script setup lang="ts">
import { ref } from 'vue'
import { NFlex, NSlider } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'

const size = ref(24)
const icons = ['search', 'arrow-up', 'arrow-down', 'c181huiche']
</script>

<template>
  <NFlex align="center" :size="24">
    <div v-for="name in icons" :key="name" style="display: grid; justify-items: center; gap: 12px">
      <Icon :name="name" :size="size" color="var(--docs-brand)" />
      <span style="font-size: 11px">{{ name }}</span>
    </div>
  </NFlex>
  <NSlider
    v-model:value="size"
    :min="16"
    :max="48"
    style="margin-top: 24px"
    aria-label="图标尺寸"
  />
</template>
