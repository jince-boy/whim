<script setup lang="ts">
defineOptions({ name: 'LayoutFooter' })

const footerText = import.meta.env.VITE_APP_FOOTER_TEXT.replace(
  '{year}',
  String(new Date().getFullYear()),
)
</script>

<template>
  <n-flex class="footer" justify="center" align="center">
    <n-text depth="3">{{ footerText }}</n-text>
  </n-flex>
</template>

<style scoped>
.footer {
  height: 100%;
}
</style>
