<script setup lang="ts">
import { useProfile } from './hooks/useProfile'

defineOptions({ name: 'UserProfile' })
const { user, displayName, avatarText, roles, accountType, loading, refreshProfile } = useProfile()
</script>

<template>
  <n-el class="profile-page">
    <div class="profile-heading">
      <div>
        <h1>个人信息设置</h1>
        <p>查看个人资料和账号信息</p>
      </div>
      <n-button :loading="loading" @click="refreshProfile">刷新资料</n-button>
    </div>
    <n-spin :show="loading">
      <div class="profile-grid">
        <n-card class="profile-summary" :bordered="false">
          <n-avatar
            round
            :size="80"
            :src="user?.avatar || undefined"
            :img-props="{ alt: displayName }"
            object-fit="cover"
          >
            <template v-if="!user?.avatar" #default>{{ avatarText }}</template>
            <template #fallback>{{ avatarText }}</template>
          </n-avatar>
          <h2>{{ displayName }}</h2>
          <n-text depth="3">{{ user?.username }}</n-text>
          <div class="profile-roles">
            <n-tag
              v-for="role in roles"
              :key="role.code"
              size="small"
              :bordered="false"
              type="primary"
            >
              {{ role.name }}
            </n-tag>
            <n-text v-if="!roles.length" depth="3">暂无角色</n-text>
          </div>
        </n-card>
        <n-card title="基本资料" :bordered="false">
          <n-descriptions :column="1" label-placement="left" bordered>
            <n-descriptions-item label="用户名">{{ user?.username || '—' }}</n-descriptions-item>
            <n-descriptions-item label="姓名">{{ user?.name || '—' }}</n-descriptions-item>
            <n-descriptions-item label="用户 ID">{{ user?.userId ?? '—' }}</n-descriptions-item>
            <n-descriptions-item label="部门 ID">{{ user?.deptId ?? '—' }}</n-descriptions-item>
            <n-descriptions-item label="账号类型">{{ accountType }}</n-descriptions-item>
          </n-descriptions>
        </n-card>
      </div>
    </n-spin>
  </n-el>
</template>

<style scoped lang="scss">
.profile-page {
  max-width: 1080px;
  margin: 0 auto;
  color: var(--text-color);
}

.profile-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;

  h1 {
    margin: 0 0 8px;
    font-size: 24px;
    font-weight: 600;
  }

  p {
    margin: 0;
    color: var(--text-color-3);
  }
}

.profile-grid {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.profile-summary {
  text-align: center;

  h2 {
    margin: 16px 0 4px;
    font-size: 20px;
    font-weight: 600;
    overflow-wrap: anywhere;
  }
}

.profile-roles {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}

@media (max-width: 640px) {
  .profile-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .profile-heading {
    align-items: flex-start;
  }
}
</style>
