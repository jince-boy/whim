# Whim Core 核心模块

`whim-core` 是 Whim 框架的基础能力层。它不承载具体业务，却为所有业务模块提供一组稳定、统一、可复用的底座能力：统一响应模型、认证上下文抽象、通用异常、线程池自动配置、ALTCHA 配置属性、Web 请求工具、HTTP 调用工具、对象转换、金额、日期、IP、脱敏、密码加密等常用工具。

如果把 Whim 看成一个完整的后台框架，`whim-core` 就是最先被其他模块依赖的“公共语言”。业务模块不需要关心底层认证框架怎么取用户、全局异常怎么组织响应、IP 归属地数据怎么加载，只需要使用 `whim-core` 暴露出来的清晰入口。

## 模块定位

`whim-core` 当前主要提供 6 类能力：

| 能力 | 说明 | 典型入口 |
| --- | --- | --- |
| 统一返回 | 统一 API 响应结构，支持成功、失败、参数校验、文件响应 | `Result` |
| 认证抽象 | 面向业务层暴露当前登录用户，不绑定具体认证实现 | `AuthenticationContext` |
| 自动配置 | 提供通用计算任务线程池和 ALTCHA 参数绑定 | `ThreadPoolAutoConfiguration`、`AltchaProperties` |
| 通用异常 | 定义业务、HTTP、认证、文件、锁等基础异常类型 | `ServiceException`、`HttpException` 等 |
| 通用工具 | 提供金额、日期、HTTP、Servlet、IP、脱敏、密码、随机值等工具 | `AmountUtils`、`DateUtils`、`RestClientUtils` 等 |
| 跨模块基础模型 | 定义当前用户、角色等认证侧共享模型 | `UserInfo`、`RoleInfo` |

`whim-core` 不直接处理 Controller 路由、全局异常拦截、Sa-Token 登录、MyBatis-Plus 自动填充等完整流程。这些能力由 `whim-web`、`whim-satoken`、`whim-mybatisplus` 等模块完成，但它们都会依赖 `whim-core` 中的统一模型和抽象。

## 快速开始

在 Whim 多模块项目内，业务模块通常不需要单独管理版本，直接依赖 `whim-core` 即可：

```xml
<dependency>
    <groupId>com.whim</groupId>
    <artifactId>whim-core</artifactId>
</dependency>
```

控制器中返回统一结果：

```java
@GetMapping("/profile")
public Result<UserProfileVO> getProfile() {
    UserProfileVO profile = userService.getProfile();
    return Result.success("查询成功", profile);
}
```

业务服务中获取当前登录用户：

```java
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final AuthenticationContext authenticationContext;

    @Override
    public UserProfileVO getProfile() {
        Long userId = authenticationContext.getCurrentUserInfo().getUserId();
        return loadProfile(userId);
    }
}
```

`AuthenticationContext` 的具体实现由 `whim-satoken` 提供。业务层只依赖这个抽象，因此后续即使认证实现升级，业务代码也不需要跟着改。

## 统一返回 Result

`Result<T>` 是 Whim 的统一 API 响应模型，固定包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `int` | HTTP 状态码风格的业务响应码 |
| `message` | `String` | 响应消息 |
| `data` | `T` | 响应数据 |

常用写法：

```java
return Result.success();
return Result.success("保存成功");
return Result.success(userInfo);
return Result.success("查询成功", userInfo);
return Result.error("保存失败");
return Result.validationError("参数校验失败");
return Result.unauthorized("请先登录");
return Result.permissionDenied("用户没有权限");
```

当你需要控制 HTTP 状态码时，可以配合 `toResponseEntity()`：

```java
return Result.error(HttpStatus.NOT_FOUND, "用户不存在").toResponseEntity();
```

参数校验错误可以返回字段级明细：

```java
List<Result.ValidationError> errors = List.of(
        Result.fieldError("username", "用户名不能为空"),
        Result.fieldError("password", "密码长度不能小于 8 位")
);
return Result.validationError("参数校验失败", errors);
```

文件下载场景可以使用：

```java
return Result.file(resource);
```

`Result.file(Resource resource)` 会根据资源推断 `Content-Type`，并尽量设置 `Content-Length`，适合普通文件资源响应。

## 认证上下文

`AuthenticationContext` 是 Whim 对当前登录用户的统一抽象：

```java
public interface AuthenticationContext {

    UserInfo getCurrentUserInfo();

    boolean isLogin();

    boolean isSuperAdministrator();
}
```

它的设计目标很明确：业务代码不要直接依赖 Sa-Token API。业务只表达“我要当前用户”，而不是“我要从哪个 Token 框架、哪个 Session 字段里拿用户”。

当前登录用户模型为 `UserInfo`：

| 字段 | 说明 |
| --- | --- |
| `userId` | 用户 ID |
| `username` | 用户名 |
| `deptId` | 当前部门 ID |
| `permissionCodeSet` | 当前用户权限编码集合 |
| `roleCodeSet` | 当前用户角色编码集合 |
| `roleInfoList` | 当前用户角色信息集合 |

角色模型为 `RoleInfo`：

| 字段 | 说明 |
| --- | --- |
| `id` | 角色 ID |
| `name` | 角色名称 |
| `code` | 角色编码 |
| `dataScope` | 数据权限范围 |

推荐用法：

```java
if (!authenticationContext.isLogin()) {
    throw new UserNotFoundException("当前用户未登录");
}

UserInfo userInfo = authenticationContext.getCurrentUserInfo();
```

框架内的衔接关系：

| 模块 | 与 `AuthenticationContext` 的关系 |
| --- | --- |
| `whim-core` | 定义抽象和用户模型 |
| `whim-satoken` | 提供基于 Sa-Token 的实现 |
| `whim-mybatisplus` | 在自动填充审计字段时读取当前用户 |
| 业务模块 | 注入 `AuthenticationContext` 获取当前用户 |

## 线程池自动配置

`whim-core` 提供一个可选的计算任务线程池，Bean 名称为 `computeTaskExecutor`。只有当 `thread-pool.enabled=true` 时才会自动创建。

示例配置：

```yaml
thread-pool:
  enabled: true
  thread-name-prefix: whim-async-task-
  core-pool-size: 8
  max-pool-size: 16
  queue-capacity: 128
  keep-alive-seconds: 300
  allow-core-thread-timeout: false
  wait-for-tasks-to-complete-on-shutdown: true
  await-termination-seconds: 10
```

配置项说明：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `thread-pool.enabled` | `false` | 是否启用通用计算任务执行器 |
| `thread-pool.thread-name-prefix` | `whim-async-task-` | 线程名前缀 |
| `thread-pool.core-pool-size` | `可用处理器数 + 1` | 核心线程数 |
| `thread-pool.max-pool-size` | `corePoolSize * 2` | 最大线程数 |
| `thread-pool.queue-capacity` | `128` | 队列容量 |
| `thread-pool.keep-alive-seconds` | `300` | 非核心线程空闲存活秒数 |
| `thread-pool.allow-core-thread-timeout` | `false` | 是否允许核心线程超时回收 |
| `thread-pool.wait-for-tasks-to-complete-on-shutdown` | `true` | 关闭时是否等待任务完成 |
| `thread-pool.await-termination-seconds` | `10` | 关闭时最长等待秒数 |

使用方式：

```java
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Qualifier("computeTaskExecutor")
    private final ThreadPoolTaskExecutor computeTaskExecutor;

    @Override
    public void refreshReport() {
        computeTaskExecutor.execute(this::doRefreshReport);
    }
}
```

线程池拒绝策略使用 `CallerRunsPolicy`。当线程池和队列都满了时，任务会回退到提交线程执行，避免任务被静默丢弃。

## ALTCHA 配置

`AltchaProperties` 用于绑定 ALTCHA 验证码相关参数，配置前缀为 `whim.auth.altcha`。

```yaml
whim:
  auth:
    altcha:
      hmac-signature-secret: ${ALTCHA_SECRET:whim-altcha-dev-secret}
      algorithm: PBKDF2/SHA-256
      cost: 5000
      expires-in-seconds: 300
```

配置项说明：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `hmac-signature-secret` | `whim-altcha-dev-secret` | 挑战签名密钥，生产环境必须使用安全随机值 |
| `algorithm` | `PBKDF2/SHA-256` | ALTCHA 验证算法 |
| `cost` | `5000` | 计算成本，值越高验证成本越高 |
| `expires-in-seconds` | `300` | 挑战有效秒数 |

在业务服务中使用：

```java
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final AltchaProperties altchaProperties;

    @Override
    public void createChallenge() {
        String secret = altchaProperties.getHmacSignatureSecret();
        int cost = altchaProperties.getCost();
        // 根据业务需要创建 ALTCHA challenge
    }
}
```

生产环境请务必通过环境变量或安全配置中心覆盖 `hmac-signature-secret`，不要使用默认开发密钥。

## 通用异常

`whim-core` 定义了一组基础运行时异常，`whim-web` 会在全局异常处理器中将它们转换为统一响应。

| 异常 | 推荐场景 | 默认响应倾向 |
| --- | --- | --- |
| `ServiceException` | 普通业务处理失败 | 500 |
| `HttpException` | 远程 HTTP 调用失败 | 502 |
| `FileStorageException` | 文件上传、下载、存储失败 | 500 |
| `LockException` | 分布式锁获取或执行失败 | 业务自行处理 |
| `UserNotFoundException` | 用户不存在 | 404 |
| `UserPasswordNotMatchException` | 用户名或密码错误 | 401 |
| `UserDisableException` | 用户已被禁用 | 403 |

推荐写法：

```java
if (user == null) {
    throw new UserNotFoundException("用户不存在");
}

if (!BCryptUtils.matches(password, user.getPassword())) {
    throw new UserPasswordNotMatchException("用户名或密码错误");
}
```

异常消息会面向最终响应和日志，因此应使用清晰的中文描述。

## HTTP 调用工具

`RestClientUtils` 基于 Spring `RestClient` 封装了统一的 HTTP 调用方式，默认使用 JDK `HttpClient`，连接超时 5 秒，读取超时 30 秒，并启用 HTTP/2。

简单调用：

```java
UserInfo userInfo = RestClientUtils.get(
        "https://example.com/api/users",
        Map.of("enabled", true),
        UserInfo.class
);
```

复杂调用建议使用不可变 `Request`：

```java
RestClientUtils.Request request = RestClientUtils.Request
        .post("https://example.com/api/users/{id}", updateUserDTO)
        .withUriVariable("id", 1)
        .withHeader("Authorization", "Bearer " + token)
        .withContentType(MediaType.APPLICATION_JSON)
        .withAccept(MediaType.APPLICATION_JSON);

UserInfo userInfo = RestClientUtils.execute(request, UserInfo.class);
```

泛型响应：

```java
List<UserInfo> users = RestClientUtils.execute(
        RestClientUtils.Request.get("https://example.com/api/users"),
        new ParameterizedTypeReference<List<UserInfo>>() {
        }
);
```

当远程服务返回错误状态码，或请求执行失败时，工具会抛出 `HttpException`。如果项目引入了 `whim-web`，全局异常处理会统一返回“远程服务调用失败”。

如果需要替换默认 `RestClient`：

```java
RestClientUtils.setRestClient(customRestClient);
RestClientUtils.resetRestClient();
```

## Web 请求工具

`ServletUtils` 用于读取当前请求上下文，适合 Controller、拦截器、过滤器或框架内部使用。

常用能力：

| 方法 | 说明 |
| --- | --- |
| `currentRequest()` | 获取当前请求的 `Optional<HttpServletRequest>` |
| `getRequest()` | 获取当前请求，取不到时返回 `null` |
| `getHeader(name)` | 获取请求头 |
| `getParameter(name)` | 获取请求参数 |
| `getParameterToInt(name)` | 将请求参数转为 `Integer` |
| `getParameterToBoolean(name)` | 将请求参数转为 `Boolean` |
| `getPageNum()` | 获取 `pageNum`，默认 `1` |
| `getPageSize()` | 获取 `pageSize`，默认 `10` |
| `getRequestURL()` | 获取完整请求 URL |
| `getRequestOrigin()` | 获取请求来源 |
| `getClientAgentInfo()` | 解析浏览器、设备、系统等客户端信息 |

示例：

```java
Integer pageNum = ServletUtils.getPageNum();
Integer pageSize = ServletUtils.getPageSize();
String origin = ServletUtils.getRequestOrigin();
```

User-Agent 解析基于 Yauaa，可识别浏览器、浏览器版本、设备类型、设备品牌、操作系统、渲染引擎，并判断是否移动端或机器人访问。

## IP 工具

`IPUtils` 基于内置 `ip2region` xdb 数据文件提供离线 IP 归属地查询，同时支持从反向代理请求头中提取真实客户端 IP。

常用方法：

```java
String clientIp = IPUtils.getClientIpAddress();
String cityInfo = IPUtils.getCityInfo("8.8.8.8");
Optional<String> cityInfoOptional = IPUtils.getCityInfoOptional(clientIp);
```

内置数据文件：

| 文件 | 说明 |
| --- | --- |
| `ip2region_v4.xdb` | IPv4 离线归属地库 |
| `ip2region_v6.xdb` | IPv6 离线归属地库 |

客户端 IP 提取会依次处理 `Forwarded`、`X-Forwarded-For`、`X-Real-IP` 等常见代理头，并自动跳过 `unknown` 和非法 IP。

## 对象转换

`BeanConvertUtils` 基于 MapStruct Plus 的 `Converter` 执行对象转换：

```java
UserVO userVO = BeanConvertUtils.convert(userEntity, UserVO.class);
List<UserVO> userVOList = BeanConvertUtils.convertList(userEntityList, UserVO.class);
UserDTO userDTO = BeanConvertUtils.convertMap(sourceMap, UserDTO.class);
```

适用场景：

| 场景 | 推荐方法 |
| --- | --- |
| Entity 转 VO | `convert(source, Target.class)` |
| DTO 覆盖已有 Entity | `convert(source, target)` |
| 列表转换 | `convertList(sourceList, Target.class)` |
| Map 转对象 | `convertMap(sourceMap, Target.class)` |

业务代码中应优先保持 DTO、VO、Entity 边界清晰，不要为了省事直接把 Entity 暴露给前端。

## 密码与随机值

`BCryptUtils` 统一封装 BCrypt 密码加密：

```java
String encodedPassword = BCryptUtils.encode(rawPassword);
boolean matched = BCryptUtils.matches(rawPassword, encodedPassword);
```

`RandomStringUtils` 提供常用随机字符串：

```java
String code = RandomStringUtils.randomNumeric(6);
String token = RandomStringUtils.randomAlphaNumeric(32);
String upper = RandomStringUtils.randomUpperCase(8);
```

`IdUtils` 提供 UUID：

```java
String uuid = IdUtils.uuid();
String uuid32 = IdUtils.uuid32();
```

其中 `uuid32()` 会去掉中划线，更适合文件名、业务流水号后缀等场景。

## 金额工具

`AmountUtils` 面向金额计算和展示，统一使用 `BigDecimal`。

常用方法：

```java
BigDecimal amount = AmountUtils.parse("12.30");
BigDecimal scaled = AmountUtils.scale(amount);
long cent = AmountUtils.toCent(amount);
BigDecimal yuan = AmountUtils.fromCent(cent);
String text = AmountUtils.toChineseUppercase(amount);
```

方法速查：

| 方法 | 说明 |
| --- | --- |
| `nullToZero(amount)` | `null` 金额转为 `BigDecimal.ZERO` |
| `isZero(amount)` | 判断是否为 0 |
| `isPositive(amount)` | 判断是否大于 0 |
| `isNegative(amount)` | 判断是否小于 0 |
| `scale(amount)` | 默认保留 2 位小数 |
| `toCent(amount)` | 元转分 |
| `fromCent(cent)` | 分转元 |
| `toPlainString(amount)` | 转普通数字字符串 |
| `toTrimmedString(amount)` | 转数字字符串并去掉无意义小数 0 |
| `toChineseUppercase(amount)` | 转中文大写金额 |
| `parse(amountText)` | 文本转金额 |
| `parseToCent(amountText)` | 文本金额转分 |

金额字段不要使用 `double` 承载业务存储，计算和持久化请优先使用 `BigDecimal` 或分单位整数。

## 日期时间工具

`DateUtils` 统一封装 Java Time 与旧版 `Date` 的常用操作。

内置格式：

| 常量 | 格式 |
| --- | --- |
| `DATE_PATTERN` | `uuuu-MM-dd` |
| `TIME_PATTERN` | `HH:mm:ss` |
| `DATE_TIME_PATTERN` | `uuuu-MM-dd HH:mm:ss` |
| `COMPACT_DATE_TIME_PATTERN` | `uuuuMMddHHmmss` |
| `MILLIS_DATE_TIME_PATTERN` | `uuuu-MM-dd HH:mm:ss.SSS` |
| `COMPACT_DATE_PATTERN` | `uuuuMMdd` |
| `SLASH_DATE_PATTERN` | `uuuu/MM/dd` |
| `YEAR_MONTH_PATTERN` | `uuuuMM` |
| `YEAR_PATTERN` | `uuuu` |

常用方法：

```java
LocalDate today = DateUtils.today();
LocalDateTime now = DateUtils.currentDateTime();
String text = DateUtils.format(now, DateUtils.DATE_TIME_PATTERN);
LocalDate date = DateUtils.parseLocalDate("2026-07-05", DateUtils.DATE_PATTERN);
LocalDateTime start = DateUtils.startOfDay(date);
LocalDateTime end = DateUtils.endOfDay(date);
```

与旧版 `Date` 互转：

```java
Date date = DateUtils.toDate(LocalDateTime.now());
LocalDateTime localDateTime = DateUtils.toLocalDateTime(date);
Instant instant = DateUtils.toInstant(date);
```

对于新代码，推荐优先使用 `LocalDate`、`LocalDateTime`、`Instant`，只在兼容旧 API 或 JDBC 类型时转换为 `Date`、`Timestamp`。

## 数据转换工具

`ValueParserUtils` 用于把不确定输入转换为目标类型，适合处理请求参数、配置值、导入值等场景。

常用方法：

```java
Integer pageSize = ValueParserUtils.toInteger(input, 10);
Long userId = ValueParserUtils.toLong(input);
Boolean enabled = ValueParserUtils.toBoolean(input, false);
BigDecimal amount = ValueParserUtils.toBigDecimal(input, BigDecimal.ZERO);
```

支持类型：

| 类型 | 方法 |
| --- | --- |
| 字符串 | `toString` |
| 字符 | `toChar` |
| 整数 | `toByte`、`toShort`、`toInteger`、`toLong` |
| 浮点数 | `toFloat`、`toDouble` |
| 布尔值 | `toBoolean` |
| 大数字 | `toBigInteger`、`toBigDecimal` |
| 枚举 | `toEnum` |
| 数组 | `splitToIntegerArray`、`splitToLongArray`、`splitToStringArray` |
| 全角半角 | `toFullWidth`、`toHalfWidth` |

布尔转换支持 `true`、`1`、`yes`、`y`、`ok`、`on` 作为真值，支持 `false`、`0`、`no`、`n`、`off` 作为假值。

## 脱敏工具

`DesensitizationUtils` 用于手机号、身份证号、邮箱、姓名、银行卡等敏感信息展示。

```java
String phone = DesensitizationUtils.mask("13800138000", DesensitizationType.PHONE);
String email = DesensitizationUtils.mask("jince@example.com", DesensitizationType.EMAIL);
String custom = DesensitizationUtils.mask("abcdefg", 2, 2);
```

内置类型：

| 类型 | 规则 |
| --- | --- |
| `PHONE` | 保留前 3 位、后 4 位 |
| `ID_CARD` | 保留前 6 位、后 4 位 |
| `EMAIL` | 保留邮箱用户名首字母和完整域名 |
| `NAME` | 保留首字 |
| `BANK_CARD` | 保留前 4 位、后 4 位 |

脱敏只解决展示问题，不等同于加密存储。需要保护原始数据时，应结合数据库加密、字段权限、日志过滤等机制。

## Spring 容器工具

`SpringUtils` 用于在框架内部获取 Spring 容器、环境变量、Bean、AOP 代理等。

常用方法：

| 方法 | 说明 |
| --- | --- |
| `getApplicationContext()` | 获取应用上下文 |
| `getBean(name)` | 按名称获取 Bean |
| `getBean(Class)` | 按类型获取 Bean |
| `containsBean(name)` | 判断 Bean 是否存在 |
| `getAopProxy(invoker)` | 获取当前对象的 AOP 代理 |
| `getActiveProfiles()` | 获取当前激活 profile |
| `getRequiredProperty(key)` | 获取必需配置 |

业务代码优先使用构造器注入。只有在框架工具、静态入口、AOP 自调用等确实不方便注入的场景下，再考虑使用 `SpringUtils`。

## 缓存键常量

`CacheKeys` 用于集中维护框架级缓存键，当前包含：

```java
public static final String SYS_CONFIG = "sys_config";
```

新增缓存键时建议统一放在常量类中，避免业务代码散落硬编码字符串。

## 最佳实践

优先依赖抽象：业务层获取当前用户时注入 `AuthenticationContext`，不要直接调用 Sa-Token。

统一返回模型：Controller 返回 `Result<T>`，不要在同一个项目里混用多套响应结构。

异常要有业务含义：用户不存在抛 `UserNotFoundException`，远程调用失败抛 `HttpException`，普通业务失败抛 `ServiceException`。

金额用 `BigDecimal`：涉及金额计算、展示、转换时使用 `AmountUtils`，避免 `double` 精度问题。

时间用 Java Time：新代码优先使用 `LocalDate`、`LocalDateTime`、`Instant`，只在边界处转换旧类型。

敏感信息先脱敏再输出：手机号、身份证、银行卡、邮箱进入响应或日志前，应根据场景使用 `DesensitizationUtils`。

生产密钥必须外部化：`whim.auth.altcha.hmac-signature-secret` 不能使用默认开发值。

## 常见问题

### 为什么业务代码不要直接依赖 Sa-Token？

因为认证框架属于基础设施，业务代码关心的是“当前用户是谁”。`AuthenticationContext` 把业务语义固定下来，Sa-Token 只是当前实现。这样后续扩展多账号体系、切换认证策略、调整 Session 字段时，业务代码不需要被迫跟着改。

### `Result` 的 `code` 是 HTTP 状态码吗？

当前实现采用 HTTP 状态码风格的整数编码，例如 `200`、`400`、`401`、`403`、`500`。如果需要让实际 HTTP 响应状态也同步变化，使用 `toResponseEntity()`。

### 线程池为什么默认不启用？

线程池属于运行时资源。默认关闭可以避免应用在没有明确异步任务需求时创建多余线程。需要使用时，将 `thread-pool.enabled` 设置为 `true`。

### `RestClientUtils` 适合什么场景？

适合项目内部普通 HTTP 调用、第三方接口调用、需要统一异常处理的轻量远程访问。如果调用链路非常复杂，例如需要重试、熔断、限流、观测链路等，可以在外层组合更完整的治理能力。

### `BeanConvertUtils` 和手写转换怎么选？

字段映射清晰、规则简单时，用 `BeanConvertUtils` 可以减少重复代码。存在复杂业务规则、字段需要二次计算、权限控制或跨表组装时，建议手写转换逻辑，让业务含义更明确。

## 小结

`whim-core` 的价值不在于“工具类很多”，而在于它把框架中最容易散乱的基础能力统一成了稳定入口：

```text
Controller 统一返回 Result
业务层统一读取 AuthenticationContext
异常统一交给 whim-web 处理
HTTP 调用统一抛出 HttpException
金额、日期、脱敏、IP、密码等基础能力统一使用 core 工具
```

当业务模块遵循这些入口，Whim 的代码风格会更统一，模块之间的耦合会更低，后续扩展认证、权限、审计、日志、远程调用治理时也会更从容。
