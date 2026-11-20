<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useOperLog } from '@/views/system/operLog/hooks/useOperLog.ts'
import { useIcon } from '@/components/icon/useIcon.ts'

const { createIcon } = useIcon()

const {
  registerTable,
  form,
  canView,
  canClean,
  sysStatusOptions,
  sysOperTypeOptions,
  exportExcel,
  removeOperLogBatch,
  handleCleanOperLog,
  showModal,
  currentOperLog,
  requestSummary,
  responseSummary,
  failureReason,
  getDictOperTypeData,
  getDictStatusData,
} = useOperLog()
</script>

<template>
  <div>
    <WhimTable
      v-if="canView"
      @register="registerTable"
      @delete="removeOperLogBatch"
      @export="exportExcel"
    >
      <template #search>
        <n-form-item-gi label="模块名称" path="title">
          <n-input v-model:value="form.title" placeholder="请输入模块名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作人" path="operName">
          <n-input v-model:value="form.operName" placeholder="请输入操作人" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作结果" path="status">
          <n-select
            placeholder="全部状态"
            v-model:value="form.status"
            :options="sysStatusOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="IP 地址" path="ipAddress">
          <n-input v-model:value="form.ipAddress" placeholder="请输入操作 IP 地址" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作类型" path="logType">
          <n-select
            placeholder="请选择操作类型"
            v-model:value="form.logType"
            :options="sysOperTypeOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="操作时间" path="dateRange" :span="2">
          <n-date-picker
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            v-model:value="form.dateRange"
            type="datetimerange"
            clearable
          />
        </n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-button
          type="error"
          v-if="canClean"
          :render-icon="createIcon('trash-2')"
          @click="handleCleanOperLog"
          >清空</n-button
        >
      </template>
    </WhimTable>
    <n-card v-else><n-empty description="暂无操作日志查询权限" /></n-card>
    <n-modal
      v-model:show="showModal"
      preset="card"
      title="操作日志详情"
      size="small"
      style="width: min(800px, 94vw); max-height: min(80dvh, 44rem); overflow: clip"
      :content-style="{ display: 'flex', flexDirection: 'column', minHeight: 0, overflow: 'clip' }"
      :bordered="false"
    >
      <n-element v-if="currentOperLog" class="log-details">
        <dl class="log-metadata">
          <dt>模块名称</dt>
          <dd>
            <n-text strong
              ><n-ellipsis>{{ currentOperLog.title }}</n-ellipsis></n-text
            >
          </dd>

          <dt>操作人信息</dt>
          <dd class="operator-info">
            <n-ellipsis class="operator-text">
              {{
                [currentOperLog.operName, currentOperLog.operIp, currentOperLog.operLocation]
                  .filter(Boolean)
                  .join(' · ')
              }}
            </n-ellipsis>
            <n-tag size="small" type="info"
              ><n-ellipsis>{{ currentOperLog.operName }}</n-ellipsis></n-tag
            >
            <n-tag size="small"
              ><n-ellipsis>{{ currentOperLog.operIp }}</n-ellipsis></n-tag
            >
            <n-tag size="small" type="success"
              ><n-ellipsis>{{ currentOperLog.operLocation }}</n-ellipsis></n-tag
            >
          </dd>
          <dt>日志类型</dt>
          <dd>
            <n-tag :type="getDictOperTypeData(currentOperLog.logType)?.listClass" size="small">
              {{ getDictOperTypeData(currentOperLog.logType)?.label }}
            </n-tag>
          </dd>
          <dt>操作结果</dt>
          <dd>
            <n-tag :type="getDictStatusData(currentOperLog.status)?.listClass" size="small">
              {{ getDictStatusData(currentOperLog.status)?.label }}
            </n-tag>
          </dd>

          <dt>请求信息</dt>
          <dd class="request-info">
            <n-ellipsis
              >{{ currentOperLog.requestMethod }} {{ currentOperLog.requestUrl }}</n-ellipsis
            >
            <n-ellipsis>{{ currentOperLog.methodName }}</n-ellipsis>
          </dd>

          <dt>时间信息</dt>
          <dd class="time-info">
            <n-ellipsis>{{ currentOperLog.operTime }}</n-ellipsis>
            <n-ellipsis>耗时 {{ currentOperLog.costTime }} ms</n-ellipsis>
          </dd>

          <template v-if="failureReason">
            <dt>失败原因</dt>
            <dd class="failure-reason">
              <n-ellipsis
                line-clamp="var(--failure-lines, 4)"
                :tooltip="{
                  style: {
                    maxWidth: 'min(32rem, 80vw)',
                    whiteSpace: 'pre-wrap',
                    overflowWrap: 'anywhere',
                  },
                }"
              >
                <n-text type="error">{{ failureReason }}</n-text>
                <template #tooltip>{{ failureReason }}</template>
              </n-ellipsis>
            </dd>
          </template>
        </dl>
        <dl
          v-for="item in [
            { label: '请求摘要', value: requestSummary },
            { label: '响应摘要', value: responseSummary },
          ]"
          :key="item.label"
          class="log-summary"
        >
          <dt>{{ item.label }}</dt>
          <dd :tabindex="item.value ? 0 : undefined" :aria-label="item.label">
            <n-code v-if="item.value" :code="item.value" language="json" word-wrap />
            <n-text v-else depth="3">—</n-text>
          </dd>
        </dl>
      </n-element>
    </n-modal>
  </div>
</template>

<style scoped>
.log-details {
  --label-width: 7em;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: clip;
  border: 1px solid var(--divider-color);
  border-radius: var(--border-radius);
}

.log-details dl {
  display: grid;
  grid-template-columns: var(--label-width) minmax(0, 1fr);
  gap: 1px;
  margin: 0;
  background: var(--divider-color);
}

.log-metadata {
  flex: none;
}

.operator-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.operator-info > * {
  min-width: 0;
  max-width: 100%;
}

.operator-info :deep(.n-tag__content) {
  min-width: 0;
}

.operator-info .operator-text {
  display: none;
}

.request-info {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
}

.time-info {
  display: flex;
  flex-wrap: wrap;
  align-content: center;
  column-gap: 1em;
}

.log-summary {
  flex: 0 1 auto;
  grid-template-rows: minmax(0, 1fr);
  min-height: 2.5em;
  max-height: 14rem;
  border-top: 1px solid var(--divider-color);
}

.log-details dt,
.log-details dd {
  margin: 0;
  padding: 8px 12px;
}

.log-details dt {
  align-content: center;
  background: var(--table-header-color);
  text-align: right;
  white-space: nowrap;
}

.log-details dd {
  min-width: 0;
  background: var(--modal-color);
  overflow-wrap: anywhere;
}

.failure-reason {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-content: center;
  text-align: left;
  white-space: pre-wrap;
}

.log-summary dt {
  align-content: start;
}

.log-summary dd {
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior-y: contain;
}

@media (max-width: 540px), (max-height: 700px) {
  .failure-reason {
    --failure-lines: 2;
  }

  .log-details {
    --label-width: 6.25em;
    line-height: 1.4;
  }

  .log-details dt,
  .log-details dd {
    padding: 3px 8px;
  }
}

@media (min-width: 540px) and (max-height: 600px), (max-height: 520px) {
  .log-details .log-metadata {
    grid-template-columns: repeat(2, var(--label-width) minmax(0, 1fr));
  }

  .failure-reason {
    grid-column: 2 / -1;
  }

  .operator-info {
    flex-wrap: nowrap;
  }
}

@media (max-height: 420px) {
  .log-details {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    grid-template-rows: max-content minmax(0, auto);
  }

  .log-metadata {
    grid-column: 1 / -1;
  }

  .log-summary {
    min-height: 0;
  }
}

@media (max-width: 540px) and (max-height: 590px) {
  .log-details {
    --label-width: 5.75em;
    font-size: 12px;
  }

  .log-details dt,
  .log-details dd {
    padding: 2px 4px;
  }

  .operator-info > :not(.operator-text) {
    display: none;
  }

  .operator-info .operator-text {
    display: inline-block;
  }
}
</style>
