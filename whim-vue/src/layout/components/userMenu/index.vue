<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import { useUserMenu } from './hooks/useUserMenu'

defineOptions({ name: 'LayoutUserMenu' })
const props = withDefaults(defineProps<{ collapsed?: boolean; compact?: boolean }>(), {
  collapsed: false,
  compact: false,
})
const { username, displayName, avatar, avatarText, signingOut, menuShown, options, handleSelect } = useUserMenu()
</script>

<template>
  <n-el class="user-menu" :class="{ 'user-menu--compact': props.compact, 'user-menu--collapsed': props.collapsed }">
    <n-dropdown v-model:show="menuShown" trigger="click" :options="options" :width="224"
      :placement="props.compact ? 'bottom-end' : props.collapsed ? 'right-end' : 'top-start'" @select="handleSelect">
      <n-button quaternary class="user-menu-button" :class="{ 'user-menu-button--open': menuShown }"
        :disabled="signingOut" :loading="signingOut" :theme-overrides="{ borderRadius: '8px' }"
        :title="props.collapsed || props.compact ? displayName : undefined"
        :aria-label="`个人菜单：${displayName}`" aria-haspopup="menu" :aria-expanded="menuShown">
        <n-avatar class="user-menu-avatar" :round="false" :size="props.compact ? 28 : 34" :src="avatar"
          :img-props="{ alt: displayName }" object-fit="cover">
          <template v-if="!avatar" #default>{{ avatarText }}</template>
          <template #fallback>{{ avatarText }}</template>
        </n-avatar>
        <span v-if="!props.collapsed && !props.compact" class="user-menu-identity">
          <n-ellipsis :tooltip="false" class="user-menu-name">{{ displayName }}</n-ellipsis>
          <n-ellipsis :tooltip="false" class="user-menu-username">{{ username }}</n-ellipsis>
        </span>
        <Icon v-if="!props.collapsed" class="user-menu-chevron" name="chevrons-up-down" :size="14" />
      </n-button>
    </n-dropdown>
  </n-el>
</template>

<style scoped lang="scss">
.user-menu {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 4px 8px;
  box-sizing: border-box;
}

.user-menu-button {
  width: 100%;
  min-width: 0;
  height: 100%;
  padding: 0 8px;

  :deep(.n-button__content) {
    width: 100%;
    min-width: 0;
    justify-content: flex-start;
    gap: 10px;
  }
}

.user-menu-button--open {
  background-color: var(--button-color-2-hover);
}

.user-menu-avatar {
  flex-shrink: 0;
  color: var(--primary-color);
  background-color: color-mix(in srgb, var(--primary-color) 12%, var(--card-color));
  font-size: 15px;
  font-weight: 600;
}

.user-menu-identity {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  gap: 1px;
  min-width: 0;
  text-align: left;
}

.user-menu-name {
  font-size: 13px;
  font-weight: 600;
  line-height: 20px;
}

.user-menu-username {
  color: var(--text-color-3);
  font-size: 11px;
  line-height: 16px;
}

.user-menu-chevron {
  flex-shrink: 0;
  color: var(--text-color-3);
}

.user-menu--collapsed .user-menu-button {
  padding: 0;

  :deep(.n-button__content) {
    justify-content: center;
  }
}

.user-menu--compact {
  width: auto;
  height: 36px;
  padding: 0;

  .user-menu-button {
    height: 36px;
    padding: 0 6px;
  }

  .user-menu-button :deep(.n-button__content) {
    gap: 6px;
  }
}
</style>
