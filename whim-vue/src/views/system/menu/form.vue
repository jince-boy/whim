<script setup lang="ts">
import type { MenuFormProps } from './hooks/types'
import { useMenuForm } from './hooks/useMenuForm'
import { WhimSelectIcon } from '@/components/icon'

const props = defineProps<MenuFormProps>()
const {
  formRef, formModel, rules, parentOptions, componentOptions,
  semanticsLocked, typeLockReason, statusLockReason, isPage, isButton, isExternal,
  changeType,
} = useMenuForm(props)

defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" label-placement="top" :model="formModel" :rules="rules" require-mark-placement="left">
    <n-form-item label="上级菜单" path="parentId">
      <n-tree-select
        v-model:value="formModel.parentId"
        :options="parentOptions"
        :default-expand-all="true"
        filterable
        :placeholder="isButton ? '请选择所属页面' : '请选择上级目录，或设为顶级菜单'"
      />
    </n-form-item>
    <n-form-item label="菜单类型" path="menuType">
      <n-flex vertical :size="8">
        <n-radio-group :value="formModel.menuType" :disabled="!!typeLockReason" @update:value="changeType">
          <n-radio-button v-for="item in props.dictionaryOptions.menuTypes" :key="item.value" :value="item.value" :label="item.label" />
        </n-radio-group>
        <n-text v-if="typeLockReason" depth="3">{{ typeLockReason }}</n-text>
      </n-flex>
    </n-form-item>
    <n-grid :cols="24" :x-gap="16" item-responsive>
      <n-form-item-gi label="菜单名称" path="menuName" span="24 560:12">
        <n-input v-model:value="formModel.menuName" placeholder="请输入菜单名称" :maxlength="64" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="显示排序" path="sort" span="24 560:12">
        <n-input-number v-model:value="formModel.sort" :min="0" :max="2147483647" :precision="0" placeholder="数值越小越靠前" style="width: 100%" />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isButton" label="菜单图标" path="icon" :span="24">
        <WhimSelectIcon v-model="formModel.icon" />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isButton && !isExternal" label="路由名称" path="routeName" span="24 560:12">
        <n-input v-model:value="formModel.routeName" placeholder="例如 SysMenu，须以字母开头" :maxlength="128" />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isButton" :label="isExternal ? '外链地址' : '路由地址'" path="path" :span="isExternal ? 24 : '24 560:12'">
        <n-input v-model:value="formModel.path" :placeholder="isExternal ? 'https://example.com' : '例如 /system/menu'" :maxlength="255" />
      </n-form-item-gi>
      <n-form-item-gi v-if="isPage" label="页面组件" path="component" :span="24">
        <n-select v-model:value="formModel.component" :options="componentOptions" filterable clearable placeholder="搜索并选择页面组件" />
      </n-form-item-gi>
      <n-form-item-gi v-if="isPage || isButton" label="权限标识" path="perms" :span="isButton ? 24 : '24 560:12'">
        <n-input v-model:value="formModel.perms" :disabled="semanticsLocked" placeholder="可留空，例如 system:menu:list" :maxlength="128" clearable />
      </n-form-item-gi>
      <n-form-item-gi v-if="isPage" label="路由参数" path="param" span="24 560:12">
        <n-input v-model:value="formModel.param" placeholder="例如 tab=basic，选填" :maxlength="255" clearable />
      </n-form-item-gi>
      <n-form-item-gi v-if="!isButton" label="显示状态" path="visible" span="24 560:8">
        <n-radio-group v-model:value="formModel.visible" aria-label="显示状态">
          <n-space>
            <n-radio v-for="item in props.dictionaryOptions.visibilities" :key="item.value" :value="item.value" :label="item.label" />
          </n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="菜单状态" path="status" span="24 560:8">
        <n-flex vertical :size="8">
          <n-radio-group v-model:value="formModel.status" aria-label="菜单状态">
            <n-space>
              <n-radio v-for="item in props.dictionaryOptions.statuses" :key="item.value" :value="item.value" :label="item.label" :disabled="item.value === 1 && !!statusLockReason" />
            </n-space>
          </n-radio-group>
          <n-text v-if="statusLockReason" depth="3">{{ statusLockReason }}</n-text>
        </n-flex>
      </n-form-item-gi>
      <n-form-item-gi v-if="isPage" label="页面缓存" path="isCache" span="24 560:8">
        <n-radio-group v-model:value="formModel.isCache" aria-label="页面缓存">
          <n-space>
            <n-radio v-for="item in props.dictionaryOptions.cacheModes" :key="item.value" :value="item.value" :label="item.label" />
          </n-space>
        </n-radio-group>
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" :span="24">
        <n-input v-model:value="formModel.remark" type="textarea" placeholder="补充说明（选填）" :maxlength="255" :autosize="{ minRows: 2, maxRows: 4 }" show-count />
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>
