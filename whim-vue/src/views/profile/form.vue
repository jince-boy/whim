<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import WhimImageCropper from '@/components/imageCropper/WhimImageCropper.vue'
import type { ProfileFormModel, ProfileFormProps } from './hooks/types'
import { profileRules } from './hooks/rule'
import { useProfileForm } from './hooks/useProfileForm'

defineOptions({ name: 'ProfileForm' })
const props = defineProps<ProfileFormProps>()
const emit = defineEmits<{ save: [model: ProfileFormModel]; changePassword: [] }>()
const {
  formModel, disabled, isDirty, roles, department, posts, displayName, avatarText, avatarUrl, genderOptions,
  avatarCropFile, avatarCropShown, avatarPreviewUrl, avatarPreviewShown, avatarPreviewLoading,
  selectAvatar, changeAvatar, confirmAvatarCrop, removeAvatar, previewAvatar, resetProfile, submitProfile,
} = useProfileForm(props, (model) => emit('save', model))
</script>

<template>
  <n-form ref="profileForm" :model="formModel" :rules="profileRules" label-placement="top" require-mark-placement="left" @submit.prevent="submitProfile">
    <div class="profile-editor-grid">
      <n-card :bordered="false" content-style="padding: 28px 24px">
        <div class="profile-portrait">
          <n-button v-if="avatarUrl" text class="profile-avatar-button" :loading="avatarPreviewLoading"
            :disabled="disabled || avatarPreviewLoading" aria-label="查看头像原图" title="查看头像原图" @click="previewAvatar">
            <n-avatar :round="false" :size="96" :src="avatarUrl" :img-props="{ alt: displayName }" object-fit="cover">
              <template #fallback>{{ avatarText }}</template>
            </n-avatar>
            <span class="profile-avatar-zoom"><Icon name="maximize" :size="14" /></span>
          </n-button>
          <n-avatar v-else class="profile-avatar-placeholder" :round="false" :size="96" :theme-overrides="{ fontSize: '30px' }">{{ avatarText }}</n-avatar>
          <div class="profile-identity-text">
            <h2>{{ displayName }}</h2>
            <n-text depth="3" class="profile-username">{{ props.initialModel.username }}</n-text>
          </div>
        </div>
        <div class="profile-photo-actions">
          <n-button secondary :type="formModel.avatar || formModel.avatarFile ? 'warning' : 'primary'" :disabled="disabled" @click="selectAvatar">
            <template #icon><Icon name="upload" /></template>
            {{ formModel.avatar || formModel.avatarFile ? '更换头像' : '上传头像' }}
          </n-button>
          <n-button v-if="formModel.avatar || formModel.avatarFile" secondary type="error" :disabled="disabled" @click="removeAvatar">
            <template #icon><Icon name="trash" /></template>移除
          </n-button>
        </div>
        <p class="profile-photo-hint">JPG、PNG、GIF，最大 5MB<br />选择图片后可裁剪</p>
        <input ref="avatarInput" type="file" accept="image/jpeg,image/png,image/gif" hidden @change="changeAvatar" />
        <section class="profile-role-section" :aria-label="props.initialModel.superAdministrator ? '账号身份' : '当前角色'">
          <h3 class="profile-role-label">{{ props.initialModel.superAdministrator ? '账号身份' : '当前角色' }}</h3>
          <div class="profile-tags profile-role-tags">
            <n-tag v-if="props.initialModel.superAdministrator" size="small" :bordered="false" type="primary">超级管理员</n-tag>
            <template v-else>
              <n-tag v-for="role in roles" :key="role.code" size="small" :bordered="false" type="primary">{{ role.name }}</n-tag>
              <n-text v-if="!roles.length" depth="3" class="profile-role-empty">暂未分配角色</n-text>
            </template>
          </div>
        </section>
      </n-card>

      <div class="profile-details">
        <n-card :bordered="false" content-style="padding: 24px">
          <div class="profile-section-heading"><Icon name="user-round" :size="20" /><h2>基本资料</h2></div>
          <div class="profile-form-fields">
            <n-grid :cols="2" :x-gap="20" item-responsive>
              <n-form-item-gi label="姓名" path="name" span="2 560:1">
                <n-input v-model:value="formModel.name" :disabled="disabled" :maxlength="24" placeholder="请输入姓名" :input-props="{ autocomplete: 'name' }" />
              </n-form-item-gi>
              <n-form-item-gi label="性别" path="gender" span="2 560:1">
                <n-radio-group v-model:value="formModel.gender" :disabled="disabled" aria-label="性别">
                  <n-space><n-radio v-for="option in genderOptions" :key="option.value" :value="option.value" :label="option.label" /></n-space>
                </n-radio-group>
              </n-form-item-gi>
              <n-form-item-gi label="手机号码" path="phone" span="2 560:1">
                <n-input v-model:value="formModel.phone" :disabled="disabled" :maxlength="20" clearable placeholder="请输入手机号码" :input-props="{ autocomplete: 'tel', inputmode: 'tel' }" />
              </n-form-item-gi>
              <n-form-item-gi label="电子邮箱" path="email" span="2 560:1">
                <n-input v-model:value="formModel.email" :disabled="disabled" :maxlength="64" clearable placeholder="请输入电子邮箱" :input-props="{ autocomplete: 'email', inputmode: 'email' }" />
              </n-form-item-gi>
            </n-grid>
          </div>
          <footer class="profile-form-actions">
            <n-text depth="3" class="profile-draft-status" aria-live="polite">{{ isDirty ? '有未保存的修改' : '' }}</n-text>
            <n-space :size="8">
              <n-button secondary :disabled="disabled || !isDirty" @click="resetProfile">重置</n-button>
              <n-button type="warning" attr-type="submit" :loading="props.saving" :disabled="disabled || !isDirty || avatarCropShown">
                <template #icon><Icon name="save" /></template>保存修改
              </n-button>
            </n-space>
          </footer>
        </n-card>
        <n-card :bordered="false" content-style="padding: 24px">
          <div class="profile-section-heading"><Icon name="network" :size="20" /><h2>组织信息</h2></div>
          <dl class="profile-organization-fields">
            <div><dt>所属部门</dt><dd>{{ department }}</dd></div>
            <div><dt>任职岗位</dt><dd class="profile-tags">
              <n-tag v-for="(post, index) in posts" :key="index" size="small" :bordered="false">{{ post }}</n-tag>
              <span v-if="!posts.length" class="profile-value-empty">暂未分配岗位</span>
            </dd></div>
          </dl>
        </n-card>
        <n-card :bordered="false" content-style="padding: 24px">
          <div class="profile-section-heading"><Icon name="shield-check" :size="20" /><h2>账号安全</h2></div>
          <div class="profile-password-entry">
            <div class="profile-password-description">
              <h3>登录密码</h3>
              <n-text depth="3">验证原密码后修改，修改成功需重新登录</n-text>
            </div>
            <n-button secondary type="warning" :disabled="disabled" @click="emit('changePassword')">
              <template #icon><Icon name="key-round" /></template>修改密码
            </n-button>
          </div>
        </n-card>
      </div>
    </div>
    <WhimImageCropper v-model:show="avatarCropShown" :file="avatarCropFile" title="裁剪头像" @confirm="confirmAvatarCrop" />
    <n-image-preview v-model:show="avatarPreviewShown" :src="avatarPreviewUrl" show-toolbar-tooltip />
  </n-form>
</template>

<style scoped lang="scss">
.profile-editor-grid {
  display: grid;
  grid-template-columns: 288px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}
.profile-portrait {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.profile-identity-text {
  min-width: 0;
  max-width: 100%;
}
.profile-identity-text h2 {
  margin: 18px 0 4px;
  font-size: 21px;
  font-weight: 600;
  line-height: 30px;
  overflow-wrap: anywhere;
}
.profile-username {
  font-size: 13px;
  overflow-wrap: anywhere;
}
.profile-avatar-placeholder {
  color: var(--primary-color);
  background-color: color-mix(in srgb, var(--primary-color) 12%, var(--card-color));
  font-weight: 500;
  flex-shrink: 0;
}
.profile-avatar-button {
  position: relative;
  width: 96px;
  height: 96px;
  flex-shrink: 0;
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
.profile-photo-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}
.profile-photo-hint {
  margin: 12px 0 0;
  color: var(--text-color-3);
  text-align: center;
  font-size: 12px;
  line-height: 20px;
}
.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.profile-role-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--divider-color);
  text-align: center;
}
.profile-role-label {
  margin: 0 0 12px;
  color: var(--text-color-3);
  font-size: 12px;
  font-weight: 400;
  line-height: 18px;
}
.profile-role-tags {
  justify-content: center;
}
.profile-role-empty {
  font-size: 13px;
  line-height: 24px;
}
.profile-details {
  display: grid;
  gap: 20px;
  min-width: 0;
}
.profile-section-heading {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--divider-color);
}
.profile-section-heading > .whim-icon {
  color: var(--primary-color);
}
.profile-section-heading h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}
.profile-form-fields {
  padding-top: 24px;
}
.profile-form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--divider-color);
}
.profile-draft-status {
  font-size: 12px;
}
.profile-organization-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin: 20px 0 0;
}
.profile-organization-fields > div {
  min-width: 0;
}
.profile-organization-fields dt {
  margin-bottom: 7px;
  color: var(--text-color-3);
  font-size: 12px;
  line-height: 18px;
}
.profile-organization-fields dd {
  margin: 0;
  font-size: 14px;
  line-height: 22px;
  overflow-wrap: anywhere;
}
.profile-value-empty {
  color: var(--text-color-3);
}
.profile-password-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  padding-top: 20px;
}
.profile-password-description h3 {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 500;
}
.profile-password-description .n-text {
  font-size: 12px;
  line-height: 20px;
}
@container (max-width: 760px) {
  .profile-editor-grid {
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
  }
  .profile-portrait {
    flex-direction: row;
    gap: 16px;
    text-align: left;
  }
  .profile-identity-text h2 {
    margin-top: 0;
    font-size: 20px;
  }
  .profile-photo-actions {
    justify-content: flex-start;
  }
  .profile-photo-hint {
    text-align: left;
  }
  .profile-role-section {
    text-align: left;
  }
  .profile-role-tags {
    justify-content: flex-start;
  }
  .profile-organization-fields {
    grid-template-columns: minmax(0, 1fr);
  }
  .profile-form-actions {
    justify-content: flex-end;
  }
  .profile-draft-status {
    width: 100%;
  }
}
</style>
