<script setup lang="ts">
import WhimPassword from '@/components/password/WhimPassword.vue'
import { twoFactorResetRules } from './hooks/rule'
import type { AccountSecurityFormProps } from './hooks/types'
import { useAccountSecurityForm } from './hooks/useAccountSecurityForm'

const props = defineProps<AccountSecurityFormProps>()
const emit = defineEmits<{ close: []; save: [] }>()
const { activeTab, formId, formRef, formModel, passwordModel, resetModel, rules } = useAccountSecurityForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-tabs v-model:value="activeTab" type="line">
    <n-tab-pane name="password" tab="修改密码" :disabled="submitting">
      <n-form :id="`${formId}-password`" ref="passwordRef" :model="passwordModel" :rules="rules" :disabled="submitting"
        label-placement="top" require-mark-placement="left" @submit.prevent="emit('save')">
        <n-alert type="info" class="security-notice">保存新密码后，该用户需要重新登录。</n-alert>
        <n-form-item label="新密码" path="password">
          <WhimPassword v-model:value="passwordModel.password" :user-inputs="[user.username, user.name, user.email ?? '']"
            :input-props="{ name: 'managedPassword' }" :maxlength="72" placeholder="请输入新密码" />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="passwordModel.confirmPassword" type="password" show-password-on="click"
            :input-props="{ autocomplete: 'new-password', name: 'managedPasswordConfirmation' }"
            :maxlength="72" placeholder="请再次输入新密码" />
        </n-form-item>
      </n-form>
    </n-tab-pane>
    <n-tab-pane v-if="showTwoFactor" name="twoFactor" tab="两步验证" :disabled="submitting">
      <n-alert v-if="self" type="info">请在「个人信息」中查看和管理本人的两步验证。</n-alert>
      <n-text v-else-if="user.twoFactorBindingId == null">该用户尚未绑定验证器。</n-text>
      <n-form v-else :id="`${formId}-reset`" ref="resetRef" :model="resetModel" :rules="twoFactorResetRules"
        :disabled="submitting" label-placement="top" require-mark-placement="left" @submit.prevent="emit('save')">
        <n-alert type="warning" class="security-notice">
          将解除「{{ user.username }}」的验证器绑定并退出该用户的所有设备。用户可用原密码登录，再绑定新手机。
        </n-alert>
        <n-form-item label="超级管理员的当前密码" path="currentPassword">
          <n-input v-model:value="resetModel.currentPassword" type="password" :maxlength="72" placeholder="输入你自己的登录密码"
            show-password-on="mousedown" :input-props="{ autocomplete: 'current-password' }" />
        </n-form-item>
      </n-form>
    </n-tab-pane>
  </n-tabs>
  <n-flex justify="end" class="security-actions">
    <n-button :disabled="submitting" @click="emit('close')">关闭</n-button>
    <n-button v-if="activeTab === 'password'" type="warning" attr-type="submit" :form="`${formId}-password`" :loading="submitting">
      保存密码
    </n-button>
    <n-button v-else-if="showTwoFactor && !self && user.twoFactorBindingId != null" type="warning"
      attr-type="submit" :form="`${formId}-reset`" :loading="submitting">
      确认重置两步验证
    </n-button>
  </n-flex>
</template>

<style scoped>
.security-notice { margin-bottom: 16px; }
.security-actions { margin-top: 16px; }
</style>
