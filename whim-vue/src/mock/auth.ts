import authData from './data/auth.json'
import { buildMenuTree } from './menu'
import { currentTime, fail, success, type MockParams } from './utils'
import { database, persistDatabase } from './database'

const captchaIds = new Set<string>()

/** 模拟验证码和登录，沿用登录表单校验及令牌存储流程。 */
export function handleAuth(path: string, method: string, data: MockParams) {
  if (path === '/system/auth/captcha' && method === 'GET') {
    const uuid = crypto.randomUUID()
    captchaIds.add(uuid)
    // 限制失效验证码数量，避免反复刷新时无限增长。
    if (captchaIds.size > 20) captchaIds.delete(captchaIds.values().next().value!)
    const image = `<svg xmlns="http://www.w3.org/2000/svg" width="100" height="34"><rect width="100" height="34" rx="4" fill="#eef2ff"/><text x="10" y="24" font-size="20" fill="#4338ca">1 + 2 = ?</text></svg>`
    return success({ uuid, image: `data:image/svg+xml;charset=utf-8,${encodeURIComponent(image)}` })
  }
  if (path === '/system/auth/login' && method === 'POST') {
    if (!captchaIds.delete(String(data.uuid)) || String(data.captcha) !== '3') {
      return { code: 400, message: '验证码错误或已失效，请重新输入', data: null }
    }
    if (
      data.username !== authData.account.username ||
      data.password !== authData.account.password
    ) {
      return { code: 400, message: '测试账号或密码错误，请使用登录页提示的账号', data: null }
    }
    database.loginLogs.unshift({
      id: crypto.randomUUID(),
      username: authData.account.username,
      browser: '本地测试',
      os: '本地测试',
      deviceType: '桌面设备',
      loginLocation: '本地开发环境',
      ipAddress: '127.0.0.1',
      status: 0,
      remark: '使用本地测试账号登录',
      loginTime: currentTime(),
    })
    persistDatabase()
    return success(
      { prefix: 'Bearer', token: `mock-${crypto.randomUUID()}`, expire: 86400 },
      '登录成功',
    )
  }
}

/** 检查模拟令牌，防止切换模式后误用真实环境的登录状态。 */
export function requireMockLogin(authorization: unknown): void {
  if (typeof authorization !== 'string' || !authorization.startsWith('Bearer mock-')) {
    fail('请使用本地测试账号重新登录', 401)
  }
}

/** 返回测试用户及当前可用菜单，隐藏按钮、禁用菜单和隐藏字典详情路由。 */
export function getMockUserInfo() {
  return success({
    user: authData.user,
    roleCode: authData.roleCode,
    permissionCode: authData.permissionCode,
    menus: buildMenuTree(database.menus.filter((menu) => menu.type !== 3 && menu.status === 0)),
  })
}
