<script setup lang="ts">
import {useTabBar} from './hooks/useTabBar'
import Icon from '@/components/icon/Icon.vue'

defineOptions({name: 'LayoutTabBar'})
const emit = defineEmits<{ fullScreen: []; refresh: [] }>()
const {
  createIcon,
  themeStore,
  tabStore,
  showDropdown,
  x,
  y,
  dropdownOptions,
  handleContextMenu,
  onClickOutside,
  handleSelect,
  handleTabClick,
  handleCloseTab,
  handleScroll,
} = useTabBar(emit)
</script>

<template>
  <n-el ref="tabRoot" class="tab" :class="`tab--${themeStore.getTabStyle}`">
    <n-flex align="center" justify="space-between" :wrap="false" :size="0" class="tab-wrapper">
      <n-button quaternary :focusable="false" :render-icon="createIcon('chevron-left')" class="tab-scroll tab-scroll-left" aria-label="向左滚动标签" @click="handleScroll('left')"/>
      <n-scrollbar trigger="none" ref="scrollbar" class="tab-scrollbar" x-scrollable content-style="height:100%">
        <n-flex v-if="themeStore.getTabStyle === 'button'" align="center" class="tab-box" :wrap="false" role="navigation" aria-label="页面标签">
          <n-button v-for="item in tabStore.getAllTabs" :key="item.name" class="button-tab" :type="item.focused ? 'primary' : 'default'" size="small" :secondary="!item.focused" :focusable="false" icon-placement="left" :render-icon="themeStore.getShowTabIcon ? createIcon(item.icon) : null" :aria-current="item.focused ? 'page' : undefined" @click="handleTabClick(item.path)" @contextmenu="(e: MouseEvent) => handleContextMenu(e, item)">
            {{ item.title }}
            <span v-if="item.closeable" class="button-tab-close" :title="`关闭${item.title}`" @click.stop="handleCloseTab(item.name)">
              <Icon name="x" :size="12" />
            </span>
          </n-button>
        </n-flex>
        <div v-else class="tab-box" role="navigation" aria-label="页面标签">
          <div v-for="item in tabStore.getAllTabs" :key="item.name" class="tab-item" :class="{ 'tab-item--active': item.focused }" @contextmenu="(e: MouseEvent) => handleContextMenu(e, item)">
            <span v-if="themeStore.getTabStyle === 'browser'" class="tab-separator" aria-hidden="true"/>
            <button type="button" class="tab-link" :aria-current="item.focused ? 'page' : undefined" :title="item.title" @click="handleTabClick(item.path)">
              <n-icon v-if="themeStore.getShowTabIcon && item.icon" :size="16" :component="createIcon(item.icon)"/>
              <span class="tab-title">{{ item.title }}</span>
            </button>
            <button v-if="item.closeable" type="button" class="tab-close" :aria-label="`关闭${item.title}`" :title="`关闭${item.title}`" @click.stop="handleCloseTab(item.name)">
              <Icon name="x" :size="12" />
            </button>
          </div>
        </div>
        <n-dropdown placement="bottom-start" trigger="manual" :x="x" :y="y" :options="dropdownOptions" :show="showDropdown" :on-clickoutside="onClickOutside" @select="handleSelect"/>
      </n-scrollbar>
      <n-button quaternary :focusable="false" :render-icon="createIcon('chevron-right')" class="tab-scroll tab-scroll-right" aria-label="向右滚动标签" @click="handleScroll('right')"/>
    </n-flex>
  </n-el>
</template>

<style scoped lang="scss">
.tab {
  --tab-color-transition: 0.3s var(--n-bezier);

  height: 36px;
  background-color: var(--card-color);
  border-bottom: 1px solid var(--divider-color);
  transition: color var(--tab-color-transition),
  background-color var(--tab-color-transition),
  border-color var(--tab-color-transition);

  .tab-wrapper {
    height: 100%;
  }

  .tab-scroll {
    border-radius: 0;
    height: 100%;
    flex-shrink: 0;
    transition: color var(--tab-color-transition),
    background-color var(--tab-color-transition),
    border-color var(--tab-color-transition);
  }

  .tab-scroll-left {
    border-right: 1px solid var(--divider-color);
  }

  .tab-scroll-right {
    border-left: 1px solid var(--divider-color);
  }

  .tab-box {
    display: flex;
    align-items: center;
    gap: 6px;
    width: max-content;
    min-width: 100%;
    height: 100%;
    padding: 0 8px;
    box-sizing: border-box;
  }

  :deep(.n-scrollbar-rail__scrollbar) {
    display: none;
  }
}

.tab-scrollbar {
  flex: 1;
  min-width: 0;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  height: 28px;
  padding: 0 6px 0 10px;
  border: 1px solid transparent;
  color: var(--text-color-2);
  box-sizing: border-box;
  transition: color var(--tab-color-transition),
  background-color var(--tab-color-transition),
  border-color var(--tab-color-transition);

  &:hover {
    color: var(--primary-color);
    background-color: var(--hover-color);
  }
}

.tab-separator {
  position: absolute;
  width: 1px;
  background-color: var(--divider-color);
  pointer-events: none;
  transition: background-color var(--tab-color-transition),
  opacity var(--tab-color-transition);
}

.tab-link,
.tab-close {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;

  &:focus-visible {
    outline: 2px solid var(--primary-color);
    outline-offset: -2px;
    border-radius: 3px;
  }
}

.tab-link {
  gap: 6px;
  height: 100%;
}

.tab-title {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tab-close {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  margin-left: 4px;
  border-radius: 50%;
  transition: background-color var(--tab-color-transition);

  &:hover {
    background-color: var(--action-color);
  }

}

.tab-link :deep(.n-icon) {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.tab--button .tab-box {
  padding: 0;
}

.button-tab {
  flex-shrink: 0;
}

.button-tab-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: var(--n-icon-margin);
}

.tab--line .tab-box {
  gap: 0;
}

.tab--line .tab-item {
  height: 100%;
  border: 0;
  border-bottom: 2px solid transparent;
  padding-top: 2px;
  padding-right: 12px;
  padding-left: 12px;

  &:hover {
    background-color: transparent;
  }

  &--active {
    border-bottom-color: var(--primary-color);
    color: var(--primary-color);
  }
}

.tab--browser {
  border-bottom: 0;
}

.tab--browser .tab-box {
  align-items: flex-end;
  gap: 0;
  padding: 0 12px;
}

.tab--browser .tab-item {
  z-index: 1;
  min-width: 110px;
  height: 32px;
  padding: 0 10px 0 14px;
  border: 0;
  border-radius: 8px 8px 0 0;

  .tab-link {
    flex: 1;
    justify-content: flex-start;
  }

  .tab-separator {
    top: 8px;
    right: 0;
    bottom: 8px;
  }

  &:last-child .tab-separator,
  &:has(+ .tab-item--active) .tab-separator,
  &--active .tab-separator {
    opacity: 0;
  }

  &:hover,
  &:focus-within {
    background-color: color-mix(in srgb, var(--card-color) 96%, var(--primary-color));
  }

  &--active {
    z-index: 2;
    color: var(--primary-color);

    &,
    &:hover,
    &:focus-within {
      background-color: color-mix(in srgb, var(--card-color) 88%, var(--primary-color));
    }

    .tab-title {
      font-weight: var(--font-weight-strong);
    }

    // 镜像圆角直接继承主体正在过渡的背景色，不再独立插值阴影颜色。
    &::before,
    &::after {
      position: absolute;
      bottom: 0;
      width: 10px;
      height: 10px;
      background-color: inherit;
      mask-image: radial-gradient(circle at 0 0, transparent 9.5px, #000 10px);
      content: '';
      pointer-events: none;
    }

    &::before {
      left: -10px;
    }

    &::after {
      right: -10px;
      transform: scaleX(-1);
    }
  }
}
</style>
