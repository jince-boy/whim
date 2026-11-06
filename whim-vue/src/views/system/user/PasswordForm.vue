<script setup lang="ts">
import WhimPassword from '@/components/password/WhimPassword.vue'
import type { PasswordFormProps } from './hooks/types'
import { usePasswordForm } from './hooks/usePasswordForm'

const props = defineProps<PasswordFormProps>()
const { formRef, formModel, rules } = usePasswordForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-alert type="info" style="margin-bottom: 16px">修改“{{ user.name }}（{{ user.username }}）”的密码，保存后该用户需要重新登录。</n-alert>
    <n-form-item label="新密码" path="password">
      <WhimPassword v-model:value="formModel.password" :user-inputs="[user.username, user.name, user.email ?? '']" :input-props="{ name: 'managedPassword' }" :maxlength="72" placeholder="请输入新密码" />
    </n-form-item>
    <n-form-item label="确认密码" path="confirmPassword">
      <n-input v-model:value="formModel.confirmPassword" type="password" show-password-on="click" :input-props="{ autocomplete: 'new-password', name: 'managedPasswordConfirmation' }" :maxlength="72" placeholder="请再次输入新密码" />
    </n-form-item>
  </n-form>
</template>

