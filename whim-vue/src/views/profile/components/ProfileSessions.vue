<script setup lang="ts">
import AppIcon from '@/components/appIcon/AppIcon.vue'
import TableAction from '@/components/dataTable/TableAction.vue'
import { useLoginSessions } from '../hooks/useLoginSessions'

const { sessions, loading, loadFailed, refresh, confirmLogout, deviceIcon } = useLoginSessions()
</script>

<template>
  <n-card :bordered="false" content-style="padding: clamp(16px, 3cqi, 24px)">
    <section class="session-list" aria-label="登录会话">
      <header class="session-toolbar">
        <div class="session-heading">
          <h2>登录会话</h2>
          <n-tag v-if="sessions.length" size="small" round :bordered="false">{{ sessions.length }}</n-tag>
        </div>
        <div class="session-actions">
          <n-button secondary type="info" size="small" class="session-refresh"
            aria-label="刷新登录会话" :aria-busy="loading" :disabled="loading" @click="refresh">
            <template #icon>
              <AppIcon name="refresh-cw" :size="16" :class="{ 'refresh-spinning': loading && sessions.length > 0 }" />
            </template>
            刷新
          </n-button>
          <n-button secondary type="error" size="small" @click="confirmLogout()">
            <template #icon><AppIcon name="log-out" /></template>退出全部会话
          </n-button>
        </div>
      </header>

      <div v-if="loading && !sessions.length" class="session-feedback" role="status">
        <n-spin size="small" />正在获取登录会话…
      </div>
      <n-empty v-else-if="loadFailed" class="session-feedback" description="登录会话获取失败，请刷新重试" />
      <n-empty v-else-if="!sessions.length" class="session-feedback" description="暂无有效登录会话" />
      <n-table v-else size="small" :bordered="false" :single-line="true" class="sessions" aria-label="已登录设备">
        <thead>
          <tr>
            <th scope="col">设备 / 浏览器</th>
            <th scope="col" class="wide-field">登录 IP</th>
            <th scope="col" class="wide-field">登录时间</th>
            <th scope="col" class="wide-field">最近活动</th>
            <th scope="col" class="action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="session in sessions" :key="session.sessionId">
            <td>
              <div class="device">
                <AppIcon :name="deviceIcon(session.deviceType)" :size="16" />
                <div class="device-info">
                  <span>{{ session.os || session.deviceType || '未知设备' }}</span>
                  <span class="browser">{{ session.browser || '未知浏览器' }}</span>
                </div>
                <n-tag v-if="session.current" size="small" type="success" :bordered="false">当前会话</n-tag>
              </div>
              <details class="mobile-details">
                <summary :aria-label="`登录 IP ${session.ipAddress || '未知'}，查看登录时间和最近活动`">
                  IP {{ session.ipAddress || '未知' }} <AppIcon name="chevron-down" :size="12" />
                </summary>
                <dl>
                  <dt>登录时间</dt><dd>{{ session.loginTime }}</dd>
                  <dt>最近活动</dt><dd>{{ session.lastActiveTime || '未知' }}</dd>
                </dl>
              </details>
            </td>
            <td class="wide-field address">{{ session.ipAddress || '未知' }}</td>
            <td class="wide-field timestamp">{{ session.loginTime }}</td>
            <td class="wide-field timestamp">{{ session.lastActiveTime || '未知' }}</td>
            <td class="action">
              <TableAction :label="session.current ? '退出当前会话' : '撤销会话'"
                :icon="session.current ? 'log-out' : 'circle-x'" type="error" @click="confirmLogout(session)" />
            </td>
          </tr>
        </tbody>
      </n-table>
    </section>
  </n-card>
</template>

<style scoped>
.session-list {
  container: sessions / inline-size;
}
.session-toolbar, .session-heading, .session-actions, .device, .session-feedback {
  display: flex;
  align-items: center;
  gap: 8px;
}
.session-toolbar {
  flex-wrap: wrap;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--divider-color);
}
.session-actions {
  margin-inline-start: auto;
}
.session-toolbar h2 {
  margin: 0;
  color: var(--text-color-1);
  font-size: 16px;
  font-weight: 600;
}
.session-refresh:focus-visible {
  outline: 2px solid var(--info-color);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: no-preference) {
  .refresh-spinning {
    animation: session-refresh 1s linear infinite;
  }
}
@keyframes session-refresh {
  to { transform: rotate(1turn); }
}
.device {
  flex-wrap: wrap;
  gap: 6px 8px;
  overflow-wrap: anywhere;
}
.device > .whim-icon {
  color: var(--text-color-3);
}
.device-info {
  display: grid;
  min-width: 0;
}
.browser {
  color: var(--text-color-2);
  font-size: 12px;
}
.sessions td {
  padding-block: 7px;
}
.sessions .action {
  width: 36px;
  text-align: center;
}
.address {
  overflow-wrap: anywhere;
}
.timestamp {
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}
.session-feedback {
  justify-content: center;
  padding: 24px 0;
}
.mobile-details {
  display: none;
}
@container sessions (max-width: 780px) {
  .wide-field { display: none; }
  .mobile-details {
    display: block;
    margin: 2px 0 0 24px;
    color: var(--text-color-2);
    font-size: 12px;
    overflow-wrap: anywhere;
  }
  .mobile-details summary {
    width: fit-content;
    max-width: 100%;
    list-style: none;
    cursor: pointer;
  }
  .mobile-details summary::-webkit-details-marker { display: none; }
  .mobile-details[open] summary .whim-icon { transform: rotate(180deg); }
  .mobile-details dl {
    display: grid;
    grid-template-columns: auto 1fr;
    gap: 2px 10px;
    margin: 4px 0 0;
  }
  .mobile-details dt { color: var(--text-color-3); }
  .mobile-details dd { margin: 0; }
}
</style>
