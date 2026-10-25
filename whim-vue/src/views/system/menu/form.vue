<script setup lang="ts">
import { MenuType, type MenuFormProps } from '@/views/system/menu/hooks/types.ts'
import { useMenuForm } from '@/views/system/menu/hooks/useMenuForm.ts'
import { WhimSelectIcon } from '@/components/icon'

const props = defineProps<MenuFormProps>()
const {
  formRef,
  formModel,
  parentOptions,
  changeType,
  rules,
  semanticsLocked,
  menuTypeOptions,
  showStatusOptions,
  runStatusOptions,
  cacheStatusOptions,
} = useMenuForm(props)

defineExpose({
  formRef,
  formModel,
})
</script>

<template>
  <n-form
    label-align="right"
    ref="formRef"
    label-width="85"
    label-placement="left"
    :model="formModel"
    :rules="rules"
    require-mark-placement="left"
  >
    <n-form-item label="上级菜单" path="parentId">
      <n-tree-select
        :options="parentOptions"
        label-field="label"
        key-field="key"
        v-model:value="formModel.parentId"
      />
    </n-form-item>
    <n-form-item label="菜单类型" path="menuType">
      <n-radio-group
        v-model:value="formModel.menuType"
        :disabled="semanticsLocked"
        @update:value="changeType"
      >
        <n-radio-button
          v-for="item in menuTypeOptions"
          :key="item.value"
          :value="Number(item.value)"
          :label="item.label"
        />
      </n-radio-group>
    </n-form-item>
    <n-form-item
      label="菜单图标"
      v-if="
        formModel.menuType === MenuType.DIRECTORY ||
        formModel.menuType === MenuType.MENU ||
        formModel.menuType === MenuType.EXTERNAL
      "
      path="icon"
    >
      <WhimSelectIcon v-model="formModel.icon" />
    </n-form-item>
    <n-grid :cols="2" :x-gap="12">
      <n-form-item-gi label="菜单名称" path="menuName">
        <n-input
          v-model:value="formModel.menuName"
          type="text"
          placeholder="请输入菜单名称"
          clearable
        />
      </n-form-item-gi>
      <n-form-item-gi label="外链地址" v-if="formModel.menuType === MenuType.EXTERNAL" path="path">
        <n-input v-model:value="formModel.path" type="text" placeholder="请输入外链地址" />
      </n-form-item-gi>
      <n-form-item-gi
        label="权限标识"
        v-if="formModel.menuType === MenuType.BUTTON || formModel.menuType === MenuType.MENU"
        path="perms"
      >
        <n-input
          v-model:value="formModel.perms"
          :disabled="semanticsLocked"
          type="text"
          placeholder="可留空，例如system:menu:list"
        />
      </n-form-item-gi>
      <n-form-item-gi
        label="路由名称"
        v-if="formModel.menuType === MenuType.DIRECTORY || formModel.menuType === MenuType.MENU"
        path="routeName"
      >
        <n-input v-model:value="formModel.routeName" type="text" placeholder="请输入路由名称" />
      </n-form-item-gi>
      <n-form-item-gi
        label="路由地址"
        v-if="formModel.menuType === MenuType.DIRECTORY || formModel.menuType === MenuType.MENU"
        path="path"
      >
        <n-input v-model:value="formModel.path" type="text" placeholder="请输入路由地址" />
      </n-form-item-gi>
      <n-form-item-gi label="组件路径" v-if="formModel.menuType === MenuType.MENU" path="component">
        <n-input v-model:value="formModel.component" type="text" placeholder="请输入组件路径" />
      </n-form-item-gi>
      <n-form-item-gi label="路由参数" v-if="formModel.menuType === MenuType.MENU" path="param">
        <n-input v-model:value="formModel.param" type="text" placeholder="路由参数" />
      </n-form-item-gi>
      <n-form-item-gi label="菜单顺序" path="sort">
        <n-input-number style="width: 100%" v-model:value="formModel.sort" :min="0" :max="9999" />
      </n-form-item-gi>
      <n-form-item-gi
        label="显示状态"
        v-if="formModel.menuType === MenuType.DIRECTORY || formModel.menuType === MenuType.MENU"
        path="visible"
      >
        <n-tabs
          v-model:value="formModel.visible"
          size="small"
          type="segment"
          animated
          style="width: 100px"
        >
          <n-tab v-for="item in showStatusOptions" :key="item.value" :name="Number(item.value)">{{
            item.label
          }}</n-tab>
        </n-tabs>
      </n-form-item-gi>
      <n-form-item-gi label="菜单状态" path="status">
        <n-tabs
          v-model:value="formModel.status"
          size="small"
          type="segment"
          animated
          style="width: 100px"
        >
          <n-tab v-for="item in runStatusOptions" :key="item.value" :name="Number(item.value)">{{
            item.label
          }}</n-tab>
        </n-tabs>
      </n-form-item-gi>
      <n-form-item-gi label="缓存状态" v-if="formModel.menuType === MenuType.MENU" path="isCache">
        <n-tabs
          v-model:value="formModel.isCache"
          size="small"
          type="segment"
          animated
          style="width: 100px"
        >
          <n-tab v-for="item in cacheStatusOptions" :key="item.value" :name="Number(item.value)">{{
            item.label
          }}</n-tab>
        </n-tabs>
      </n-form-item-gi>
      <n-form-item-gi label="备注" path="remark" :span="12">
        <n-input
          v-model:value="formModel.remark"
          type="textarea"
          placeholder="请输入备注"
          :autosize="{
            minRows: 3,
            maxRows: 3,
          }"
        />
      </n-form-item-gi>
    </n-grid>
  </n-form>
</template>
