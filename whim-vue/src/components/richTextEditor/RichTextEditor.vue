<script setup lang="ts">
import { Editor, Toolbar } from '@wangeditor-next/editor-for-vue'
import '@wangeditor-next/editor/dist/css/style.css'
import { useThemeVars } from 'naive-ui'
import { message } from '@/plugins'
import type { RichTextEditorProps } from './types'
import { useRichTextEditor } from './hooks/useRichTextEditor'

const props = withDefaults(defineProps<RichTextEditorProps>(), { placeholder: '请输入正文…' })
const model = defineModel<string>({ required: true })
const busy = defineModel<boolean>('busy', { default: false })
const theme = useThemeVars()
const { editor, initialHtml, ready, failed, uploads, count, toolbarConfig, editorConfig, initialize, updateHtml } =
  useRichTextEditor(props, model, busy)
</script>

<template>
  <div class="rich-editor" role="group" aria-label="富文本编辑器">
    <template v-if="ready">
      <Toolbar v-if="editor" class="toolbar" :editor="editor" :default-config="toolbarConfig" />
      <div class="editor">
        <Editor :default-html="initialHtml" :default-config="editorConfig"
          @on-created="instance => editor = instance" @update:model-value="updateHtml"
          @custom-alert="(info, type) => message[type](info)" />
      </div>
    </template>
    <div v-else class="editor-status">
      <template v-if="failed && !imageHandlers">当前编辑器无法读取这段正文中的图片。</template>
      <template v-else-if="failed">正文图片加载失败 <n-button text type="primary" @click="initialize">重新加载</n-button></template>
      <template v-else>正在加载正文…</template>
    </div>
    <div v-if="uploads || maxlength !== undefined" class="footer">
      <n-text v-if="uploads" depth="3" role="status">正在上传 {{ uploads }} 张图片…</n-text>
      <n-text v-if="maxlength !== undefined" class="count" :type="count > maxlength ? 'error' : 'default'" depth="3">{{ count }} / {{ maxlength }}</n-text>
    </div>
  </div>
</template>

<style scoped>
.rich-editor {
  --w-e-textarea-bg-color: v-bind('theme.cardColor');
  --w-e-textarea-color: v-bind('theme.textColor1');
  --w-e-textarea-border-color: v-bind('theme.borderColor');
  --w-e-toolbar-color: v-bind('theme.textColor2');
  --w-e-toolbar-bg-color: v-bind('theme.cardColor');
  --w-e-toolbar-active-color: v-bind('theme.primaryColor');
  --w-e-toolbar-active-bg-color: v-bind('theme.hoverColor');
  --w-e-toolbar-border-color: v-bind('theme.borderColor');
  --w-e-modal-button-bg-color: v-bind('theme.buttonColor2');
  --w-e-modal-button-border-color: v-bind('theme.borderColor');
  width: 100%; min-width: 0; border: 1px solid v-bind('theme.borderColor'); border-radius: 6px;
  background: v-bind('theme.cardColor');
}
.rich-editor:focus-within { border-color: v-bind('theme.primaryColor'); }
.toolbar { border-bottom: 1px solid v-bind('theme.borderColor'); border-radius: 6px 6px 0 0; }
.toolbar :deep(.w-e-bar) { border-radius: inherit; flex-wrap: wrap; }
.editor {
  --writing-inset-inline: 12px;
  --writing-inset-block: 12px;
  height: clamp(320px, 38dvh, 440px);
  min-height: 240px;
  overflow: auto;
  resize: vertical;
}
/* wangEditor 把提示文字放在编辑节点外，两者须从同一行、同一列起笔。 */
.editor :deep([data-slate-editor]) {
  box-sizing: border-box;
  padding: var(--writing-inset-block) var(--writing-inset-inline);
  border-top: 0;
  line-height: 1.5;
}
.editor :deep([data-slate-editor] > p:first-child) { margin-block-start: 0; }
.editor :deep(.w-e-text-placeholder) {
  left: var(--writing-inset-inline);
  top: var(--writing-inset-block);
  line-height: 1.5;
  font-style: normal;
  color: v-bind('theme.placeholderColor');
}
@media (max-width: 600px) {
  .editor { height: clamp(260px, 36dvh, 320px); }
}
.editor :deep(.w-e-text-container img) { max-width: 100%; }
.editor-status { padding: 18px; }
.footer { display: flex; align-items: center; gap: 8px; padding: 4px 12px; font-size: 12px; }
.count { margin-inline-start: auto; }
</style>
