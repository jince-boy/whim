<script setup lang="ts">
import ProfileForm from './form.vue'
import { useProfile } from './hooks/useProfile'

defineOptions({ name: 'UserProfile' })
const { user, saving, saveProfile, openPasswordDialog } = useProfile()
</script>

<template>
  <div class="profile-page">
    <n-el>
      <n-card class="profile-heading-card" :bordered="false" content-style="padding: 22px 24px">
        <header class="profile-heading">
          <h1>个人信息</h1>
          <p>管理你的头像、基本资料与账号安全</p>
        </header>
      </n-card>
      <ProfileForm v-if="user" :initial-model="user" :saving="saving" @save="saveProfile" @change-password="openPasswordDialog" />
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
  margin-bottom: 20px;
}
.profile-heading h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 34px;
}
.profile-heading p {
  margin: 6px 0 0;
  color: var(--text-color-3);
  font-size: 13px;
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
</style>
