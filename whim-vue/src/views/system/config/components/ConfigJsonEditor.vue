<script setup lang="ts">
import { computed, ref, useTemplateRef } from 'vue'
import { useThemeVars } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'
import { message } from '@/plugins'
import { isValidConfigJson } from '../hooks/rule'

const value = defineModel<string>('value', { required: true })
const theme = useThemeVars()
const highlight = useTemplateRef<HTMLDivElement>('highlight')
const composing = ref(false)
const lines = computed(() => value.value.split(/\r\n|[\r\n]/))
const gutterWidth = computed(() => Math.max(2, String(lines.value.length).length) + 'ch')

function formatJson() {
  if (!isValidConfigJson(value.value)) {
    message.warning('JSON 语法有误，请修正后再格式化')
    return
  }

  // 只调整标记之间的空白，保留大整数精度、键顺序和字符串转义。
  const parts: string[] = []
  let depth = 0
  let previous = ''
  for (const [token] of value.value.matchAll(/"(?:[^"\\]|\\.)*"|[a-zA-Z0-9.+-]+|[^\s]/g)) {
    const closes = token === '}' || token === ']'
    const afterOpen = previous === '{' || previous === '['
    if (closes) depth--
    if (previous === ',' || (afterOpen && !closes) || (closes && !afterOpen)) {
      parts.push('\n', '  '.repeat(depth))
    } else if (previous === ':') {
      parts.push(' ')
    }
    parts.push(token)
    if (token === '{' || token === '[') depth++
    previous = token
  }
  value.value = parts.join('')
}

function syncScroll(event: Event) {
  const textarea = event.currentTarget as HTMLTextAreaElement
  highlight.value?.scrollTo(textarea.scrollLeft, textarea.scrollTop)
}
</script>

<template>
  <div class="json-editor" :class="{ composing }" @compositionstart="composing = true" @compositionend="composing = false">
    <n-input v-model:value="value" type="textarea" :rows="7" :resizable="false" placeholder="请输入 JSON"
      :input-props="{ 'aria-label': 'JSON 配置值', spellcheck: false, onScroll: syncScroll }" />
    <div ref="highlight" class="highlight" aria-hidden="true">
      <div class="line-numbers">
        <div v-for="(line, index) in lines" :key="index" class="numbered-line" :data-line="line">
          <span>{{ index + 1 }}</span>
        </div>
      </div>
      <n-code :code="value" language="json" :trim="false" />
    </div>
    <n-tooltip placement="top" :delay="250">
      <template #trigger>
        <n-button class="format-button" circle size="small" aria-label="格式化"
          :disabled="composing || !value.trim()" @click="formatJson">
          <template #icon><Icon name="braces" :size="16" /></template>
        </n-button>
      </template>
      格式化 JSON
    </n-tooltip>
  </div>
</template>

<style scoped>
.json-editor {
  --format-size: 28px;
  --format-gap: 8px;
  --code-right: calc(var(--format-size) + 2 * var(--format-gap));
  --code-left: calc(22px + v-bind(gutterWidth));
  position: relative;
  width: 100%;
  min-width: 0;
}

.json-editor :deep(.n-input-wrapper) {
  padding: 0;
}

.format-button {
  position: absolute;
  z-index: 1;
  top: 6px;
  right: var(--format-gap);
  width: var(--format-size);
  height: var(--format-size);
  background-color: v-bind('theme.popoverColor');
  box-shadow: 0 1px 4px color-mix(in srgb, v-bind('theme.textColor1') 16%, transparent);
}

@media (hover: hover) and (pointer: fine) {
  .json-editor:not(:hover):not(:focus-within) .format-button {
    opacity: 0;
    pointer-events: none;
  }
}

/* 同一组排版参数让高亮字符、选区和插入光标在长行换行时保持对齐。 */
.json-editor :deep(.n-input textarea.n-input__textarea-el),
.json-editor :deep(.n-input .n-input__placeholder),
.line-numbers,
.highlight :deep(pre) {
  box-sizing: border-box;
  padding: 8px var(--code-right) 8px var(--code-left);
  font: 14px/1.6 v-bind('theme.fontFamilyMono');
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: normal;
  tab-size: 4;
}

.highlight {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  user-select: none;
}

.highlight :deep(.n-code) {
  display: block;
}

/* 用相同的行宽折行，使下一行的行号出现在这条逻辑行的全部折行之后。 */
.line-numbers {
  position: absolute;
  inset: 0 0 auto;
  color: v-bind('theme.textColor3');
}

.numbered-line {
  position: relative;
  min-height: 1lh;
}

.numbered-line::after {
  content: attr(data-line);
  visibility: hidden;
}

.numbered-line > span {
  position: absolute;
  right: calc(100% + 10px);
  top: 0;
}

.highlight :deep(pre)::after {
  content: '\200b';
}

/* 输入法组字时显示原生输入，下划线和候选位置由浏览器处理。 */
@media (forced-colors: none) {
  .json-editor:not(.composing) :deep(textarea) {
    color: transparent;
    -webkit-text-fill-color: transparent;
  }
}

.composing .highlight :deep(.n-code) {
  visibility: hidden;
}

@media (forced-colors: active) {
  .highlight :deep(.n-code) {
    visibility: hidden;
  }
}
</style>
