<script setup lang="ts">
import defaultLogo from '@/assets/images/logo.png'
import type { ConfigImageProps } from '../hooks/types'
import { useConfigImage } from '../hooks/useConfigImage'

const props = withDefaults(defineProps<ConfigImageProps>(), { size: 36 })
const { source, loading } = useConfigImage(props)
</script>

<template>
  <n-spin v-if="loading" size="small" />
  <n-image v-else-if="source || siteLogo" :src="source || defaultLogo" :width="size" :height="size"
    object-fit="contain" :alt="siteLogo ? '系统 Logo' : '配置图片'" />
  <n-text v-else depth="3">{{ fileId || file ? '图片不可用' : '—' }}</n-text>
</template>
