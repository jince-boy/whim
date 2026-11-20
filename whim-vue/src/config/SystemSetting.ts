import { reactive } from 'vue'

export const deploymentSettings = {
  title: import.meta.env.VITE_APP_TITLE,
  description: import.meta.env.VITE_APP_DESCRIPTION,
  copyright: import.meta.env.VITE_APP_COPYRIGHT,
}
const systemSetting = reactive({ ...deploymentSettings })
export default systemSetting
