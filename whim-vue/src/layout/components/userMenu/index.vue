<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/modules/auth'
import { useUserStore } from '@/stores/modules/user'
import { logout } from '@/views/login/hooks/api'

defineOptions({ name: 'LayoutUserMenu' })
const props = withDefaults(defineProps<{ collapsed?: boolean; compact?: boolean }>(), {
  collapsed: false,
  compact: false,
})
const authStore = useAuthStore()
const userStore = useUserStore()
const router = useRouter()
const message = useMessage()
const signingOut = ref(false)
const username = computed(() => userStore.user?.username || authStore.username || '用户')
const avatarText = computed(() => username.value.slice(0, 1).toUpperCase())
const options = computed(() => [
  { label: signingOut.value ? '正在退出…' : '退出登录', key: 'logout', disabled: signingOut.value },
])

/** 服务端退出成功后清理本地登录状态和页面缓存。 */
async function handleSelect(key: string | number): Promise<void> {
  if (key !== 'logout' || signingOut.value) return
  signingOut.value = true
  try {
    const res = await logout()
    if (res.code !== 200) {
      message.error(res.message)
      return
    }
    authStore.logout()
    await router.replace('/login')
    message.success(res.message || '退出成功')
  } catch {
    // 请求错误由公共拦截器提示；失败时保留会话以便重试。
  } finally {
    signingOut.value = false
  }
}
</script>

<template>
  <n-dropdown trigger="click" :options="options" placement="bottom-end" @select="handleSelect">
    <n-button
      quaternary
      class="user-menu"
      :class="{ 'user-menu--compact': props.compact, 'user-menu--collapsed': props.collapsed }"
      :disabled="signingOut"
      :title="username"
      aria-label="用户菜单"
    >
      <n-avatar
        round
        :size="props.compact ? 28 : 32"
        :src="userStore.user?.avatar ?? undefined"
        :img-props="{ alt: username }"
        object-fit="cover"
      >
        <template v-if="!userStore.user?.avatar" #default>{{ avatarText }}</template>
        <template #fallback>{{ avatarText }}</template>
      </n-avatar>
      <n-ellipsis v-if="!props.collapsed && !props.compact" :tooltip="false" class="user-menu-name">
        {{ username }}
      </n-ellipsis>
    </n-button>
  </n-dropdown>
</template>

<style scoped lang="scss">
.user-menu {
  width: 100%;
  height: 100%;
  padding: 0 16px;

  :deep(.n-button__content) {
    width: 100%;
    min-width: 0;
    justify-content: flex-start;
    gap: 12px;
  }

  :deep(.n-avatar) {
    flex-shrink: 0;
  }
}

.user-menu-name {
  min-width: 0;
  text-align: left;
}

.user-menu--compact {
  width: 36px;
  height: 36px;
  padding: 4px;
}

.user-menu--collapsed {
  padding: 0;
}

.user-menu--compact,
.user-menu--collapsed {
  :deep(.n-button__content) {
    justify-content: center;
  }
}
</style>
