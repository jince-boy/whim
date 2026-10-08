# 本地字体资源

普通界面使用 Inter + Noto Sans SC，代码与等宽文本使用 JetBrains Mono + Noto Sans SC，与参考字体页面的字体搭配一致。

资源来自 Fontsource 的以下可变字体包，版本均为 `5.3.0`：

- `@fontsource-variable/inter`
- `@fontsource-variable/noto-sans-sc`
- `@fontsource-variable/jetbrains-mono`

仅保留正常字形的字重可变 WOFF2、对应字形分片 CSS 和每个字体包的原始 `LICENSE`。CSS 中的字体名称带 `Variable` 后缀，用于区分可变字体，并非另一种字体。原始字体二进制文件未修改。

字体由 Vite 随应用打包并通过当前站点加载，不请求 Google Fonts。各分片保留 `unicode-range`，浏览器根据实际使用的字符加载相应资源；`font-display: swap` 允许加载过程中先显示回退字体。

公共字体变量位于 `src/assets/style/fonts.css`，原生元素与 Naive UI 组件共用；主题 getter 在读取旧缓存或修改主题色后仍应用相同字体。

来源：[Inter](https://fontsource.org/fonts/inter)、[Noto Sans SC](https://fontsource.org/fonts/noto-sans-sc)、[JetBrains Mono](https://fontsource.org/fonts/jetbrains-mono)。各字体均使用 OFL-1.1 许可证，完整许可文本保留在各自目录中，并复制到 `public/fonts/<字体名>/LICENSE`，随发布构建一同分发。
