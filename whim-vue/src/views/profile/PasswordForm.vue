<script setup lang="ts">
import WhimPassword from '@/components/password/WhimPassword.vue'
import type { ProfilePasswordFormProps } from './hooks/types'
import { useProfilePasswordForm } from './hooks/useProfilePasswordForm'

defineOptions({ name: 'ProfilePasswordForm' })
const props = defineProps<ProfilePasswordFormProps>()
const { formRef, formModel, rules } = useProfilePasswordForm()
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left" @submit.prevent>
    <n-alert type="info" :show-icon="false" class="password-notice">修改成功后，当前账号的所有登录会话将退出，请使用新密码重新登录。</n-alert>
    <n-form-item label="原密码" path="oldPassword">
      <n-input v-model:value="formModel.oldPassword" type="password" show-password-on="click" :maxlength="72" placeholder="请输入原密码" :input-props="{ autocomplete: 'current-password', name: 'currentPassword' }" />
    </n-form-item>
    <n-form-item label="新密码" path="password">
      <WhimPassword v-model:value="formModel.password" :user-inputs="[props.user.username, props.user.name ?? '', props.user.email ?? '']" :maxlength="72" placeholder="请输入8～72位新密码" :input-props="{ name: 'profileNewPassword' }" />
    </n-form-item>
    <n-form-item label="确认新密码" path="confirmPassword">
      <n-input v-model:value="formModel.confirmPassword" type="password" show-password-on="click" :maxlength="72" placeholder="请再次输入新密码" :input-props="{ autocomplete: 'new-password', name: 'profilePasswordConfirmation' }" />
    </n-form-item>
  </n-form>
</template>

<style scoped>
.password-notice {
  margin: 4px 0 22px;
}
</style>
