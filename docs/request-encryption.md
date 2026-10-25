# 请求加密

前端配置公钥，后端配置对应私钥。公共请求层统一加密 JSON 请求，后端自动识别并解密；不需要策略接口、凭证接口、前端接口清单或 `isEncrypt` 标识。登录接口已经接入，业务调用保持原样，响应不加密。

## 配置

公私钥均使用单行 Base64 字符串，不带 `BEGIN/END` 头尾标记，不带换行或 `\n` 转义。私钥是 PKCS#8 DER 编码，公钥是 SPKI DER 编码，RSA 长度至少 3072 位。

后端直接在 `whim-start/src/main/resources/application.yml` 中配置私钥，格式如下（此处为示意，项目配置中已经填写实际私钥）：

```yaml
whim:
  request-encryption:
    private-key: "此处填写整段单行Base64私钥"
    max-body-bytes: 65536
    requests-per-minute: 120
```

私钥直接保存在这份 YAML 中，不再引入外部配置文件。当前已填入原有私钥，与 `whim-vue/.env` 中的公钥配对。缺少私钥或格式不正确时启动失败，不自动更换密钥。

前端 `.env` 直接填写对应公钥的 Base64 字符串，格式示意：

```dotenv
VITE_REQUEST_ENCRYPTION_PUBLIC_KEY="此处填写整段单行Base64公钥"
```

上面是格式示意，不是可用密钥。公钥可以公开，会进入前端构建产物；私钥绝不能放入任何 `VITE_` 配置。公钥与私钥必须配对，多实例使用相同私钥。更换密钥时同步修改前端公钥并重新构建、部署。

## 生成公私钥

在项目根目录执行：

```shell
node scripts/generateRequestKey.mjs
```

脚本只使用 Node.js 内置密码学模块，无需安装依赖。每次生成一对新的 3072 位 RSA 密钥，输出两项可直接复制的配置：

- `private-key`：复制到 `application.yml` 的 `whim.request-encryption` 下。
- `VITE_REQUEST_ENCRYPTION_PUBLIC_KEY`：复制到 `whim-vue/.env`。

两项都为不带头尾标记、没有换行的 Base64 字符串。脚本仅输出密钥，不自动覆盖配置；需要更换时一起更新前后端。

## 业务使用

前端继续调用统一的 `request()`，例如登录仍使用：

```typescript
login({ username, password, uuid, captchaCode })
```

公共层自动处理 POST、PUT、PATCH、DELETE 中的 JSON 对象或数组。GET、无请求体请求、文件及表单上传保持原有方式。当前登录、菜单新增/修改、字典新增/修改/导出的 JSON 请求均自动加密。加密失败时终止发送，不回退明文。

后端在需要强制拒绝明文的接口上添加 `com.whim.core.annotation.RequestDecrypt`。登录已有该注解：

```java
/**
 * 解密并校验登录参数后返回认证信息，密码保持原始字符。
 *
 * @param loginDTO 登录参数
 * @return 登录结果
 */
@PostMapping("/login")
@RequestDecrypt
@XssIgnore
public Result<AuthenticationToken> login(@RequestBody @Valid AuthLoginDTO loginDTO) {
    return Result.success("登录成功", authService.login(loginDTO));
}
```

- 有注解：必须提交密文，明文或错误格式返回 HTTP 400。
- 无注解：自动解密密文，也兼容原来的业务 JSON。
- 解密发生在 XSS 清洗和 DTO 校验之前。登录使用 `@XssIgnore` 保留密码特殊字符，其他接口沿用已有清洗规则。
- `encryptedData` 为框架保留的顶层字段。JSON 请求体默认最多 65536 字节，密文按完整包装体计算。所有密文解密按客户端 IP 合计限流，默认 120 次/分钟；登录原有业务限流继续生效。

## 模块职责

- `whim-core`：注解、固定私钥配置和 JWE 密码学校验、解密。
- `whim-web`：HTTP 请求解析、注解检查、过滤器、长度限制和分布式解密限流。
- 前端公共请求层：读取配置公钥并自动加密 JSON，不发送额外网络请求。

## HTTP 格式

使用固定公钥加密下列 UTF-8 JSON：

```json
{
  "method": "POST",
  "path": "/system/auth/login",
  "body": {
    "username": "",
    "password": "",
    "uuid": "",
    "captchaCode": ""
  }
}
```

`method` 为实际 HTTP 方法的大写形式。`path` 使用实际路径变量值，不含反向代理前缀或 Servlet context path。加密请求的业务参数全部放在 Body 中，不附带查询参数或 Content-Encoding。

标准 JWE Compact 算法固定为 `RSA-OAEP-256` + `A256GCM`，每次加密由 JOSE 库生成新的 AES 密钥和 IV。受保护头包含 `alg`、`enc`、`kid`（公钥 RFC 7638 SHA-256 指纹）、`typ: whim-request+jwe` 和 `cty: application/json`。后端拒绝其他算法、错误指纹、压缩和未知关键头。

向原业务接口直接发送 `Content-Type: application/json`：

```json
{
  "encryptedData": "五段以点分隔的 JWE 密文"
}
```

登录在获取验证码后直接发送一次登录请求。其业务字段定义、校验规则及真实线上的密文包装格式均维护在 Apifox Body 中；不创建调试用例。文档示例不代表运行页已填入可用密文。

HTTP 400 表示明文被拒绝、JSON 或密文无效、请求体过大、密钥或目标不匹配，以及原有参数校验失败；HTTP 429 表示限流。身份、权限和其他业务错误沿用现有规则。响应保持原有 JSON 或文件下载格式。

应用层加密不替代 HTTPS。生产使用 HTTPS，Web Crypto 在开发时允许 localhost 安全上下文。此简化方案不提供一次性凭证或通用防重放能力，登录验证码仍按原业务流程校验和消费。业务代码不记录密码、解密请求体或密钥。

协议参考：[RFC 7516](https://www.rfc-editor.org/rfc/rfc7516)、[RFC 7638](https://www.rfc-editor.org/rfc/rfc7638)、[jose](https://github.com/panva/jose)。
