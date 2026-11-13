<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import { useProfile } from './hooks/useProfile'

defineOptions({ name: 'UserProfile' })
const {
  user, displayName, avatar, avatarText, roles, gender, department, posts, loading, refreshProfile,
  avatarPreviewUrl, avatarPreviewShown, avatarPreviewLoading, previewAvatar,
} = useProfile()
</script>

<template>
  <div class="profile-page">
    <n-el class="profile-content">
      <header class="profile-heading">
        <div>
          <h1>个人信息</h1>
          <p>你的个人资料与组织信息</p>
        </div>
        <n-button secondary type="info" :loading="loading" @click="refreshProfile">
          <template #icon><Icon name="refresh-cw" /></template>
          刷新资料
        </n-button>
      </header>

      <n-spin :show="loading">
        <div v-if="user" class="profile-grid">
          <n-card class="profile-identity" :bordered="false" content-style="padding: 32px 24px" :theme-overrides="{ borderRadius: '12px' }">
            <div class="profile-portrait">
              <n-button v-if="avatar" text class="profile-avatar-button" :loading="avatarPreviewLoading"
                :disabled="avatarPreviewLoading" aria-label="查看头像原图" title="查看头像原图" @click="previewAvatar">
                <n-avatar :round="false" :size="88" :src="avatar" :img-props="{ alt: displayName }" object-fit="cover">
                  <template #fallback>{{ avatarText }}</template>
                </n-avatar>
                <span class="profile-avatar-zoom"><Icon name="maximize" :size="14" /></span>
              </n-button>
              <n-avatar v-else class="profile-avatar-placeholder" :round="false" :size="88" :theme-overrides="{ fontSize: '30px' }">{{ avatarText }}</n-avatar>
              <h2>{{ displayName }}</h2>
              <n-text depth="3" class="profile-username">{{ user.username }}</n-text>
            </div>
            <div class="profile-role-section">
              <div class="profile-role-label"><Icon name="shield-check" :size="16" /><span>当前角色</span></div>
              <div class="profile-roles">
                <n-tag v-for="role in roles" :key="role.code" size="small" :bordered="false" type="primary">{{ role.name }}</n-tag>
                <n-text v-if="!roles.length" depth="3">暂无角色</n-text>
              </div>
            </div>
          </n-card>

          <div class="profile-details">
            <n-card :bordered="false" content-style="padding: 24px" :theme-overrides="{ borderRadius: '12px' }">
              <div class="profile-section-heading"><Icon name="contact-round" :size="20" /><h2>基本资料</h2></div>
              <dl class="profile-fields">
                <div><dt>姓名</dt><dd>{{ user.name || '未填写' }}</dd></div>
                <div><dt>性别</dt><dd>{{ gender }}</dd></div>
                <div><dt>手机号码</dt><dd :class="{ 'profile-value-empty': !user.phone }">{{ user.phone || '未填写' }}</dd></div>
                <div><dt>电子邮箱</dt><dd :class="{ 'profile-value-empty': !user.email }">{{ user.email || '未填写' }}</dd></div>
              </dl>
            </n-card>
            <n-card :bordered="false" content-style="padding: 24px" :theme-overrides="{ borderRadius: '12px' }">
              <div class="profile-section-heading"><Icon name="network" :size="20" /><h2>组织信息</h2></div>
              <dl class="profile-fields">
                <div><dt>所属部门</dt><dd>{{ department }}</dd></div>
                <div>
                  <dt>任职岗位</dt>
                  <dd class="profile-posts">
                    <n-tag v-for="(post, index) in posts" :key="index" size="small" :bordered="false">{{ post }}</n-tag>
                    <span v-if="!posts.length" class="profile-value-empty">暂未分配岗位</span>
                  </dd>
                </div>
              </dl>
            </n-card>
          </div>
        </div>
        <n-card v-else :bordered="false" class="profile-empty">
          <n-empty description="暂未获取到个人信息">
            <template #extra><n-button secondary type="info" :loading="loading" @click="refreshProfile">重新加载</n-button></template>
          </n-empty>
        </n-card>
      </n-spin>
      <n-image-preview v-model:show="avatarPreviewShown" :src="avatarPreviewUrl" show-toolbar-tooltip />
    </n-el>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  container-type: inline-size;
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 20px 32px;
  box-sizing: border-box;
}

.profile-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;

  h1 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    line-height: 34px;
  }

  p {
    margin: 6px 0 0;
    color: var(--text-color-3);
    font-size: 13px;
  }
}

.profile-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.profile-identity,
.profile-details {
  min-width: 0;
}

.profile-portrait {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  h2 {
    max-width: 100%;
    margin: 20px 0 4px;
    font-size: 21px;
    font-weight: 600;
    line-height: 30px;
    overflow-wrap: anywhere;
  }
}

.profile-username {
  max-width: 100%;
  font-size: 13px;
  overflow-wrap: anywhere;
}

.profile-avatar-placeholder {
  color: var(--primary-color);
  background-color: color-mix(in srgb, var(--primary-color) 12%, var(--card-color));
  font-weight: 500;
}

.profile-avatar-button {
  position: relative;
  width: 88px;
  height: 88px;
}

.profile-avatar-zoom {
  position: absolute;
  right: 4px;
  bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #fff;
  background: rgb(0 0 0 / 45%);
  border-radius: 4px;
}

.profile-role-section {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid var(--divider-color);
}

.profile-role-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: var(--text-color-3);
  font-size: 12px;
}

.profile-roles,
.profile-posts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-details {
  display: grid;
  gap: 20px;
}

.profile-section-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--divider-color);

  > .whim-icon {
    color: var(--primary-color);
  }

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
  }
}

.profile-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px 32px;
  margin: 20px 0 0;

  > div {
    min-width: 0;
  }

  dt {
    margin-bottom: 7px;
    color: var(--text-color-3);
    font-size: 12px;
    line-height: 18px;
  }

  dd {
    margin: 0;
    font-size: 14px;
    line-height: 22px;
    overflow-wrap: anywhere;
  }
}

.profile-value-empty {
  color: var(--text-color-3);
}

.profile-empty {
  padding: 36px 0;
}

@media (max-width: 900px) {
  .profile-page {
    padding: 16px 8px 24px;
  }

  .profile-grid {
    grid-template-columns: 250px minmax(0, 1fr);
    gap: 16px;
  }

  .profile-fields {
    column-gap: 20px;
  }
}

@container (max-width: 760px) {
  .profile-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .profile-heading {
    align-items: flex-start;
  }

  .profile-heading h1 {
    font-size: 21px;
  }

  .profile-heading p {
    max-width: 160px;
  }

  .profile-fields {
    grid-template-columns: minmax(0, 1fr);
    gap: 20px;
  }
}
</style>
