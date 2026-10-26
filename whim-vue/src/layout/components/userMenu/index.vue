<script setup lang="ts">
import {useUserMenu} from './hooks/useUserMenu'

defineOptions({name: 'LayoutUserMenu'})
const props = withDefaults(defineProps<{ collapsed?: boolean; compact?: boolean }>(), {
  collapsed: false,
  compact: false,
})
const {displayName, avatar, avatarText, signingOut, options, openProfile, handleSelect} =
  useUserMenu()
</script>

<template>
  <div class="user-menu" :class="{ 'user-menu--compact': props.compact, 'user-menu--collapsed': props.collapsed }">
    <n-button quaternary class="user-profile-button" :disabled="signingOut" title="个人信息设置" aria-label="进入个人信息设置" @click="openProfile">
      <n-avatar round :size="props.compact ? 28 : 32" :src="avatar" :img-props="{ alt: displayName }" object-fit="cover">
        <template v-if="!avatar" #default>{{ avatarText }}</template>
        <template #fallback>{{ avatarText }}</template>
      </n-avatar>
      <n-ellipsis v-if="!props.collapsed && !props.compact" :tooltip="false" class="user-menu-name">
        {{ displayName }}
      </n-ellipsis>
    </n-button>
    <n-dropdown trigger="click" :options="options" placement="bottom-end" @select="handleSelect">
      <n-button quaternary class="user-menu-trigger" :disabled="signingOut" title="用户菜单" aria-label="打开用户菜单">
        <span aria-hidden="true">⋯</span>
      </n-button>
    </n-dropdown>
  </div>
</template>

<style scoped lang="scss">
.user-menu {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0 8px;
  box-sizing: border-box;
}

.user-profile-button {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0 8px;

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

.user-menu-trigger {
  flex-shrink: 0;
  width: 24px;
  height: 32px;
  padding: 0;
  font-size: 20px;
}

.user-menu-name {
  min-width: 0;
  text-align: left;
}

.user-menu--compact {
  width: 60px;
  height: 36px;
  padding: 0;
}

.user-menu--collapsed {
  padding: 0 4px;
}

.user-menu--compact,
.user-menu--collapsed {
  .user-profile-button {
    padding: 0;

    :deep(.n-button__content) {
      justify-content: center;
    }
  }
}
</style>
