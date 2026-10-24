<script setup lang="ts">
import {useLogin} from '@/views/login/hooks/useLogin.ts'
import {loginRules} from '@/views/login/hooks/rule.ts'
import systemSetting from '@/config/SystemSetting.ts'

defineOptions({
  name: 'LoginPage',
})

const {
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
    <div class="login-container">
      <header class="login-header">
        <div class="login-brand">
          <img src="@/assets/images/logo.png" alt="logo"/>
          <span>{{ systemSetting.title }}</span>
        </div>
        <span class="login-header-caption">工作，从这里开始</span>
      </header>
      <main class="login-main">
        <section class="login-intro" aria-labelledby="login-intro-title">
          <p class="login-eyebrow"><span aria-hidden="true"></span> 简单一点，专注更多</p>
          <h1 id="login-intro-title">让工作有序，<br/><span>让灵感发生。</span></h1>
          <p class="login-description">{{ systemSetting.description }}</p>
          <p class="login-note">留一点从容，给每一个新的开始。</p>
        </section>
        <div class="login-panel">
          <n-card class="login-box" hoverable>
            <h2>{{ systemSetting.title }}</h2>
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
                  :maxlength="32"
                  :input-props="{ autocomplete: 'username', 'aria-label': '用户名' }"
                >
                  <template #prefix>
                    <n-icon :component="userIcon"/>
                  </template>
                </n-input>
              </n-form-item>
              <n-form-item path="password">
                <n-input
                  type="password"
                  v-model:value="form.password"
                  placeholder="密码"
                  show-password-on="mousedown"
                  :maxlength="72"
                  :input-props="{ autocomplete: 'current-password', 'aria-label': '密码' }"
                >
                  <template #prefix>
                    <n-icon :component="passwordIcon"/>
                  </template>
                </n-input>
              </n-form-item>
              <n-form-item label="验证码" path="captchaCode">
                <n-space :wrap="false">
                  <n-input
                    v-model:value="form.captchaCode"
                    placeholder="验证码"
                    :maxlength="4"
                    :input-props="{ autocomplete: 'off', 'aria-label': '验证码', spellcheck: false }"
                  >
                    <template #prefix>
                      <n-icon :component="captchaIcon"/>
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
                        <n-skeleton width="100px" height="34px"/>
                      </template>
                      <template #error>
                        <span class="captcha-retry">点击重试</span>
                      </template>
                    </n-image>
                    <n-skeleton v-else-if="captchaLoading" width="100px" height="34px"/>
                    <span v-else class="captcha-retry" aria-live="polite">
                      点击重试
                    </span>
                    <span
                      v-if="captcha && captchaLoading"
                      class="captcha-loading"
                      aria-hidden="true"
                    >
                      <n-spin :size="16"/>
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
                >登录
                </n-button
                >
              </n-form-item>
            </n-form>
          </n-card>
          <p class="login-panel-note"><span aria-hidden="true"></span> 欢迎回来，开启今天的工作</p>
        </div>
      </main>
      <footer class="login-footer">
        <span class="copyright">{{ systemSetting.copyright }}</span>
      </footer>
    </div>
  </n-config-provider>
</template>

<style scoped lang="scss">
.login-container {
  position: relative;
  isolation: isolate;
  display: grid;
  grid-template-rows: auto 1fr auto;
  box-sizing: border-box;
  min-height: 100vh;
  overflow: clip;
  padding: 32px clamp(24px, 5vw, 88px) 24px;
  color: #253d34;
  background: radial-gradient(ellipse at 6% 85%, #dfede2 0, transparent 48%),
  radial-gradient(ellipse at 94% 12%, #e4efe5 0, transparent 42%), #f6f8f4;
  line-height: 1.5;

  &::before {
    position: absolute;
    z-index: -1;
    inset: 0;
    background-image: linear-gradient(#668b7410 1px, transparent 1px),
    linear-gradient(90deg, #668b7410 1px, transparent 1px);
    background-size: 64px 64px;
    mask-image: linear-gradient(135deg, #0009, transparent 65%);
    content: '';
  }

  &::after {
    position: absolute;
    z-index: -1;
    top: -330px;
    right: -200px;
    width: 760px;
    height: 760px;
    border: 1px solid #8aaa8424;
    border-radius: 50%;
    box-shadow: 0 0 0 72px #8aaa8408,
    0 0 0 144px #8aaa8405;
    content: '';
  }
}

.login-header,
.login-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 21px;
  font-weight: 650;
  letter-spacing: -0.5px;

  img {
    width: 36px;
    height: 36px;
  }
}

.login-header-caption,
.login-footer {
  color: #617266;
  font-size: 12px;
}

.login-header-caption {
  letter-spacing: 2px;
}

.login-main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  align-items: center;
  gap: clamp(48px, 7vw, 112px);
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 64px 0;
}

.login-intro {
  min-width: 0;

  h1 {
    margin: 24px 0 20px;
    font-size: clamp(38px, 3.8vw, 56px);
    font-weight: 600;
    line-height: 1.4;
    letter-spacing: -2px;

    span {
      display: inline-block;
      color: #4f8466;
    }
  }
}

.login-eyebrow {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #63816d;
  font-size: 12px;
  letter-spacing: 3px;

  span {
    width: 24px;
    height: 1px;
    background: #7b9c82;
  }
}

.login-description {
  margin: 0;
  color: #687b6d;
  font-size: 15px;
  line-height: 1.8;
}

.login-note {
  margin: 32px 0 0;
  color: #6b7d6f;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-panel {
  position: relative;
  width: 100%;

  &::before {
    position: absolute;
    z-index: -1;
    inset: -12px 12px 42px -12px;
    border: 1px solid #b7cdbb55;
    border-radius: 24px;
    transform: rotate(-3deg);
    background: #ffffff38;
    content: '';
  }
}

.login-panel-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 24px 0 0;
  color: #657869;
  font-size: 12px;
  letter-spacing: 1px;

  span {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #81a58a;
  }
}

.login-box {
  max-width: 400px;
  min-height: 389.59px;
  border: 1px solid #fff;
  border-radius: 20px;
  background: #ffffffed;
  box-shadow: 0 24px 64px -24px #34583a30,
  0 4px 16px #34583a05;

  &:hover {
    box-shadow: 0 24px 64px -24px #34583a30,
    0 4px 16px #34583a05;
  }

  :deep(.n-input__placeholder) {
    line-height: 1.6;
  }

  h2 {
    margin: 12px auto;
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
      outline: 2px solid #4f8466;
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

}

.login-button {
  width: 100%;
}

.login-footer {
  justify-content: flex-end;
}

.copyright {
  text-align: right;
  overflow-wrap: anywhere;
}

@media (min-width: 901px) and (max-height: 800px) {
  .login-main {
    padding: 36px 0;
  }
}

@media (max-width: 900px) {
  .login-container {
    padding: 28px 24px 20px;
  }

  .login-main {
    grid-template-columns: minmax(0, 400px);
    justify-content: center;
    gap: 36px;
    padding: 48px 0;
  }

  .login-intro {
    text-align: center;

    h1 {
      margin: 14px 0 12px;
      font-size: 32px;
      letter-spacing: -1px;

      br {
        display: none;
      }
    }
  }

  .login-eyebrow {
    justify-content: center;
    font-size: 11px;
    letter-spacing: 2px;
  }

  .login-note {
    display: none;
  }

  .login-description {
    font-size: 13px;
  }

  .login-footer {
    justify-content: center;
  }

  .copyright {
    text-align: center;
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 20px 20px 16px;
  }

  .login-header-caption {
    display: none;
  }

  .login-brand {
    gap: 8px;
    font-size: 18px;

    img {
      width: 30px;
      height: 30px;
    }
  }

  .login-main {
    gap: 28px;
    padding: 32px 0;
  }

  .login-intro h1 {
    font-size: 26px;
  }

  .login-panel-note {
    margin-top: 18px;
    font-size: 11px;
  }
}
</style>
