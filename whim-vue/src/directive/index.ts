import { permissionDirective, roleDirective } from '@/directive/permission/permission.ts'

import { type App } from 'vue'

export default (app: App) => {
  app.directive('permission', permissionDirective)
  app.directive('role', roleDirective)
}
