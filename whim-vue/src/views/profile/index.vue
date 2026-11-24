<script setup lang="ts">
import ProfileForm from './form.vue'
import ProfileSessions from './components/ProfileSessions.vue'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { pageTabThemeOverrides } from '@/config/NaiveTheme'
import { useProfile } from './hooks/useProfile'

defineOptions({ name: 'UserProfile' })
const { user, section, saving, saveProfile, openPasswordDialog } = useProfile()
</script>

<template>
  <div class="profile-page">
    <n-el>
      <n-card class="profile-heading-card" :bordered="false" content-style="padding: 18px 24px">
        <header class="profile-heading">
          <h1>个人信息</h1>
          <n-tabs v-if="user" v-model:value="section" class="profile-nav" type="segment" size="small"
            :theme-overrides="pageTabThemeOverrides" aria-label="个人信息栏目">
            <n-tab name="profile"><AppIcon name="user-round" />个人资料</n-tab>
            <n-tab name="sessions"><AppIcon name="monitor-smartphone" />会话与访问</n-tab>
          </n-tabs>
        </header>
      </n-card>
      <template v-if="user">
        <ProfileForm v-show="section === 'profile'" :initial-model="user" :saving="saving" @save="saveProfile" @change-password="openPasswordDialog" />
        <ProfileSessions v-if="section === 'sessions'" />
      </template>
      <n-card v-else :bordered="false" class="profile-empty"><n-empty description="暂未获取到个人信息" /></n-card>
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
.profile-heading-card {
  margin-bottom: 16px;
  border-radius: 10px;
}
.profile-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
}
.profile-nav {
  width: 248px;
  max-width: 100%;
}
.profile-nav :deep(.n-tabs-tab__label) {
  gap: 7px;
}
.profile-heading h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 34px;
}
.profile-empty {
  padding: 36px 0;
}
@media (max-width: 900px) {
  .profile-page {
    padding: 16px 8px 24px;
  }
}
@container (max-width: 760px) {
  .profile-heading h1 {
    font-size: 21px;
  }
}
@container (max-width: 450px) {
  .profile-nav {
    width: 100%;
  }
}
</style>
