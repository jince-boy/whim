# AGENTS.md

本文件用于指导 Codex（Codex.ai/code）在本仓库中开展代码相关工作。

## 构建、运行与测试

本节命令用于说明项目的构建、运行和测试方式，仅供参考。根据下文的核心规则，智能体不得执行任何 `java` 或 `maven` 命令。

- 构建完整的多模块项目：`mvn clean package`
- 运行全部测试：`mvn test`
- 仅运行应用模块的测试：`mvn -pl whim-start test`
- 运行单个测试类：`mvn -pl whim-start -Dtest=ClassName test`
- 运行单个测试方法：`mvn -pl whim-start -Dtest=ClassName#methodName test`
- 在本地启动应用：`mvn -pl whim-start spring-boot:run`
- 跳过测试进行构建：`mvn clean package -DskipTests`

## 仓库结构

本项目是基于 Java 25 的 Maven 多模块 Spring Boot 4 项目。根目录的 `pom.xml` 聚合了三个顶层模块：

- `whim-framework`：可复用的基础设施与自动配置模块。
- `whim-modules`：业务领域模块。
- `whim-start`：整合框架模块与业务模块、可独立运行的 Spring Boot 应用。

`whim-start` 是应用入口模块，通过 `com.whim.WhimApplication` 启动应用，并依赖 `whim-web` 和业务模块 `whim-system`。

## 总体架构

大多数公共能力放在框架模块中，通过 Spring Boot 自动配置提供，避免在应用模块中手动装配。

### 框架模块

- `whim-core`：基础工具、公共异常、认证抽象和线程池配置。
- `whim-json`：Jackson 定制配置。
- `whim-web`：类型转换、全局异常处理、XSS 过滤等 MVC 与 Web 公共能力。
- `whim-redis`：基于 Redisson 的 Redis 集成与缓存管理器配置。
- `whim-satoken`：基于 Sa-Token 的认证与授权集成。
- `whim-mybatisplus`：MyBatis-Plus 配置、分页拦截器、乐观锁拦截器、防全表更新与删除拦截器，以及实体字段自动填充支持。
- 根 POM 通过 `dependencyManagement` 统一管理内部模块的版本。

多个框架模块通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置。排查功能行为时，应先检查模块的自动配置，不要直接假定 Bean 都在 `whim-start` 中声明。

### 复用 `whim-core` 工具类

编写通用辅助逻辑前，先检查 `whim-framework/whim-core/src/main/java/com/whim/core/utils`。已有工具方法满足需求时，必须直接复用，不要重复实现或新建同类工具类。阅读方法约定并选择合适的重载，不要将工具方法强行用于语义不同的场景。只有在确实缺少相关能力且该能力属于 `whim-core` 职责时，才向现有的适当工具类补充公共功能。

`com.whim.core.utils` 包当前包含以下工具类：

| 工具类 | 已有能力 |
| --- | --- |
| `AmountUtils` | 金额空值处理、正负判断、舍入、元与分转换、解析、字符串格式化和中文大写金额转换。 |
| `BCryptUtils` | BCrypt 密码编码与校验。 |
| `BeanConvertUtils` | Bean 之间的转换、列表转换和 Map 转 Bean。 |
| `DateUtils` | 日期时间格式模式、格式化、解析、运算、一天的起止时间，以及 Java 日期时间类型之间的转换。 |
| `DesensitizationUtils` | 按保留的前后缀或预定义脱敏类型对敏感文本进行脱敏。 |
| `IdUtils` | UUID 生成，包括 32 位字符形式。 |
| `IPUtils` | 客户端 IP 提取与 IP 归属地查询。 |
| `RandomStringUtils` | 生成随机字母、数字、字母数字组合及自定义字符集字符串。 |
| `RestClientUtils` | 通过 `RestClient` 发起 HTTP 请求，支持常用请求方法和可配置的请求选项。 |
| `ServletUtils` | 获取当前 Servlet 请求、请求头、参数、分页参数和用户代理信息，以及 URL 参数编码。 |
| `SpringUtils` | 在无法直接注入时访问 Spring 上下文、Bean、环境和 AOP 代理；Spring Bean 内优先使用构造器注入。 |
| `ValueParserUtils` | 将值转换为字符串、数字、布尔值和枚举；按分隔符解析数组；转换全角与半角文本。 |

### 认证流程

认证能力分为两层抽象：

- `whim-core` 定义 `AuthenticationContext`，作为业务层获取当前用户身份的接口。
- `whim-satoken` 提供具体实现（`AuthContext`）和请求拦截配置（`SaTokenConfigure`）。

`StpAuthManager` 是 Sa-Token 多账号体系的统一入口。当前虽然只注册了 `SYSTEM` 账号类型，但新增账号体系也应在此处接入。除认证框架模块自身外，业务代码和基础设施代码应依赖 `AuthenticationContext`，不要直接依赖 Sa-Token API。

`whim-satoken` 在 `com.whim.satoken.annotation` 中提供系统账号校验注解：`SystemCheckLogin` 校验登录状态，`SystemCheckPermission` 校验权限码，`SystemCheckRole` 校验角色，`SystemCheckSafe` 校验二次认证。执行相应的系统账号校验时，必须使用这些项目注解，不要重复直接使用底层 Sa-Token 注解。

`whim-satoken` 与 `whim-mybatisplus` 是相互独立、同级的基础设施模块。两者都可以依赖 `whim-core`，但禁止相互依赖。业务模块必须使用 `whim-core` 中的抽象；由 `whim-start` 作为统一装配入口，整合 Sa-Token 具体实现、业务模块与持久化模块。

### 持久化约定

`whim-mybatisplus` 统一管理数据库相关行为：

- `MybatisPlusConfiguration` 在分页处理之前应用操作级数据权限，并配置乐观锁及防全表更新与删除保护。
- `BaseEntity` 定义公共审计字段（`createBy/createTime/updateBy/updateTime/deleteBy/deleteTime`）。
- `AutoFillFieldHandler` 根据 `AuthenticationContext` 自动填充这些审计字段。

业务实体统一使用 `BIGINT NOT NULL DEFAULT 0` 的 `deleted` 字段，对应 Java `Long`。MyBatis-Plus 的 `logicDeleteValue` 配置为 `id`，删除时将本行主键写入删除标识。需要在删除后复用业务键时，使用“业务键 + deleted”的联合唯一索引，不得新增 `active_*` 生成列。未删除值必须为非空的 `0`；已删除判断使用非零，不得写死为 `1`。用户名等明确要求全局唯一且删除后不可复用的业务键仍保留全局唯一约束。

修改持久化行为时，必须同时检查实体基类和元对象处理器，因为审计字段的处理逻辑分布在这两处。

创建人、创建时间、修改人、修改时间由 MyBatis-Plus 元对象处理器自动填充，业务新增和修改方法不得手动设置或回填这些审计字段，DTO 不接收审计字段。修改请求的目标 ID 和可修改字段统一放在一个 DTO 中，控制器与服务修改方法只接收该 DTO，不拆成独立的 ID 参数和 DTO；新增 DTO 不接收主键。普通修改直接将包含 ID 的 DTO 转成一个实体后调用 `updateById`，不要逐字段回填或复制到第二个更新实体；仅在业务确实需要时读取原记录、校验归属或维护关联数据。允许清空的字段明确配置更新策略，局部更新必须保留其他字段。

持久化操作默认使用 MyBatis-Plus `IService` 提供的方法及 Lambda 条件构造器。普通增删改查、存在性判断、计数、筛选、排序、分页和批量操作，只要框架能够清晰、高效地表达，就必须直接复用，不得为此新增自定义 Mapper 方法、XML SQL 或注解 SQL，也不要直接调用 Mapper 的基础 CRUD 方法。

只有条件构造器确实难以清晰或高效表达的复杂关联、聚合或批量更新，才编写必要的自定义 SQL，并就地注释说明原因。不能将 XML 中的 SQL 搬到注解、字符串拼接或大量 `apply`、`last`、`setSql` 中来规避此约定，也不要为了消除 SQL 引入循环查询或明显增加数据库往返。修改相关逻辑时，能用框架能力直接替代的手写 SQL 应一并简化。

需要原子性更新多张表的业务操作，必须在服务层定义事务边界。数据库变更影响缓存数据时，应在写入成功后使相关缓存失效或刷新缓存；涉及事务写入时，必须将缓存变更与事务提交协调，避免事务回滚后缓存与数据库不一致。

### Web 与 API 约定

以下 Web 公共能力由 `whim-web` 统一负责，不要放在 `whim-start` 的控制器中：

- 全局异常映射。
- 字符串到 `LocalDateTime` 的类型转换。
- Servlet 过滤器与 XSS 请求包装处理。

控制器放在 `whim-start/src/main/java/com/whim/controller` 下，按业务领域划分子目录。已有的 `system` 目录用于系统接口，后续业务领域应使用各自的目录。控制器目录分组与 Maven 模块边界相互独立。

Sa-Token 拦截器默认保护 `/system/**`，仅允许未经认证的请求访问 `application.yml` 中 `sa-token.exclude-paths` 配置的路径。

控制器路由中的静态路径段必须使用小驼峰命名法，且只能包含字母和数字。禁止使用连字符、下划线或其他特殊字符；例如应使用 `/userInfo`，不要使用 `/user-info` 或 `/user_info`。允许使用 `/{userId}` 这样的路径变量占位符，变量名也必须使用小驼峰命名法。

遵循现有 API 模型：普通接口通过 `whim-web` 的 `Result<T>` 返回响应；分页接口使用 `PageQueryDTO` 和 `PageDataVO<T>`；需要校验请求 DTO 时，使用 `jakarta.validation` 和 `@Valid`。错误处理复用现有异常类型与 `GlobalExceptionHandler`，保持 HTTP 状态与 `Result.code` 一致，不要为单个接口创建独立的错误格式或编码体系。权限码命名应与相关接口保持一致；需要权限校验时，使用适当的项目授权注解。

同一业务请求已使用 DTO 时，目标 ID、关联 ID、筛选条件、分页参数和可修改字段必须统一放在该请求 DTO 中，控制器、服务接口和服务实现只接收该 DTO。此规则适用于查询、分页、候选项查询、修改、状态变更及关联操作，不限于修改接口。禁止使用 `method(Long id, XxxDTO parameter)` 等“独立 ID + DTO”的签名，也不得先单独接收 ID，再在控制器中手动 `setId` 拼回 DTO。前端请求函数同步使用一个包含 ID 的参数对象，GET 请求统一通过查询参数绑定 DTO，JSON 请求统一通过 Body 绑定 DTO。请求对象使用 DTO，不直接使用数据库 Entity。仅需 ID、没有其他请求字段的详情或删除接口可以直接接收 ID，无须为此创建空壳 DTO。

控制器的 ID 参数默认遵循大于 0 的项目约定，不添加 `@Positive`、`@Min(1)` 等重复的正数校验，也不手写 `id <= 0` 之类的校验逻辑。保留必要的参数绑定、DTO 字段校验，以及服务层的记录存在性、归属和权限检查。

仅当当前任务新增、修改、删除 API 接口，或变更其对外契约时，才调用 Apifox MCP 工具更新 `whim` 项目的接口文档。对外契约包括 HTTP 方法、路由、请求参数或请求体、响应结构、认证或权限要求，以及错误响应。只更新受影响的接口，并在任务完成前核对文档与最终实现一致。

Apifox 中所有接口均不需要调试用例，不得创建或保留调试用例；导入自动生成的调试用例也必须清理。JSON 请求参数应直接维护在接口文档的 Body 中，包含字段定义、必填项、校验规则和请求示例，不得用调试用例代替请求参数文档。

Apifox 操作使用 MCP；未经用户明确要求，不读取屏幕或操作 Apifox 客户端界面。更新请求示例后，必须通过导出接口文档确认 JSON 正文被识别，不能仅凭更新成功或说明文字存在判定完成；文档示例与运行页请求数据必须区分，未验证运行页时不得声称其输入框已填好。

对于不影响 API 契约的变更，例如前端布局或样式、内部重构、性能优化、注释或项目规则，不得调用 Apifox 或更新接口文档。是否需要同步，应以当前任务产生的变更为准；工作区中已有但与当前任务无关的变更不触发 Apifox 更新。

### 业务模块

`whim-modules/whim-system` 是当前的系统模块，依赖 `whim-core`、`whim-mybatisplus` 和 `whim-redis`，不依赖具体的 `whim-satoken` 实现。系统领域的服务、Mapper、实体、DTO 和 VO 均放在此模块；系统控制器放在 `whim-start` 的 `controller/system` 下。后续可在 `whim-modules` 中新增其他业务模块，例如 `whim-business`，不要默认所有业务领域都归属于 `whim-system`。

## 配置与业务约束

- `sys_config` 仅保存应用版本、网站标题、Logo、功能开关等业务配置，不得存放锁占位记录或用作技术锁载体。需要分布式锁时复用 `whim-redis` 已有的 Lock4j 与 Redisson 能力。

- 前端业务请求统一连接真实后端，不得引入 Mock 模式、模拟业务数据、固定验证码或演示账号提示。

- 默认运行环境通过 Maven 资源过滤设置：`spring.profiles.active: @profiles_active@`。
- 根 Maven 配置以 `dev` 为默认环境，`prod` 为可选环境。
- 应用主配置位于 `whim-start/src/main/resources/application.yml`。
- Flyway 迁移脚本位于 `whim-start/src/main/resources/db/migration`。
- 每次数据库变更都必须新增版本号更高的 Flyway 迁移脚本；禁止修改、删除或复用已经执行过的版本化迁移脚本。
- `V1__initialize.sql` 用于在空数据库中初始化单组织 RBAC（基于角色的访问控制）；已禁用自动基线化。
- 用户名全局唯一；每个用户最多有一个主部门，允许未分配部门（`deptId` 为 `null`），可以拥有多个角色和多个岗位。岗位不得隐式授予角色。
- 岗位只表达任职关系，不设置负责人；岗位表、请求、响应和页面均不包含负责人字段。岗位“本人”数据范围使用不可由业务请求修改的创建人 `create_by`，所属部门范围仍使用 `dept_id`。
- 部门先创建，再由用户管理将用户归属到该部门；部门负责人仅可从主部门为当前部门的正常用户中选择，新增部门不设置负责人。调整用户部门、停用或删除用户时，必须同步检查部门负责人关系，避免留下失效归属。
- 受数据权限保护的公共服务方法必须声明 `@DataPermission`，并准确指定操作和表映射。该注解负责启用过滤，不要在权限定义中增加重复开关。受数据范围约束的业务记录在新增和变更归属时，还必须调用 `DataPermissionContext.checkOwnership`。
- 部门新增属于组织级管理操作，通过 `system:dept:create` 功能权限授权；允许操作者没有所属部门，也不按角色数据范围限制新增位置，包括创建根部门。新增服务不声明 `@DataPermission`，不调用 `DataPermissionContext.checkOwnership`，仍须校验上级部门和组织层级，并在事务提交后失效受组织变化影响的登录授权范围。
- 每个角色只有一个数据范围，由该角色授予的所有操作共享；自定义部门保存在 `sys_role_dept` 中。只有授予当前确切操作的角色才能贡献数据范围，多个角色的数据范围取并集。范围编码为：1（全部）、2（自定义部门）、3（本部门）、4（本部门及下级部门）、5（本人）。禁止增加按操作单独覆盖数据范围的机制；不同操作需要不同范围时，应通过角色组合实现。
- 功能权限与数据权限共享登录上下文。在登录或刷新用户信息时构建权限到角色的映射，并从该上下文解析数据范围，不要为每次操作重新查询用户、权限或角色授权。授权变更后，必须在事务提交后通过现有会话失效流程使所有受影响用户的会话失效。
- 超级管理员是独立的账号身份，由用户表中的受控身份标记表达，默认拥有全部功能、有效菜单和全部数据权限，不依赖角色、角色菜单关联或角色数据范围，也不在角色管理中创建专用的超级管理员角色。普通账号继续按实际菜单授权和授予当前确切操作的角色范围校验，新增菜单需显式授权。身份判断统一读取服务端登录快照，不得散落固定用户名、用户ID、角色ID或角色编码特判；普通用户管理、个人资料和角色分配请求均不能设置或变更超级管理员身份。普通管理员不能维护超级管理员账号。
- 应用运行端口为 `8089`。
- Sa-Token 放行路径在 `application.yml` 中配置，不得在应用模块中硬编码。

## 当前测试情况

仓库中目前没有位于 `src/test/java` 的测试类。如果新增测试，应用级测试优先放在 `whim-start`，因为该模块负责装配完整的运行时依赖。


# 核心规则

- 所有代码必须基于 **JDK 25+** 和 **Spring Boot 4.1+**。
- 始终优先采用最新、前沿的现代技术，以及 Spring Boot 4 推荐的最优方案。
- 避免使用已弃用、陈旧或过时的实现方式。
- 代码必须达到专业、高水平的生产级质量标准。
- **禁止**使用 `record` 定义类。
- **禁止**使用 `var` 声明变量，必须显式写出变量类型，包括循环变量和 try-with-resources 资源变量。
- 使用语义明确的命名，避免缩写。
- 没有明确必要时，不要将一个方法拆成多个方法；直观的逻辑应集中编写，仅在提升可读性或封装有意义的可复用行为时提取方法。
- 不得执行任何 `java` 或 `maven` 命令。
- 除非明确要求，否则不得编写任何测试代码。
- 必须一次性提供完整解决方案，不要在任务结束时再建议后续升级。

# Spring Boot 开发规范

- 严格遵循 **Spring Boot 4.x** 推荐的最佳实践。
- 使用 `@RequiredArgsConstructor` 进行 Bean 注入。
- 在适用场景使用 **Lombok** 消除冗余代码。
- 使用 `jakarta.*` 包，禁止使用 `javax.*` 包。
- 优先使用 `@ConfigurationProperties`，而不是 `@Value`。
- 避免不必要的 `@Component` 注解，优先显式配置 Bean。
- 构建可复用模块时使用 `@AutoConfiguration`。
- 在适合的场景优先采用函数式编程风格。

# 代码结构

- 保持清晰的包结构：
    - `controller`（控制器层）
    - `service`（业务逻辑层）
    - `impl`（业务逻辑实现）
    - `mapper`（数据访问层）
    - `config`（配置层）
    - `model/entity`（数据库实体）
    - `model/dto`（数据传输对象）
    - `model/vo`（视图对象）
- 明确区分 DTO、VO 和 Entity 的职责。
- 保持类的职责粒度较小，严格遵循**单一职责原则**。

## 自定义业务方法命名规则

自定义业务方法统一遵循以下命名规则：

1. 使用小驼峰命名法（lowerCamelCase），例如 `getUserInfo`、`addUser`。
2. 方法名采用“操作动词 + 业务对象 + 可选的 By 条件”结构，名称应直接表达方法用途。
3. 增删改查统一使用以下动词，同类操作保持一致：
   - 查询：`get`，例如 `getUserInfo`、`getUserById`。
   - 新增：`add`，例如 `addUser`。
   - 删除：`delete`，例如 `deleteUserById`。
   - 更新：`update`，例如 `updateUser`、`updateUserById`。
4. 需要突出查询或操作条件时，使用 `By + 条件名称`，例如 `getUserByUsername`、`addUserByInviteCode`。多个条件使用 `And` 连接，例如 `getUserByUsernameAndStatus`。
5. `By` 后的内容必须对应实际业务条件。仅在有助于明确方法用途或区分方法时添加，无须把所有参数都写进方法名。
6. 使用完整、明确的业务词汇，避免无意义缩写和模糊名称，例如 `getData`、`doUser`、`handleInfo`。
7. 非增删改查方法使用符合业务含义的动词，例如 `validateUserPassword`、`resetUserPassword`、`disableUser`，并保持“动词 + 业务对象”的结构。

# 日志与异常

- 使用结构化日志，推荐 SLF4J。
- **禁止**使用 `System.out.println` 输出内容。
- 日志信息必须有明确含义。
- 日志和异常信息**必须使用中文**，此项为重要要求。
- 通过 `@ControllerAdvice` 实现全局异常处理机制。

# 文档与注释（重要）

## 类注释格式（强制）

/**
* @author Jince
* @date yyyy/MM/dd
* @description
  */

## 方法注释规则（强制）

- **每个方法都必须包含注释；手写 Java 方法统一使用多行 Javadoc**，不得写成 `/** 功能说明。 */` 这样的单行简注，也不得用 `//` 替代方法注释。
- 适用于接口声明、实现方法、控制器、Mapper、自定义实体方法和私有辅助方法；接口与实现类都必须写完整注释，不能仅使用 `@Override` 或 `{@inheritDoc}` 替代。
- 第一部分用简洁、清晰的中文说明方法的具体功能，不堆砌实现过程；复杂或不易理解的逻辑按需补充说明。
- 功能说明与 `@param`、`@return` 标签之间保留一行空的 `*`。
- 有参数时，按声明顺序为每个参数写 `@param 参数名 参数含义`，参数名必须与方法声明一致；无参数时不写 `@param`。
- 有返回值时必须写 `@return 返回值含义`；`void` 方法不写 `@return`。没有参数和返回值标签时，功能说明后直接结束注释，不额外插入空行。
- `/**` 和 `*/` 分别独占一行，中间每行以对齐的 `*` 开头。新增或修改方法时，沿用以下格式：

```java
/**
 * 根据id查询字典类型。
 *
 * @param id 字典类型id
 * @return 字典类型详情
 */
```

## 关键逻辑注释（强制）

- 方法体内的关键业务分支、权限与归属判断、树形关系维护、批量更新一致性及事务提交后的缓存或会话处理，必须在对应代码附近补充简洁的中文注释，说明业务原因、影响范围或关键约束，不能只写方法 Javadoc。
- 注释应帮助读者理解“为什么这样做”，不逐行翻译代码，不给直观的赋值、转换和增删改查堆砌无意义注释；修改逻辑时同步维护注释，保持与实现一致。

# 代码生成

- 始终生成完整、可直接运行的代码。
- 包含所有必要的 `import` 语句、注解和配置细节。
- **禁止**省略任何关键代码。
- **禁止**混用新旧代码风格。

# 风格与质量

- 优先保证逻辑清晰、易于阅读，不追求炫技式写法。
- 避免过度设计。
- 避免逻辑深度嵌套。
- 遵循统一的代码格式规范。

# 行为准则

- 存在多种实现方案时，选择最现代且官方推荐的方案。
- 除非明确要求，否则不要建议过时的替代方案。
