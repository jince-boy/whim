import { createApp } from 'vue'

import App from '@/App.vue'
import store from '@/stores'
import router from '@/router'
import '@/router/permission'
// 本地字体：Inter、Noto Sans SC 和 JetBrains Mono。
import '@/assets/style/fonts.css'
import naive from 'naive-ui'
// 全局样式
import '@/assets/style/style.css'
import directive from '@/directive'

const app = createApp(App)

app.use(store)
app.use(router)
app.use(naive)
// 注册指令
directive(app)
app.mount('#app')
