# 前端项目规范

## 适用范围

本文件适用于 `whim-vue/` 下的前端代码，与上级 `AGENTS.md` 中适用的规则一并遵守。以下规范约束新增代码和本次修改涉及的代码；不得仅为了调整目录而批量重构无关文件。

## 规范一：项目结构与文件职责

### 1. 页面按业务功能组织

路由页面统一放在 `src/views/` 下。系统管理页面放在 `src/views/system/` 下，其他业务使用自己的业务目录，不得全部放入 `system`。

一个独立功能对应一个目录。目录名使用 lower camel case，例如 `dictType`、`loginLog`。功能目录内使用以下结构：

```text
src/views/
├── login/
│   ├── index.vue                 # 登录页面
│   └── hooks/
│       ├── useLogin.ts           # 登录页面逻辑
│       ├── api.ts                # 登录功能接口
│       ├── types.ts              # 登录功能类型
│       └── rule.ts               # 登录表单校验规则
└── system/
    └── dictType/
        ├── index.vue             # 字典类型列表页面
        ├── form.vue              # 新增、修改共用的编辑表单
        ├── components/           # 仅供该功能使用的子组件，按需创建
        │   └── DictTypeDetail.vue
        └── hooks/
            ├── useDictType.ts    # 列表页面逻辑
            ├── useDictTypeForm.ts # 编辑表单逻辑，存在编辑表单时创建
            ├── api.ts            # 当前功能的接口请求
            ├── types.ts          # 当前功能的数据类型
            └── rule.ts           # 当前功能的表单校验规则
```

该结构是文件职责示例，不是要求每个功能创建所有文件。没有编辑表单的功能不创建 `form.vue` 和表单 hook；没有校验规则的功能不创建 `rule.ts`；没有子组件的功能不创建 `components/`。禁止创建空文件或空目录占位。

### 2. 页面写在 `index.vue`

`index.vue` 是功能的路由入口，只负责页面结构、组件组合、数据绑定、事件绑定和页面样式。

- 页面使用 `<script setup lang="ts">`。
- 页面逻辑从同目录 `hooks/useXxx.ts` 引入，例如 `useDictType()`。
- `<script setup>` 中保留导入、`defineOptions` 等组件声明、hook 调用和返回值解构。
- 查询条件、分页、表格数据、加载状态、表格列配置和业务事件处理全部放入页面 hook。
- 不得在页面中直接调用接口、处理响应、组织新增或修改请求、实现删除确认和导出流程。
- 模板中可以直接调用 hook 返回的方法，或传递简单参数；不得在模板事件中编写多步业务逻辑。
- 搜索表单默认写在 `index.vue` 中，使用公共 `SearchForm` 组件；搜索状态和查询、重置逻辑放在页面 hook 中。

### 3. 编辑表单写在 `form.vue`

新增和修改使用相同字段时，统一复用当前功能目录下的 `form.vue`，不得分别复制一套表单。

- `form.vue` 负责表单字段、控件、布局、数据绑定、事件绑定和表单样式。
- 表单状态、默认值、初始化、数据回填、字段联动和表单专属事件处理放在 `hooks/useXxxForm.ts` 中。
- 校验规则放在 `hooks/rule.ts` 中，表单通过导入绑定规则，不得在模板或 Vue 文件中定义成片校验规则。
- 表单组件保留 `defineProps`、`defineEmits`、`defineModel`、`defineExpose` 等组件声明，将所需参数传入表单 hook。
- 使用现有公共表单弹窗时，表单通过 `defineExpose` 暴露 `formRef` 和 `formModel`，与 `useFormDialog` 的调用约定保持一致。
- 列表页弹窗的打开、详情请求、新增或修改接口调用、成功后刷新列表，由页面 hook 负责；`form.vue` 不直接执行这些流程。
- 登录等页面自身就是表单时，字段直接写在 `index.vue` 中，逻辑放在 `useLogin.ts` 等页面 hook 中，不额外套一层 `form.vue`。

如果一个功能确实需要多种不同表单，使用有明确含义的文件名，例如 `ResetPasswordForm.vue`，并配套 `hooks/useResetPasswordForm.ts`。不得使用 `form2.vue`、`newForm.vue` 等无法说明职责的名称。

### 4. 业务逻辑写在 `hooks/useXxx.ts`

页面和编辑表单的业务逻辑统一放在所属功能的 `hooks/` 目录中。

| 文件            | 职责                                                                         |
| --------------- | ---------------------------------------------------------------------------- |
| `useXxx.ts`     | 管理页面状态、查询条件、分页、表格列配置、数据加载、业务操作和页面生命周期。 |
| `useXxxForm.ts` | 管理编辑表单引用、表单模型、初始化、回填、字段联动和表单专属交互。           |
| `api.ts`        | 定义接口路径、请求方法、请求参数和响应类型，通过公共请求封装发送请求。       |
| `types.ts`      | 定义当前功能的查询参数、提交数据、响应数据、表单模型和其他业务类型。         |
| `rule.ts`       | 定义并导出表单校验规则；依赖运行时状态的规则使用规则工厂函数。               |

- hook 文件名与导出的组合式函数名保持一致，例如 `useDictType.ts` 导出 `useDictType()`。
- 状态在组合式函数内部创建，并通过返回对象提供给组件；不在模块顶层创建所有页面实例共用的可变业务状态。
- 组件绑定所需的状态、计算属性和处理方法由 hook 明确返回；hook 内部实现细节不必返回。
- hook 通过当前功能的 `api.ts` 调用接口，不在 hook 内直接写请求地址或重复封装 HTTP 客户端。
- 表格列的 `render` 配置允许在页面 hook 中组织按钮、标签等单元格内容；完整表单、详情区等复杂视图写成 Vue 子组件。
- 不得把所有功能放进一个全局 hook，也不得为每一个简单操作拆出一个 hook。只在职责独立或确有复用需求时继续拆分。

### 5. 接口、类型和校验规则就近维护

- 页面所属接口统一写在当前功能的 `hooks/api.ts` 中。
- `src/api/index.ts` 只维护公共 HTTP 请求封装、拦截器、下载能力和统一响应类型，不堆放业务接口。
- 当前功能的业务类型统一写在 `hooks/types.ts` 中，不在 `index.vue`、`form.vue`、`api.ts` 中重复声明相同业务类型。
- 当前功能的校验规则统一写在 `hooks/rule.ts` 中，文件名使用现有项目约定的 `rule.ts`。
- 多处使用同一业务接口或类型时，从其所属功能复用，不复制到其他功能的 `api.ts` 或 `types.ts` 中。
- 被 store 独立管理的接口和类型放在对应 store 的功能目录中，例如当前用户信息使用 `src/stores/modules/user/api.ts` 和 `types.ts`。

### 6. 公共代码与功能代码区分放置

| 目录                                  | 职责                                                                                                         |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| `src/views/<业务>/<功能>/components/` | 当前功能专用的子组件，不供其他功能直接依赖。子组件需要独立逻辑时，放在该功能的 `hooks/useXxx.ts` 中。        |
| `src/components/<组件功能>/`          | 可被多个功能使用的公共组件，例如表格、搜索表单、图标和弹窗。新增公共组件的独立逻辑放在其 `hooks/` 子目录中。 |
| `src/hooks/`                          | 多个功能共用、且不属于某个公共组件的组合式逻辑，确有需要时创建。                                             |
| `src/stores/`                         | 跨页面共享的状态，例如登录、当前用户、权限、标签页和主题。                                                   |
| `src/utils/`                          | 无组件生命周期依赖的工具函数，不存放页面状态或业务流程。                                                     |
| `src/types/`                          | 全局类型声明及真正跨功能的公共类型，不汇集所有页面类型。                                                     |
| `src/layout/`                         | 应用布局及侧栏、顶部栏、标签栏、主内容区等布局组件。                                                         |
| `src/router/`                         | 路由定义与导航守卫。                                                                                         |
| `src/directive/`                      | 公共自定义指令。                                                                                             |
| `src/config/`                         | 系统设置、主题等公共配置。                                                                                   |
| `src/plugins/`                        | 插件注册与第三方能力初始化。                                                                                 |
| `src/assets/`                         | 参与构建的图片、图标、字体和公共样式。                                                                       |
| `public/`                             | 需要保持原路径和原文件名、直接对外提供的静态资源。                                                           |

页面独有的状态留在页面 hook 中，不因为使用 Pinia 就全部放入 store。仅在确有跨页面共享需求时使用 store。提取公共能力之前，先检查现有组件、hooks、utils 和 stores，优先复用已有实现。

### 7. AI 执行约束

新增或修改前端功能时，按以下顺序确定文件归属：

1. 确认所属业务和功能目录，先读取相关现有文件。
2. 页面结构写入 `index.vue`，编辑表单结构写入 `form.vue`。
3. 页面逻辑写入 `hooks/useXxx.ts`，编辑表单逻辑写入 `hooks/useXxxForm.ts`。
4. 接口、类型和校验分别写入 `hooks/api.ts`、`types.ts` 和 `rule.ts`。
5. 确认是否已有可复用的公共能力，仅在必要时新增公共文件。

禁止为了省事把业务逻辑全部塞入 Vue 文件，禁止复制业务接口和类型，禁止一次任务中混用多套新目录约定。已有不符合规范的代码按本次任务范围逐步调整，不默认要求全项目迁移。

## 规范二：组件通信与数据所有权

本项目以 Vue 3 官方组件通信能力为基础，统一以下写法。`defineModel` 是官方推荐的组件双向绑定方式；类型式 props、命名元组 emits 和具体命名方式是本项目在官方支持的语法中选定的约定，不代表官方禁止其他有效语法。

### 1. 父传子使用类型式 props

- 使用 `defineProps<Props>()` 声明只读输入；需要默认值时使用 `withDefaults`。
- 脚本中使用 camelCase 属性名，模板传参使用 kebab-case，例如 `initialModel` 对应 `:initial-model`。
- 不使用数组式 props；普通业务组件不使用运行时对象式声明。确有自定义运行时校验需求时，允许使用对象式声明并注明原因。
- 保留 `props.xxx` 访问方式，便于将整个响应式 props 对象传给 hooks。
- 需要单独传递某个属性给 hook 时，传 getter 或 `toRef`，不得把普通属性值传入后误认为它仍会跟随父组件更新。
- 禁止直接赋值 props，也禁止通过 `toRef(props, 'xxx')`、`ref(props.xxx)` 或其他别名绕过只读边界修改传入的对象、数组。

### 2. 子传父使用类型化 emits

普通操作通知使用 `defineEmits`，事件参数采用命名元组声明：

```ts
const emit = defineEmits<{
  refresh: []
  edit: [id: string]
  delete: [ids: string[]]
}>()
```

- 不使用无参数类型约束的数组式 emits，也不新增调用签名式声明。
- 多个单词的事件名在脚本中使用 camelCase，例如 `fullScreen`；父组件模板使用 `@full-screen`。
- hook 需要发送事件时，从组件接收已声明的 emit 函数；宏声明仍保留在 Vue 文件中。
- 刷新、关闭、删除等普通操作通知不得改成 `onRefresh`、`onClose` 等函数型 props。
- emit 是通知机制，不能用 `await emit(...)` 等待父组件业务完成或获取其返回值。

### 3. 双向绑定使用 defineModel

- 自定义组件需要实时同步值时，使用 `defineModel<T>()` 或 `defineModel<T>('属性名')`。
- 不手写 `modelValue + update:modelValue + computed(get/set)` 作为双向绑定桥接。
- 模型在组件的 `<script setup>` 中声明，将模型 ref 传给 hook 管理交互；不得把 `defineModel` 放进普通 `.ts` 文件。
- 已由 `defineModel` 声明的属性和更新事件，不再在 `defineProps`、`defineEmits` 中重复声明。
- 单个主值使用默认 `v-model`；独立状态使用具名模型，例如 `v-model:page`、`v-model:page-size`、`v-model:checked-row-keys`。
- 必须由父组件控制的模型使用 `{ required: true }`，父组件初始化绑定值。允许不绑定、由组件自行维护的状态可以设置默认值；对象和数组默认值使用工厂函数。禁止给已绑定但未初始化的父状态依赖子组件默认值。
- 模型 ref 的整体赋值才会触发对应更新事件。更新受控对象或数组时创建新值，不依赖修改嵌套字段、`push`、`pop`、`splice` 等操作隐式同步。
- 表格页码、每页数量、选中行和展开行分别通过模型更新；分页总数等只读配置继续使用 props。子组件不得修改父组件传入的分页对象。
- 第三方组件继续遵循其公开的 `v-model:value`、`v-model:show` 等接口，不修改第三方实现。

### 4. 编辑表单使用独立草稿

- 需要“取消后不影响原数据”的编辑表单使用只读 `initialModel` 属性，不将它命名为 `modelValue`，也不把它当作实时双向绑定。
- 表单 hook 根据初始数据创建独立 `formModel`，新增时补齐默认字段；初始数据更新时重新回填。
- 当前菜单、字典类型和字典数据模型均为标量字段，复制对象即可隔离草稿。将来出现嵌套对象或数组时，必须同时复制可编辑的嵌套部分，不能仅浅拷贝顶层对象。
- 现有公共表单弹窗通过表单公开的 `formRef` 校验、读取 `formModel` 提交；取消时丢弃草稿，成功后由页面重新加载数据。
- 搜索表单的字段由页面维护，搜索组件只读取 model，发送 submit、reset 事件。重置事件携带默认查询值，由页面 hook 恢复自己的状态；搜索组件不得直接修改 model。

### 5. 同级与跨层组件通信

- 同一页面内的兄弟组件联动优先由共同父组件协调：子组件发送事件，父组件更新状态或调用必要的子组件公开方法。
- 登录、用户、权限、主题等跨页面共享状态使用现有 Pinia store，状态修改逻辑集中在 store 的 actions 中。
- 同级组件不得直接导入并操作彼此的组件实例，不建立隐藏的全局可变对象或自建 EventBus 传递业务状态。
- 跨多层但仅属于某个组件子树的上下文可以使用 `provide/inject`，使用有类型的 `InjectionKey` 并由提供方维护修改逻辑；不为简单父子通信引入该机制。

### 6. 组件引用、回调与属性透传

- 静态模板引用使用 `useTemplateRef<T>('引用名')`，Vue 子组件通过 `defineExpose` 公开必要接口。普通数据传递和操作通知仍使用 props、emits 或模型。
- 动态 `h()` 渲染的表单等无静态模板引用的场景使用有类型的 `shallowRef` 保存组件实例，避免对实例再次深层代理；每个动态实例独立维护引用。
- 只公开必要的校验、聚焦、刷新方法或表单提交接口，不暴露整个组件内部状态。
- `useFormDialog` 等需要等待业务结果的命令式接口允许保留 `onSubmit` 回调。调用方必须返回完整请求 Promise，调用方与封装均须正确等待；不能用普通 emit 替代需要返回值的回调。
- 封装第三方组件时使用 `inheritAttrs: false`，将 `$attrs` 显式绑定到实际的内部组件，避免属性和事件落到外层卡片或容器。
- 已声明的事件、模型更新必须明确处理；其他透传事件只转发一次。不得将 `$attrs` 当作响应式状态源缓存到 ref 或仅依赖它的 computed 中。

官方参考：[组件 v-model](https://vuejs.org/guide/components/v-model.html)、[TypeScript 与组合式 API](https://vuejs.org/guide/typescript/composition-api.html)、[Props 单向数据流](https://vuejs.org/guide/components/props.html#one-way-data-flow)、[组件事件](https://vuejs.org/guide/components/events.html)、[模板引用](https://vuejs.org/guide/essentials/template-refs.html)、[状态管理](https://vuejs.org/guide/scaling-up/state-management.html)。
