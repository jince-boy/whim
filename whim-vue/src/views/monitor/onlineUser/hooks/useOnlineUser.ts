import { computed, h, ref } from 'vue'
import { NTag, type DataTableColumns } from 'naive-ui'
import { useFormDialog } from '@/components/dialog'
import { TableAction, useWhimTable } from '@/components/table'
import { useUserStore } from '@/stores/modules/user'
import { fetchOnlineUserPage, kickoutOnlineUser } from './api'
import type { OnlineUserQuery, OnlineUserSession } from './types'

/** 管理在线用户列表和单终端强退确认。 */
export function useOnlineUser() {
  const user = useUserStore()
  const { openConfirmDialog } = useFormDialog()
  const canView = computed(() => user.hasPermission('monitor:onlineUser:page'))
  const canKickout = computed(() => user.hasPermission('monitor:onlineUser:kickout'))
  const totals = ref<{ accounts: number; sessions: number }>()
  let requestVersion = 0
  const ellipsis = { tooltip: true }
  const columns: DataTableColumns<OnlineUserSession> = [
    { title: '用户名', key: 'username', minWidth: 100, ellipsis },
    { title: '客户端', key: 'clientType', minWidth: 100, ellipsis },
    { title: '设备类型', key: 'deviceType', minWidth: 100, ellipsis },
    { title: '所属部门', key: 'deptName', minWidth: 96, ellipsis },
    { title: '主机 IP', key: 'ipAddress', minWidth: 134, ellipsis },
    { title: '登录地点', key: 'loginLocation', minWidth: 110, ellipsis },
    { title: '操作系统', key: 'os', minWidth: 112, ellipsis },
    { title: '浏览器', key: 'browser', minWidth: 96, ellipsis },
    { title: '登录时间', key: 'loginTime', minWidth: 168, ellipsis },
    { title: '最后活动时间', key: 'lastActiveTime', minWidth: 168, ellipsis },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 96,
      render(row) {
        if (row.current)
          return h(NTag, { size: 'small', type: 'success', bordered: false }, () => '当前会话')
        if (!canKickout.value || !row.canKickout) return '—'
        return h(TableAction, {
          label: '强制下线',
          icon: 'log-out',
          type: 'error',
          onClick: () => confirmKickout(row),
        })
      },
    },
  ]
  const [registerTable, table] = useWhimTable<OnlineUserSession, OnlineUserQuery>(() => ({
    title: '在线用户',
    columns,
    scrollX: 1280,
    renderCell: (value) => value || '—',
    rowKey: (row) => row.sessionId,
    search: {
      defaultValues: { username: '', ipAddress: '' },
      fields: [
        { key: 'username', label: '用户名' },
        { key: 'ipAddress', label: '主机 IP' },
      ],
    },
    async request({ pageNum, pageSize, username, ipAddress }) {
      const version = ++requestVersion
      totals.value = undefined
      const result = await fetchOnlineUserPage({
        pageNum,
        pageSize,
        username: username.trim() || undefined,
        ipAddress: ipAddress.trim() || undefined,
      })
      // 快速切换筛选时，汇总与表格一样只接收最后发出的请求。
      if (version === requestVersion && result.code === 200) {
        totals.value = { accounts: result.data.accountCount, sessions: result.data.total }
      }
      return result
    },
  }))

  /** 管理员确认账号、终端与地址后，只提交所选会话。 */
  function confirmKickout(row: OnlineUserSession) {
    const terminal = [row.browser, row.os].filter(Boolean).join(' / ') || '未知终端'
    openConfirmDialog({
      title: '强制下线此会话？',
      content: `账号 ${row.username}，${terminal}，主机 IP ${row.ipAddress || '未知'}。该终端需要重新登录，同一账号的其他会话仍可使用。`,
      modalProps: { type: 'error', positiveText: '确认下线' },
      onSubmit: () => kickoutOnlineUser({ userId: row.userId, sessionId: row.sessionId }),
      onSuccess: table.reload,
    })
  }

  return { registerTable, form: table.searchModel, canView, totals }
}
