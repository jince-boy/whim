<script setup lang="ts">
import WhimPassword from '@/components/password/WhimPassword.vue'
import Icon from '@/components/icon/Icon.vue'
import WhimImageCropper from '@/components/imageCropper/WhimImageCropper.vue'
import type { UserFormProps } from './hooks/types'
import { useUserForm } from './hooks/useUserForm'

const props = defineProps<UserFormProps>()
const {
  formRef, formModel, rules, isEdit, isSelf, organizationReadonly, selfEditNotice, statusOptions, genderOptions,
  avatarName, avatarText, avatarUrl, selectAvatar, changeAvatar, removeAvatar,
  avatarSource, avatarPreviewUrl, avatarPreviewShown, avatarPreviewLoading, openAvatarPreview,
  avatarCropFile, avatarCropVisible, confirmAvatarCrop,
  departmentOptions, departmentLoading, departmentError, canLoadDepartments, reloadDepartments,
  canLoadPosts, postOptions, postLoading, postError, searchPosts, loadMorePosts, retryPosts,
} = useUserForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-alert v-if="departmentError" type="error" style="margin-bottom: 16px">
      {{ departmentError }} <n-button text type="info" @click="reloadDepartments">重新加载部门</n-button>
    </n-alert>
    <n-alert v-if="isSelf" type="info" style="margin-bottom: 16px">{{ selfEditNotice }}</n-alert>
    <n-grid :cols="2" :x-gap="16" item-responsive>
      <n-form-item-gi label="头像" path="avatar" span="2">
        <div class="user-avatar">
          <n-button text class="user-avatar__preview" :disabled="!avatarUrl || avatarPreviewLoading" :loading="avatarPreviewLoading"
            :aria-label="`查看${avatarName}的头像`" :title="avatarUrl ? '查看头像' : undefined" @click="openAvatarPreview(avatarSource)">
            <n-avatar :round="false" :src="avatarUrl" :size="80" :img-props="{ alt: avatarName }" object-fit="cover">
              <template v-if="!avatarUrl" #default>{{ avatarText }}</template>
              <template #fallback>{{ avatarText }}</template>
            </n-avatar>
          </n-button>
          <div class="user-avatar__details">
            <n-flex align="center" :size="8">
              <n-button type="primary" secondary @click="selectAvatar">
                <template #icon><Icon name="upload" /></template>
                {{ formModel.avatar || formModel.avatarFile ? '更换头像' : '上传头像' }}
              </n-button>
              <n-button v-if="formModel.avatar || formModel.avatarFile" type="error" secondary @click="removeAvatar">
                <template #icon><Icon name="trash" /></template>
                移除
              </n-button>
            </n-flex>
            <n-text depth="3" class="user-avatar__hint">支持 JPG、PNG、GIF，大小不超过 5MB<br />选择后可裁剪，保存后生效</n-text>
          </div>
          <input ref="avatarInput" type="file" accept="image/jpeg,image/png,image/gif" hidden @change="changeAvatar" />
        </div>
      </n-form-item-gi>
      <n-form-item-gi label="用户名" path="username" span="2 560:1">
        <n-input v-model:value="formModel.username" :disabled="isEdit" :maxlength="32" :input-props="{ autocomplete: 'off', name: 'managedUsername' }" placeholder="3～32位字母、数字或下划线" />
      </n-form-item-gi>
      <n-form-item-gi label="姓名" path="name" span="2 560:1">
        <n-input v-model:value="formModel.name" :maxlength="24" :input-props="{ autocomplete: 'off', name: 'managedName' }" placeholder="请输入姓名" clearable />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isEdit" label="初始密码" path="password" span="2 560:1">
        <WhimPassword v-model:value="formModel.password" :user-inputs="[formModel.username, formModel.name, formModel.email ?? '']" :input-props="{ name: 'managedPassword' }" :maxlength="72" placeholder="请输入8～72位密码" />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isEdit" label="确认密码" path="confirmPassword" span="2 560:1">
        <n-input v-model:value="formModel.confirmPassword" type="password" show-password-on="click" :input-props="{ autocomplete: 'new-password', name: 'managedPasswordConfirmation' }" :maxlength="72" placeholder="请再次输入密码" />
      </n-form-item-gi>
      <n-form-item-gi label="所属部门" path="deptId" span="2 560:1">
        <n-tree-select v-model:value="formModel.deptId" :options="departmentOptions" :loading="departmentLoading" :disabled="organizationReadonly || !canLoadDepartments || departmentLoading || !!departmentError" filterable clearable default-expand-all placeholder="未分配部门" />
      </n-form-item-gi>
      <n-form-item-gi label="岗位" path="postIds" span="2 560:1">
        <n-select v-model:value="formModel.postIds" multiple filterable remote clearable :options="postOptions" :loading="postLoading" :disabled="organizationReadonly || !canLoadPosts" :placeholder="canLoadPosts ? '请选择岗位，可搜索' : '暂无岗位查询权限'" @search="searchPosts" @scroll="loadMorePosts" />
      </n-form-item-gi>
      <n-gi v-if="postError" span="2">
        <n-alert type="error" style="margin-bottom: 16px">{{ postError }} <n-button text type="info" @click="retryPosts">重试</n-button></n-alert>
      </n-gi>
      <n-form-item-gi label="联系电话" path="phone" span="2 560:1">
        <n-input v-model:value="formModel.phone" :maxlength="20" placeholder="请输入联系电话" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="邮箱" path="email" span="2 560:1">
        <n-input v-model:value="formModel.email" :maxlength="64" placeholder="请输入邮箱" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="性别" path="gender" span="2 560:1">
        <n-radio-group v-model:value="formModel.gender" aria-label="性别">
          <n-space><n-radio v-for="option in genderOptions" :key="option.value" :value="option.value" :label="option.label" /></n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="状态" path="status" span="2 560:1">
        <n-radio-group v-model:value="formModel.status" :disabled="isSelf" aria-label="用户状态">
          <n-space><n-radio v-for="option in statusOptions" :key="option.value" :value="option.value" :label="option.label" /></n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" span="2">
        <n-input v-model:value="formModel.remark" type="textarea" :maxlength="255" show-count :autosize="{ minRows: 3, maxRows: 5 }" placeholder="请输入备注" />
      </n-form-item-gi>
    </n-grid>
    <WhimImageCropper v-model:show="avatarCropVisible" :file="avatarCropFile" title="裁剪头像" @confirm="confirmAvatarCrop" />
    <n-image-preview v-model:show="avatarPreviewShown" :src="avatarPreviewUrl" show-toolbar-tooltip />
  </n-form>
</template>

<style scoped>
.user-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.user-avatar__preview {
  display: flex;
  flex-shrink: 0;
  width: 80px;
  height: 80px;
}

.user-avatar__details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.user-avatar__hint {
  font-size: 12px;
  line-height: 20px;
}
</style>

