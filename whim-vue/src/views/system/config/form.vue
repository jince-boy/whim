<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import WhimImageCropper from '@/components/imageCropper/WhimImageCropper.vue'
import type { ConfigFormProps } from './hooks/types'
import ConfigImage from './components/ConfigImage.vue'
import ConfigJsonEditor from './components/ConfigJsonEditor.vue'
import ConfigValueTypeTag from './components/ConfigValueTypeTag.vue'
import { useConfigForm } from './hooks/useConfigForm'

const props = defineProps<ConfigFormProps>()
const {
  formRef, formModel, selectableOptions, isSiteSetting, isSiteLogo, logoCropFile,
  valueTypeOptions, rules, changeType, newOption, selectImage, clearImage,
} = useConfigForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="top" require-mark-placement="left">
    <n-form-item label="配置名称" path="configName">
      <n-input v-model:value="formModel.configName" :disabled="props.initialModel?.builtIn" :maxlength="128" placeholder="请输入配置名称" />
    </n-form-item>
    <n-form-item label="配置键" path="configKey">
      <n-input v-model:value="formModel.configKey" :disabled="!!props.initialModel" :maxlength="128" placeholder="请输入配置键" />
    </n-form-item>
    <n-form-item label="值类型" path="valueType">
      <ConfigValueTypeTag v-if="props.initialModel?.builtIn" :value="formModel.valueType" />
      <n-radio-group v-else :value="formModel.valueType" aria-label="值类型" @update:value="changeType">
        <n-space>
          <n-radio v-for="option in valueTypeOptions" :key="option.value" :value="option.value" :label="option.label" />
        </n-space>
      </n-radio-group>
    </n-form-item>
    <n-form-item v-if="formModel.valueType === 'RADIO' && !props.initialModel?.builtIn" label="单选选项" path="valueOptions">
      <n-dynamic-input v-model:value="formModel.valueOptions" :on-create="newOption" :max="100">
        <template #default="{ value }">
          <n-flex :wrap="false" style="width: 100%">
            <n-input v-model:value="value.label" placeholder="选项名称" aria-label="选项名称" :maxlength="128" />
            <n-input v-model:value="value.value" placeholder="选项值" aria-label="选项值" :maxlength="128" />
          </n-flex>
        </template>
        <template #create-button-default>添加选项</template>
      </n-dynamic-input>
    </n-form-item>
    <n-form-item label="配置值" path="configValue">
      <n-radio-group v-if="formModel.valueType === 'RADIO'" v-model:value="formModel.configValue" aria-label="配置值">
        <n-space v-if="selectableOptions.length">
          <n-radio v-for="(option, index) in selectableOptions" :key="index" :value="option.value" :label="option.label" />
        </n-space>
        <n-text v-else depth="3">请先填写单选选项</n-text>
      </n-radio-group>
      <ConfigJsonEditor v-else-if="formModel.valueType === 'JSON'" v-model:value="formModel.configValue" />
      <n-flex v-else-if="formModel.valueType === 'IMAGE'" align="center">
        <ConfigImage :config-id="props.initialModel?.id" :file-id="formModel.configValue" :file="formModel.imageFile"
          :site-logo="isSiteLogo" :size="64" />
        <n-upload accept="image/png,image/jpeg,image/gif" :default-upload="false" :file-list="[]" :show-file-list="false"
          style="width: auto" @change="selectImage">
          <n-button secondary type="primary">
            <template #icon><Icon name="upload" /></template>
            {{ isSiteLogo ? '更换 Logo' : '选择图片' }}
          </n-button>
        </n-upload>
        <n-button v-if="formModel.configValue || formModel.imageFile" secondary type="error" @click="clearImage">
          <template #icon><Icon name="trash" /></template>
          {{ isSiteLogo ? '恢复默认 Logo' : '清空' }}
        </n-button>
      </n-flex>
      <n-input v-else v-model:value="formModel.configValue" type="textarea" :autosize="{ minRows: 2, maxRows: 8 }"
        :placeholder="isSiteSetting ? '留空使用部署默认值' : '请输入配置值'" />
      <template v-if="isSiteLogo" #feedback>选择图片后可裁剪，保存后生效</template>
      <template v-else-if="isSiteSetting" #feedback>清空并保存后使用部署默认值</template>
    </n-form-item>
    <n-form-item label="备注" path="remark">
      <n-input v-model:value="formModel.remark" :disabled="props.initialModel?.builtIn" :maxlength="255" placeholder="请输入备注" />
    </n-form-item>
    <WhimImageCropper v-if="isSiteLogo" :show="logoCropFile !== null" :file="logoCropFile" title="裁剪系统 Logo"
      @update:show="logoCropFile = null" @confirm="formModel.imageFile = $event" />
  </n-form>
</template>

