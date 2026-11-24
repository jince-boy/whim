<script setup lang="ts">
import AppIcon from '@/components/appIcon/AppIcon.vue'
import ProfileSecurityRow from './ProfileSecurityRow.vue'
import { useTwoFactorSettings } from './hooks/useTwoFactorSettings'

const props = defineProps<{ username: string; disabled: boolean }>()
const emit = defineEmits<{ changePassword: [] }>()
const formId = useId()
const { enabled, loading, statusDescription, dialog, uri, canSubmit, open, close, refresh, submit, copyRecoveryCode } =
  useTwoFactorSettings(() => props.username)
</script>

<template>
  <div class="profile-security-list">
    <ProfileSecurityRow title="登录密码" description="验证原密码后修改，修改成功需重新登录">
      <n-button secondary type="warning" :disabled="props.disabled" @click="emit('changePassword')">
        <template #icon><AppIcon name="key-round" /></template>修改密码
      </n-button>
    </ProfileSecurityRow>
    <ProfileSecurityRow title="两步验证" :description="statusDescription">
      <n-button v-if="enabled === null" secondary :loading="loading" @click="refresh">{{ loading ? '查询中' : '重新查询' }}</n-button>
      <n-button v-else secondary :disabled="props.disabled || loading" :type="enabled ? 'warning' : 'success'" @click="open">
        <template #icon><AppIcon name="shield-check" /></template>{{ enabled ? '关闭验证' : '开启验证' }}
      </n-button>
    </ProfileSecurityRow>
    <n-modal :show="!!dialog" preset="card" class="factor-dialog" :class="{ 'factor-dialog-binding': dialog?.setup }"
      :title="dialog?.disabling ? '关闭两步验证' : dialog?.setup ? '绑定手机验证器' : '开启两步验证'"
      :closable="!dialog?.submitting" :mask-closable="!dialog?.submitting" :close-on-esc="!dialog?.submitting" @update:show="close"
      content-style="min-height: 0; overflow-y: auto;">
      <n-el v-if="dialog">
        <n-form :id="formId" :disabled="dialog.submitting" label-placement="top" @submit.prevent="submit">
          <div v-if="dialog.setup" class="binding-fields">
            <div class="pairing">
              <div class="qr-frame"><n-qr-code :value="uri" :size="134" :padding="12" /></div>
              <div class="pairing-fields">
                <p class="hint">打开手机验证器，扫描二维码添加 Whim 账号。</p>
                <details class="manual-key">
                  <summary>无法扫码？手动输入密钥</summary>
                  <code>{{ dialog.setup.secret }}</code>
                </details>
                <n-form-item label="验证器动态码" :show-feedback="false">
                  <n-input v-model:value="dialog.code" :maxlength="6" placeholder="输入 6 位数字"
                    :input-props="{ inputmode: 'numeric', autocomplete: 'one-time-code', 'aria-label': '验证器动态码' }" />
                </n-form-item>
              </div>
            </div>
            <section class="recovery" aria-label="保存恢复码">
              <h3>保存恢复码</h3>
              <p class="hint">手机丢失时可用此码找回账号。请保存在手机以外的安全位置。</p>
              <div class="recovery-code">
                <code ref="recoveryDisplay">{{ dialog.setup.recoveryCode }}</code>
                <n-button size="small" quaternary :disabled="dialog.submitting" @click="copyRecoveryCode">
                  <template #icon><AppIcon name="copy" /></template>复制
                </n-button>
              </div>
              <n-checkbox v-model:checked="dialog.savedRecovery">我已保存恢复码</n-checkbox>
            </section>
            <p class="hint">开启后，所有设备都需要重新登录。</p>
          </div>
          <div v-else class="password-fields">
            <p class="hint">{{ dialog.disabling ? '关闭后，当前账号将在所有设备退出登录。' : '先验证你的登录密码，再绑定手机验证器。' }}</p>
            <n-form-item label="当前密码" :show-feedback="false">
              <n-input v-model:value="dialog.password" type="password" autofocus :maxlength="72" placeholder="输入当前登录密码"
                show-password-on="mousedown" :input-props="{ autocomplete: 'current-password', 'aria-label': '当前密码' }" />
            </n-form-item>
            <n-form-item v-if="dialog.disabling" label="动态码或恢复码" :show-feedback="false">
              <n-input v-model:value="dialog.code" :maxlength="32" placeholder="输入 6 位动态码或 32 位恢复码"
                :input-props="{ autocomplete: 'one-time-code', 'aria-label': '动态码或恢复码' }" />
            </n-form-item>
          </div>
        </n-form>
      </n-el>
      <template #footer>
        <n-space v-if="dialog" justify="end">
          <n-button :disabled="dialog.submitting" @click="close">取消</n-button>
          <n-button v-if="dialog.setup" :disabled="dialog.submitting" @click="open">重新验证密码</n-button>
          <n-button attr-type="submit" :form="formId" :type="dialog.disabling ? 'warning' : 'success'" :loading="dialog.submitting" :disabled="!canSubmit">
            {{ dialog.disabling ? '确认关闭' : dialog.setup ? '确认开启' : '验证密码' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.profile-security-list {
  margin-top: 18px;
}
.profile-security-list > :not(:first-child) {
  border-top: 1px solid var(--divider-color);
  margin-top: 14px;
  padding-top: 14px;
}
.password-fields, .binding-fields, .pairing-fields { display: grid; gap: 16px; }
.hint { margin: 0; color: var(--text-color-3); font-size: 13px; line-height: 1.6; }
.pairing {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 20px;
}
.qr-frame {
  display: flex;
  justify-self: center;
  overflow: hidden;
  border: 1px solid var(--divider-color);
  border-radius: 8px;
}
.manual-key { min-width: 0; font-size: 13px; }
.manual-key summary { width: fit-content; color: var(--primary-color); cursor: pointer; }
.manual-key code { display: block; margin-top: 6px; }
.recovery {
  display: grid;
  gap: 10px;
  padding: 14px 16px;
  border: 1px solid var(--divider-color);
  border-radius: 8px;
  background: var(--action-color);
}
.recovery h3 { margin: 0; font-size: 14px; line-height: 20px; font-weight: 600; }
.recovery-code { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
code { font-family: var(--font-family-mono), monospace; font-size: 13px; overflow-wrap: anywhere; user-select: all; }
@media (max-width: 520px) {
  .pairing { grid-template-columns: minmax(0, 1fr); gap: 12px; }
  .recovery-code { flex-wrap: wrap; }
}
</style>

<!-- Naive UI 将卡片挂在页面外，选择器直接限定到这一个模态框。 -->
<style>
.n-card.factor-dialog {
  width: min(440px, calc(100% - 32px));
  max-height: calc(100dvh - 32px);
}
.n-card.factor-dialog-binding { width: min(560px, calc(100% - 32px)); }
.factor-dialog > .n-card-header, .factor-dialog > .n-card__footer { flex-shrink: 0; }
.factor-dialog > .n-card__footer {
  border-top: 1px solid var(--n-border-color);
  padding-top: 14px;
  padding-bottom: 14px;
}
</style>
