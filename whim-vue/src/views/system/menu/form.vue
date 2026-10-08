<script setup lang="ts">
import { MenuType, type MenuFormProps } from '@/views/system/menu/hooks/types.ts'
import { useMenuForm } from '@/views/system/menu/hooks/useMenuForm.ts'
import { menuRules } from '@/views/system/menu/hooks/rule.ts'
import WhimSelectIcon from '@/components/icon/WhimSelectIcon.vue'
import iconfont from '@/assets/iconfont/iconfont.json'
import Icon from '@/components/icon/Icon.vue'

const props = defineProps<MenuFormProps>()
const { formRef, formModel, extendedData, menuData, clearUnrelatedFields } = useMenuForm(props)

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
    :rules="menuRules"
    require-mark-placement="left"
  >
    <n-form-item label="上级菜单" path="parentId">
      <n-tree-select
        :options="menuData"
        label-field="title"
        key-field="id"
        v-model:value="formModel.parentId"
      />
    </n-form-item>
    <n-form-item label="菜单类型" path="type">
      <n-radio-group v-model:value="formModel.type" @update:value="clearUnrelatedFields">
        <n-radio-button
          v-for="item in extendedData.menuTypeDict"
          :key="item.value"
          :value="Number(item.value)"
          :label="item.label"
        />
      </n-radio-group>
    </n-form-item>
    <n-form-item
      label="菜单图标"
      v-if="
        formModel.type === MenuType.DIRECTORY ||
        formModel.type === MenuType.MENU ||
        formModel.type === MenuType.EXTERNAL
      "
      path="icon"
    >
      <WhimSelectIcon :icon-data="iconfont.glyphs" v-model="formModel.icon" />
    </n-form-item>
    <n-grid :cols="2" :x-gap="12">
      <n-form-item-gi label="菜单名称" path="title">
        <n-input v-model:value="formModel.title" type="text" placeholder="请输入菜单名称" clearable>
          <template #suffix>
            <Icon name="taiyang1" style="margin-left: 12px"></Icon>
          </template>
        </n-input>
      </n-form-item-gi>
      <n-form-item-gi label="外链地址" v-if="formModel.type === MenuType.EXTERNAL" path="redirect">
        <n-input v-model:value="formModel.redirect" type="text" placeholder="请输入外链地址" />
      </n-form-item-gi>
      <n-form-item-gi label="权限标识" v-if="formModel.type === MenuType.BUTTON" path="code">
        <n-input v-model:value="formModel.code" type="text" placeholder="请输入权限标识" />
      </n-form-item-gi>
      <n-form-item-gi
        label="路由名称"
        v-if="formModel.type === MenuType.DIRECTORY || formModel.type === MenuType.MENU"
        path="name"
      >
        <n-input v-model:value="formModel.name" type="text" placeholder="请输入路由名称" />
      </n-form-item-gi>
      <n-form-item-gi
        label="路由地址"
        v-if="formModel.type === MenuType.DIRECTORY || formModel.type === MenuType.MENU"
        path="path"
      >
        <n-input v-model:value="formModel.path" type="text" placeholder="请输入路由地址" />
      </n-form-item-gi>
      <n-form-item-gi label="组件路径" v-if="formModel.type === MenuType.MENU" path="component">
        <n-input v-model:value="formModel.component" type="text" placeholder="请输入组件路径" />
      </n-form-item-gi>
      <n-form-item-gi label="路由参数" v-if="formModel.type === MenuType.MENU" path="queryParam">
        <n-input v-model:value="formModel.queryParam" type="text" placeholder="路由参数" />
      </n-form-item-gi>
      <n-form-item-gi label="菜单顺序" path="sort">
        <n-input-number style="width: 100%" v-model:value="formModel.sort" :min="0" :max="9999" />
      </n-form-item-gi>
      <n-form-item-gi
        label="显示状态"
        v-if="formModel.type === MenuType.DIRECTORY || formModel.type === MenuType.MENU"
        path="visible"
      >
        <n-tabs
          v-model:value="formModel.visible"
          size="small"
          type="segment"
          animated
          style="width: 100px"
        >
          <n-tab
            v-for="item in extendedData.sysShowStatusDict"
            :key="item.value"
            :name="Number(item.value)"
            >{{ item.label }}</n-tab
          >
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
          <n-tab
            v-for="item in extendedData.sysRunStatusDict"
            :key="item.value"
            :name="Number(item.value)"
            >{{ item.label }}</n-tab
          >
        </n-tabs>
      </n-form-item-gi>
      <n-form-item-gi label="缓存状态" v-if="formModel.type === MenuType.MENU" path="keepAlive">
        <n-tabs
          v-model:value="formModel.keepAlive"
          size="small"
          type="segment"
          animated
          style="width: 100px"
        >
          <n-tab
            v-for="item in extendedData.sysMenuKeepAliveStatus"
            :key="item.value"
            :name="Number(item.value)"
            >{{ item.label }}</n-tab
          >
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
