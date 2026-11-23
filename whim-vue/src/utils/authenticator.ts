/** 手机验证器和绑定时发放的恢复码使用不同的输入格式。 */
export const authenticatorCodePattern = /^[0-9]{6}$/
export const recoveryCodePattern = /^[0-9a-f]{32}$/i
