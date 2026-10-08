<script setup lang="ts">
import systemSetting from '@/config/SystemSetting.ts'

defineOptions({
  name: 'LayoutFooter',
})
</script>

<template>
  <n-el class="footer">
    {{ systemSetting.copyright }}
  </n-el>
</template>

<style scoped lang="scss">
.footer {
  width: 100%;
  height: 100%;
  text-align: center;
  line-height: 50px;
}
</style>
