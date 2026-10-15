<script setup lang="ts">
import { NButton, NEmpty, NInput, NPopover, NScrollbar, useThemeVars } from 'naive-ui'
import Icon from './Icon.vue'
import { useSelectIcon, type SelectIconProps } from './hooks/useSelectIcon'

defineOptions({ name: 'WhimSelectIcon', inheritAttrs: false })

const props = withDefaults(defineProps<SelectIconProps>(), {
  placeholder: '请选择图标',
  searchInputPlaceholder: '搜索图标名称或类名',
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
  filteredIcons,
  updateShow,
  closePanel,
  selectIcon,
  focusGrid,
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
          <span class="whim-select-icon__arrow" aria-hidden="true">⌄</span>
        </button>
        <button
          v-if="props.clearable && iconValue"
          type="button"
          class="whim-select-icon__clear"
          :disabled="disabled"
          aria-label="清空图标"
          @click.stop="selectIcon(null)"
        >
          <span aria-hidden="true">×</span>
        </button>
      </div>
    </template>

    <div
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
      <NScrollbar
        style="max-height: min(300px, calc(100dvh - 160px))"
        content-style="padding: 0 12px 12px; box-sizing: border-box"
      >
        <div
          v-if="filteredIcons.length"
          ref="gridRef"
          class="whim-select-icon__grid"
          @keydown="navigateGrid"
        >
          <NButton
            v-for="icon in filteredIcons"
            :key="icon.font_class"
            class="whim-select-icon__option"
            :type="iconValue === icon.font_class ? 'primary' : 'default'"
            :aria-label="icon.name || icon.font_class"
            :aria-pressed="iconValue === icon.font_class"
            @click="selectIcon(icon.font_class)"
          >
            <Icon :name="icon.font_class" :size="22" />
          </NButton>
        </div>
        <NEmpty v-else size="small" description="没有匹配的图标" class="whim-select-icon__empty" />
      </NScrollbar>
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
.whim-select-icon__grid {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 8px;
  padding: 2px;
}
.whim-select-icon__option {
  width: 100%;
  height: 40px;
  padding: 0;
}
.whim-select-icon__empty {
  padding: 28px 0;
}
</style>
