<script setup lang="ts">
import { NButton, NEmpty, NInput, NPopover, NSpin, NVirtualList, useThemeVars } from 'naive-ui'
import Icon from './Icon.vue'
import { getIconName } from './catalog'
import { useSelectIcon, type SelectIconProps } from './hooks/useSelectIcon'

defineOptions({ name: 'WhimSelectIcon', inheritAttrs: false })

const props = withDefaults(defineProps<SelectIconProps>(), {
  placeholder: '请选择图标',
  searchInputPlaceholder: '搜索中文或英文图标名称',
  disabled: undefined,
  clearable: true,
})
const iconValue = defineModel<string | null>({ required: true })
const theme = useThemeVars()
const {
  show,
  keyword,
  panelId,
  size,
  disabled,
  status,
  selectedName,
  activeIndex,
  loading,
  loadFailed,
  loadCatalog,
  rowHeight,
  filteredIcons,
  iconRows,
  updateShow,
  closePanel,
  selectIcon,
  focusGrid,
  syncVisibleFocus,
  navigateGrid,
  nTriggerFormFocus,
  nTriggerFormBlur,
} = useSelectIcon(props, iconValue)
</script>

<template>
  <NPopover
    :show="show"
    :disabled="disabled"
    :to="true"
    trigger="click"
    placement="bottom-start"
    :show-arrow="false"
    :internal-trap-focus="true"
    style="padding: 0"
    @update:show="updateShow"
  >
    <template #trigger>
      <div class="whim-select-icon" :class="[size, status, { disabled, open: show }]">
        <button
          ref="triggerRef"
          v-bind="$attrs"
          type="button"
          class="whim-select-icon__trigger"
          :disabled="disabled"
          :aria-label="iconValue ? `${props.placeholder}：${iconValue}` : props.placeholder"
          aria-haspopup="dialog"
          :aria-expanded="show"
          :aria-controls="show ? panelId : undefined"
          @keydown.down.prevent="updateShow(true)"
          @keydown.esc.stop.prevent="closePanel"
          @focus="nTriggerFormFocus"
          @blur="nTriggerFormBlur"
        >
          <template v-if="iconValue">
            <Icon :name="iconValue" :size="20" />
            <span class="whim-select-icon__value">{{ iconValue }}</span>
          </template>
          <span v-else class="whim-select-icon__placeholder">{{ props.placeholder }}</span>
          <Icon class="whim-select-icon__arrow" name="chevron-down" :size="16" />
        </button>
        <button
          v-if="props.clearable && iconValue"
          type="button"
          class="whim-select-icon__clear"
          :disabled="disabled"
          aria-label="清空图标"
          @click.stop="selectIcon(null)"
        >
          <Icon name="x" :size="16" />
        </button>
      </div>
    </template>

    <div
      v-if="show"
      :id="panelId"
      class="whim-select-icon__panel"
      role="dialog"
      aria-label="选择图标"
      @keydown.esc.stop.prevent="closePanel"
    >
      <div class="whim-select-icon__search">
        <NInput
          ref="searchRef"
          v-model:value="keyword"
          :placeholder="props.searchInputPlaceholder"
          :aria-label="props.searchInputPlaceholder"
          clearable
          @keydown.down.prevent="focusGrid"
        />
      </div>
      <div class="whim-select-icon__count" role="status" aria-live="polite">
        <template v-if="loading">正在加载图标目录…</template>
        <template v-else>{{ keyword.trim() ? '找到' : '共' }} {{ filteredIcons.length }} 个图标</template>
      </div>
      <div v-if="loading" class="whim-select-icon__loading"><NSpin size="small" /></div>
      <NEmpty v-else-if="loadFailed" size="small" description="图标目录加载失败" class="whim-select-icon__empty">
        <template #extra><NButton size="small" @click="loadCatalog">重新加载</NButton></template>
      </NEmpty>
      <div
        v-else-if="filteredIcons.length"
        ref="gridRef"
        class="whim-select-icon__grid"
        @keydown="navigateGrid"
      >
        <NVirtualList
          ref="virtualListRef"
          :items="iconRows"
          :item-size="rowHeight"
          :padding-top="2"
          :padding-bottom="4"
          :style="{ height: `min(${Math.min(300, iconRows.length * rowHeight + 6)}px, calc(100dvh - 200px))` }"
          @scroll="syncVisibleFocus"
        >
          <template #default="{ item }">
            <div class="whim-select-icon__row" :style="{ height: `${rowHeight}px` }">
              <NButton
                v-for="(icon, index) in item.icons"
                :key="icon.name"
                class="whim-select-icon__option"
                :type="selectedName === getIconName(icon.name) ? 'primary' : 'default'"
                :aria-label="icon.label"
                :title="`${icon.label}（${icon.name}）`"
                :aria-pressed="selectedName === getIconName(icon.name)"
                :data-icon-index="item.start + index"
                :tabindex="activeIndex === item.start + index ? 0 : -1"
                @focus="activeIndex = item.start + index"
                @click="selectIcon(icon.name)"
              >
                <Icon :name="icon.name" :size="22" />
              </NButton>
            </div>
          </template>
        </NVirtualList>
      </div>
      <NEmpty v-else size="small" description="没有匹配的图标" class="whim-select-icon__empty" />
    </div>
  </NPopover>
</template>

<style scoped>
.whim-select-icon {
  display: flex;
  box-sizing: border-box;
  width: 100%;
  height: v-bind('theme.heightMedium');
  border: 1px solid v-bind('theme.borderColor');
  border-radius: v-bind('theme.borderRadius');
  background: v-bind('theme.inputColor');
  color: v-bind('theme.textColor2');
  transition: border-color 0.2s;
}
.whim-select-icon.small {
  height: v-bind('theme.heightSmall');
}
.whim-select-icon.large {
  height: v-bind('theme.heightLarge');
}
.whim-select-icon:not(.disabled):hover,
.whim-select-icon:focus-within,
.whim-select-icon.open {
  border-color: v-bind('theme.primaryColor');
}
.whim-select-icon.error {
  border-color: v-bind('theme.errorColor');
}
.whim-select-icon.warning {
  border-color: v-bind('theme.warningColor');
}
.whim-select-icon.success {
  border-color: v-bind('theme.successColor');
}
.whim-select-icon.disabled {
  background: v-bind('theme.inputColorDisabled');
  opacity: 0.6;
}
.whim-select-icon__trigger,
.whim-select-icon__clear {
  display: flex;
  align-items: center;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}
.whim-select-icon__trigger {
  flex: 1;
  min-width: 0;
  gap: 8px;
  padding: 0 12px;
}
.whim-select-icon__trigger:focus-visible,
.whim-select-icon__clear:focus-visible {
  outline: 2px solid v-bind('theme.primaryColor');
  outline-offset: -2px;
}
.whim-select-icon__trigger:disabled,
.whim-select-icon__clear:disabled {
  cursor: not-allowed;
}
.whim-select-icon__placeholder {
  color: v-bind('theme.placeholderColor');
}
.whim-select-icon__value {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.whim-select-icon__arrow {
  flex-shrink: 0;
  margin-left: auto;
  color: v-bind('theme.placeholderColor');
}
.whim-select-icon__clear {
  padding: 0 10px;
  font-size: 18px;
}
.whim-select-icon__panel {
  width: min(488px, calc(100vw - 32px));
}
.whim-select-icon__search {
  padding: 12px;
}
.whim-select-icon__count {
  padding: 0 14px 8px;
  color: v-bind('theme.textColor3');
  font-size: 12px;
}
.whim-select-icon__grid {
  padding-bottom: 8px;
}
.whim-select-icon__row {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 8px;
  box-sizing: border-box;
  padding: 0 14px 8px;
}
.whim-select-icon__option {
  width: 100%;
  height: 40px;
  padding: 0;
}
.whim-select-icon__empty {
  padding: 28px 0;
}
.whim-select-icon__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 180px;
}
</style>
