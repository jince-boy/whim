<script setup lang="ts">
import { WhimTable } from '@/components/table'
import PostMembers from './components/PostMembers.vue'
import { usePost } from './hooks/usePost'

defineOptions({ name: 'Post' })
const {
  registerTable, form, canView, detail, showDetail, memberPost, showMembers, membersBusy,
  canLoadDepartments, departmentOptions, departmentNames, departmentName, statusOptions,
  openAddDialog, changeOrganizationWide,
} = usePost()
</script>

<template>
  <div>
    <WhimTable v-if="canView" @register="registerTable" @add="openAddDialog">
      <template #search>
        <n-form-item-gi label="岗位名称" path="postName">
          <n-input v-model:value="form.postName" :maxlength="64" placeholder="请输入岗位名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="岗位编码" path="postCode">
          <n-input v-model:value="form.postCode" :maxlength="64" placeholder="请输入岗位编码" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="所属部门" path="deptId">
          <n-tree-select v-if="canLoadDepartments" v-model:value="form.deptId" :options="departmentOptions" :disabled="form.organizationWide" filterable clearable placeholder="全部部门" />
          <n-tree-select v-else :options="departmentOptions" disabled placeholder="暂无部门查询权限" />
        </n-form-item-gi>
        <n-form-item-gi label="状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="岗位范围" path="organizationWide">
          <n-checkbox :checked="form.organizationWide === true" @update:checked="changeOrganizationWide">仅全组织岗位</n-checkbox>
        </n-form-item-gi>
      </template>
      <template #empty><n-empty description="暂无岗位，请新增或调整查询条件" /></template>
    </WhimTable>
    <n-empty v-else description="暂无岗位查询权限" />
    <n-modal v-model:show="showDetail" preset="card" title="岗位详情" :style="{ width: '640px', maxWidth: 'calc(100vw - 32px)', maxHeight: 'calc(100dvh - 32px)', overflowY: 'auto' }" :mask-closable="true">
        <n-descriptions v-if="detail" :column="1" label-placement="left" bordered>
          <n-descriptions-item label="岗位名称">{{ detail.postName }}</n-descriptions-item>
          <n-descriptions-item label="岗位编码">{{ detail.postCode }}</n-descriptions-item>
          <n-descriptions-item label="岗位 ID">{{ detail.id }}</n-descriptions-item>
          <n-descriptions-item label="所属部门">{{ departmentName(detail.deptId) }}</n-descriptions-item>
          <n-descriptions-item label="显示排序">{{ detail.sort }}</n-descriptions-item>
          <n-descriptions-item label="状态"><n-tag :type="detail.status === 0 ? 'success' : 'error'" :bordered="false">{{ detail.status === 0 ? '正常' : '停用' }}</n-tag></n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.remark || '—' }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ detail.createTime || '—' }}</n-descriptions-item>
          <n-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</n-descriptions-item>
        </n-descriptions>
    </n-modal>
    <n-drawer v-model:show="showMembers" :default-width="960" :min-width="360" :style="{ maxWidth: '100vw' }" :mask-closable="!membersBusy" :close-on-esc="!membersBusy" resizable>
      <n-drawer-content :title="memberPost ? `${memberPost.postName} · 岗位成员` : '岗位成员'" :closable="!membersBusy">
        <PostMembers v-if="showMembers && memberPost" :key="String(memberPost.id)" v-model:busy="membersBusy" :post="memberPost" :department-options="departmentOptions" :department-names="departmentNames" :can-load-departments="canLoadDepartments" />
      </n-drawer-content>
    </n-drawer>
  </div>
</template>
