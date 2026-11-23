<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import defaultLogo from '@/assets/images/logo.png'
import systemSetting from '@/config/SystemSetting'

defineProps<{ inverted?: boolean }>()
const failed = ref(false)
watch(() => systemSetting.logoFileId, () => { failed.value = false })
const customLogo = computed(() => systemSetting.logoFileId && !failed.value
  ? `${import.meta.env.VITE_API_BASE_URL}/system/config/siteLogo?id=${encodeURIComponent(systemSetting.logoFileId)}`
  : '')
</script>

<template>
  <img :src="customLogo || defaultLogo" :alt="systemSetting.title"
    :class="{ 'system-logo--inverted': inverted && !customLogo }" @error="failed = true" />
</template>

<style scoped>
img {
  object-fit: contain;
  flex-shrink: 0;
}
.system-logo--inverted {
  filter: brightness(0) invert(1);
}
</style>
