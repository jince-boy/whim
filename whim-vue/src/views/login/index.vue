<script setup lang="ts">
import { useLogin } from '@/views/login/hooks/useLogin.ts'
import { loginRules } from '@/views/login/hooks/rule.ts'
import systemSetting from '@/config/SystemSetting.ts'

defineOptions({
  name: 'LoginPage',
})

const {
  isMockEnabled,
  formRef,
  form,
  captcha,
  userIcon,
  passwordIcon,
  captchaIcon,
  handleLogin,
  getCaptcha,
  spinShow,
  captchaLoading,
} = useLogin()
</script>

<template>
  <n-config-provider :theme="null" preflight-style-disabled>
    <n-flex justify="center" align="center" class="login-container">
      <n-card class="login-box" hoverable>
        <h2>{{ systemSetting.title }}</h2>
        <n-alert v-if="isMockEnabled" type="info" :show-icon="false" class="mock-hint">
          本地测试模式：账号 admin，密码 admin123，验证码 0003
        </n-alert>
        <n-form
          ref="formRef"
          :label-width="80"
          :model="form"
          :rules="loginRules"
          size="medium"
          :show-label="false"
          :disabled="spinShow"
          @submit.prevent="handleLogin"
        >
          <n-form-item path="username">
            <n-input
              v-model:value="form.username"
              placeholder="用户名"
              :input-props="{ autocomplete: 'username' }"
            >
              <template #prefix>
                <n-icon :component="userIcon" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item path="password">
            <n-input
              type="password"
              v-model:value="form.password"
              placeholder="密码"
              show-password-on="mousedown"
              :input-props="{ autocomplete: 'current-password' }"
            >
              <template #prefix>
                <n-icon :component="passwordIcon" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="验证码" path="captchaCode">
            <n-space :wrap="false">
              <n-input
                v-model:value="form.captchaCode"
                placeholder="验证码"
                :maxlength="4"
                :input-props="{ autocomplete: 'off' }"
              >
                <template #prefix>
                  <n-icon :component="captchaIcon" />
                </template>
              </n-input>
              <button
                type="button"
                class="captcha"
                aria-label="刷新验证码"
                :aria-busy="captchaLoading"
                :disabled="captchaLoading || spinShow"
                @click="getCaptcha"
              >
                <n-image
                  v-if="captcha"
                  :src="captcha"
                  width="100"
                  height="34"
                  alt="验证码"
                  preview-disabled
                  class="captcha-image"
                  :class="{ 'captcha-image-loading': captchaLoading }"
                >
                  <template #placeholder>
                    <n-skeleton width="100px" height="34px" />
                  </template>
                  <template #error>
                    <span class="captcha-retry">点击重试</span>
                  </template>
                </n-image>
                <n-skeleton v-else-if="captchaLoading" width="100px" height="34px" />
                <span v-else class="captcha-retry">点击重试</span>
                <span v-if="captcha && captchaLoading" class="captcha-loading" aria-hidden="true">
                  <n-spin :size="16" />
                </span>
              </button>
            </n-space>
          </n-form-item>
          <n-form-item>
            <n-checkbox v-model:checked="form.rememberMe">保持登录</n-checkbox>
          </n-form-item>
          <n-form-item :show-feedback="false">
            <n-button
              type="primary"
              attr-type="submit"
              class="login-button"
              style="width: 100%"
              :loading="spinShow"
              :disabled="captchaLoading || !form.uuid"
              >登录</n-button
            >
          </n-form-item>
        </n-form>
      </n-card>
      <n-el class="copyright">{{ systemSetting.copyright }}</n-el>
    </n-flex>
  </n-config-provider>
</template>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  background-image: url('@/assets/images/login-bg.svg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  background-repeat: no-repeat;
  line-height: 1.5;

  h2 {
    margin: 12px auto;
  }

  .copyright {
    color: #fff;
    text-align: center;
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 20px 0;
  }
}

.login-box {
  max-width: 400px;
  min-height: 389.59px;

  :deep(.n-input__placeholder) {
    line-height: 1.6;
  }

  h2 {
    text-align: center;
  }

  .description {
    margin: 0 0 20px;
    text-align: center;
  }

  .captcha {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 34px;
    padding: 0;
    overflow: hidden;
    border: 0;
    border-radius: 4px;
    background: #f3f5f7;
    vertical-align: middle;
    cursor: pointer;

    &:disabled {
      cursor: default;
    }

    &:focus-visible {
      outline: 2px solid #18a058;
      outline-offset: 2px;
    }
  }

  .captcha-image {
    display: block;
    width: 100px;
    height: 34px;

    &.captcha-image-loading {
      opacity: 0.4;
    }
  }

  .captcha-loading {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .captcha-retry {
    color: #767c82;
    font-size: 12px;
  }

  .mock-hint {
    margin-bottom: 20px;
  }
}

.login-button {
  width: 100%;
}
</style>
