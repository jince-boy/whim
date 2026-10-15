<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NForm, NFormItem, NModal } from 'naive-ui'
import WhimSelectIcon from '@/components/icon/WhimSelectIcon.vue'

const icon = ref<string | null>('search')
const showModal = ref(false)
</script>

<template>
  <WhimSelectIcon v-model="icon" />
  <NButton style="margin-top: 16px" @click="showModal = true">在弹窗中选择</NButton>
  <NModal
    v-model:show="showModal"
    preset="dialog"
    title="菜单图标"
    :show-icon="false"
    draggable
    style="
      width: 380px;
      max-width: calc(100vw - 32px);
      max-height: calc(100dvh - 32px);
      overflow-y: auto;
    "
  >
    <NForm label-placement="left" label-width="80">
      <NFormItem label="菜单图标">
        <WhimSelectIcon v-model="icon" />
      </NFormItem>
    </NForm>
  </NModal>
</template>
