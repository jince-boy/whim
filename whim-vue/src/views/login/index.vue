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
} = useLogin()
</script>

<template>
  <n-config-provider :theme="null" preflight-style-disabled>
    <n-flex justify="center" align="center" class="login-container">
      <n-card class="login-box" hoverable>
        <h2>{{ systemSetting.title }}</h2>
        <n-alert v-if="isMockEnabled" type="info" :show-icon="false" class="mock-hint">
          本地测试模式：账号 admin，密码 admin123，验证码 3
        </n-alert>
        <n-form
          ref="formRef"
          :label-width="80"
          :model="form"
          :rules="loginRules"
          size="medium"
          :show-label="false"
        >
          <n-form-item path="username">
            <n-input
              v-model:value="form.username"
              placeholder="用户名"
              :input-props="{ autocomplete: 'new-password' }"
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
              :input-props="{ autocomplete: 'new-password' }"
            >
              <template #prefix>
                <n-icon :component="passwordIcon" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="验证码" path="captcha">
            <n-space :wrap="false">
              <n-input
                v-model:value="form.captcha"
                placeholder="验证码"
                :input-props="{ autocomplete: 'new-password' }"
              >
                <template #prefix>
                  <n-icon :component="captchaIcon" />
                </template>
              </n-input>
              <n-image
                width="100"
                height="34"
                :src="captcha"
                preview-disabled
                lazy
                class="captcha"
                alt="验证码"
                @click="getCaptcha"
              />
            </n-space>
          </n-form-item>
          <n-form-item>
            <n-checkbox v-model:checked="form.rememberMe">记住密码</n-checkbox>
          </n-form-item>
          <n-spin :show="spinShow">
            <n-form-item :show-feedback="false">
              <n-button type="primary" class="login-button" style="width: 100%" @click="handleLogin"
                >登录</n-button
              >
            </n-form-item>
          </n-spin>
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
    vertical-align: middle;
    cursor: pointer;
  }

  .mock-hint {
    margin-bottom: 20px;
  }
}

.login-button {
  width: 100%;
}
</style>
