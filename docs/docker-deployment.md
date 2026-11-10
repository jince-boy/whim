# Docker 部署

本地构建镜像，服务器只运行已导入的镜像。当前前后端 Compose 已按这台服务器配置，上传两个 `compose.yaml` 后即可启动，不需要再上传 `.env`、Dockerfile、源码或 Nginx 模板。

| 项目 | 服务器配置文件 | 宿主机端口 → 容器端口 | 网络 |
| --- | --- | --- | --- |
| 后端 | `/opt/whim/backend/compose.yaml` | `9192 → 8089` | `my-network` |
| 前端 | `/opt/whim/frontend/compose.yaml` | `9191 → 80` | `my-network` |

## 当前已完成的配置

- 后端连接 `mysql8.2:3306/whim`，使用 `root` 账号及已确认的密码；Redis 使用 `redis:6379`、数据库 `0`，当前不带用户名和密码。
- 无密码 Redis 必须省略 `SPRING_DATA_REDIS_USERNAME` 和 `SPRING_DATA_REDIS_PASSWORD`，不要写成空字符串；Redisson 会将空字符串密码视为需要认证。Redis 启用认证时，才设置实际密码；只有使用命名 ACL 用户时才设置实际用户名。
- 后端 Compose 已填写固定的随机 JWT 签名密钥；请求加密沿用后端镜像 `application.yml` 中已与现有前端公钥配对的私钥，不额外注入私钥环境变量。重启不会更换密钥，服务器不需要手工补填。
- 后端首次启动会自动创建上传文件和日志目录、设置 UID/GID 10001 的写权限，然后降权运行应用。
- 前端首次启动会从镜像复制 Nginx 模板到服务器目录。目录中已有非空模板时保留原内容，重启不会覆盖手工修改。
- 前端公钥和网站标题等配置已经编译进镜像。运行已有镜像不需要 `VITE_REQUEST_ENCRYPTION_PUBLIC_KEY`；本地构建镜像时仍由 Dockerfile 校验公钥不能为空。
- 前后端使用已导入的本地镜像，不自动拉取镜像。容器内部仍分别监听 `8089` 和 `80`，不会占用宿主机的这两个端口。

本次已核对现有前后端镜像归档中的密钥与本地配置一致。部署配置包含数据库密码和 JWT 密钥，后端镜像含请求加密私钥，应限制访问；不在日志或聊天中展示密钥，也不要把生产部署文件提交到公开仓库。

## 1. 上传部署文件

将根目录 `compose.yaml` 上传为 `/opt/whim/backend/compose.yaml`，将 `whim-vue/compose.yaml` 上传为 `/opt/whim/frontend/compose.yaml`。

也可以将本次整理的 `whim-compose.zip` 上传到服务器，解压到 `/opt/whim`。归档包含 `backend/compose.yaml` 和 `frontend/compose.yaml` 两个文件，解压后即为所需布局：

```bash
install -d -m 0755 /opt/whim
unzip -o whim-compose.zip -d /opt/whim
chmod 600 /opt/whim/backend/compose.yaml /opt/whim/frontend/compose.yaml
```

如果服务器已修改过部署参数，覆盖前先保存旧配置。使用当前默认配置时，前端不需要 `.env`；已有 `.env` 中的端口、网络、镜像或模板路径会覆盖 Compose 默认值，应核对是否仍需要这些覆盖。

## 2. 确认已有基础服务

`my-network` 是你已有的外部 Docker 网络，Compose 不负责新建 MySQL 或 Redis。确认网络包含后端需要连接的 `mysql8.2` 和 `redis` 容器：

```bash
docker network inspect my-network
```

如果网络不存在，先执行 `docker network create my-network`；如果数据库容器尚未加入，分别执行以下命令，已经加入的容器无需重复连接：

```bash
docker network connect my-network mysql8.2
docker network connect my-network redis
```

MySQL 中的空库应已创建，字符集与项目迁移脚本保持一致：

```sql
CREATE DATABASE whim
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
```

后端启动时由 Flyway 创建表并执行版本迁移，不需要手工导入迁移 SQL。MySQL 账号需允许后端容器连接，并具备迁移所需权限；当前连接沿用 `MYSQL_SSL_MODE: REQUIRED`，要求 MySQL 提供 TLS。

## 3. 导入镜像并启动

在镜像归档所在目录导入；已经导入时跳过：

```bash
docker image load -i whim-backend.tar
docker image load -i whim-frontend.tar
```

如果导出的是合并归档 `whim-images.tar`，只需导入该文件一次。镜像标签应为 `whim-backend:local` 和 `whim-frontend:local`。

本次 HTTP 登录兼容修复的前端归档为 `whim-vue/whim-frontend-http-20261107.tar`，已包含 `whim-frontend:local` 标签。恢复或迁移本次修复时导入该归档；原先的 `whim-frontend.tar` 是修复前版本，仍会要求 HTTPS 安全上下文。

先启动后端，等待健康检查通过后再启动前端：

```bash
cd /opt/whim/backend
docker compose up -d --no-build --pull never --wait --wait-timeout 300

cd /opt/whim/frontend
docker compose up -d --no-build --pull never --wait --wait-timeout 60
```

日常也可以在各自目录执行 `docker compose up -d`。上述命令额外明确禁止构建和拉取，并等待健康检查结果；无须重新构建这次已上传的镜像。

在对应目录查看状态和日志：

```bash
docker compose ps
docker compose logs --tail=100
```

前端链路检查：

```bash
curl --connect-timeout 3 --max-time 10 http://127.0.0.1:9191/healthz
curl --connect-timeout 3 --max-time 15 http://127.0.0.1:9191/api/system/auth/captcha
```

`/healthz` 检查 Nginx，验证码接口检查前后端链路和 Redis。后端管理端口 `9090` 仅监听容器回环地址，不发布到宿主机。

## 自动目录映射

| 宿主机目录 | 容器目录 | 初始化方式 |
| --- | --- | --- |
| `/opt/whim/backend/files` | `/app/data/files` | 自动创建，设置为 UID/GID 10001、权限 0750 |
| `/opt/whim/backend/logs` | `/app/logs` | 自动创建，设置为 UID/GID 10001、权限 0750 |
| `/opt/whim/frontend/nginx/templates` | `/app/nginx-templates` | 自动创建，首次从镜像复制 Nginx 模板 |

前端通过 `NGINX_ENVSUBST_TEMPLATE_DIR` 使用映射后的模板目录，镜像内的原始模板仍保留在 `/etc/nginx/templates`，因此空宿主机目录不会遮住模板来源。前端静态页面保存在镜像中，不挂载空目录覆盖它们。Nginx 日志由 Docker 收集，后端文件日志保存在上述 `logs` 目录。

## 文件上传的 Windows / Linux 部署约束

- 文件管理页面使用数据库 `sys_file_storage` 中的存储配置；`FILE_STORAGE_PATH` 只配置文件模块的默认实例，不会覆盖数据库中已有的本地目录。Linux 容器内的文件管理配置应填写 `/app/data/files`，对应宿主机 `/opt/whim/backend/files`。不能直接沿用 Windows 的 `D:\\...` 目录，也不能在容器中填写宿主机路径。
- 普通相对目录允许用于开发，会在保存配置时按当前工作目录解析成绝对目录；生产配置使用容器内的绝对路径。代码拒绝 Linux 下的 Windows 盘符、反斜杠，以及 Windows 下缺少完整盘符的根路径，避免静默写入错误目录。
- 迁移已有本地文件时，需要同时保留数据库记录、存储配置 ID 和文件的相对对象路径。已有文件或未结束上传任务的配置禁止在管理页面改位置；正式迁移需先停写并备份，完整复制原存储目录，核对文件和目录权限，再按原存储配置 ID 调整数据库中的 `base_path`。仅新增一个默认配置不会改变历史文件的读取位置。未完成任务的 `.multipart` 目录也必须一起迁移，且仍受任务有效期限制。
- 容器运行用户为 UID/GID `10001:10001`。挂载目录及迁移进来的子目录、文件都需要相应的读写权限；Compose 的启动步骤只准备根目录，不会递归修改已有文件的所有者。上传请求的临时目录也需要可写空间。
- 本地分片目录和正式文件目录应位于同一支持原子移动的文件系统，不要单独挂载 `.multipart` 或某个对象子目录到另一块盘。合并期间还会生成完整的临时文件，磁盘容量需覆盖分片和合并内容。Java 对原子移动的支持取决于文件系统，跨卷可能失败，代码不会通过直接覆盖正式文件来掩盖失败。[JDK Files.move 文档](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...))
- 单文件默认上限为 200 MiB，当前分片大小为 8 MiB。后端 multipart 总请求上限和前端 Nginx、HTTPS 网关示例均配置为 201 MiB，兼容普通上传的表单封装开销。服务器上已存在的 Nginx 模板不会被新镜像覆盖，更新部署时也要将保留模板中的 `client_max_body_size` 调整为 `201m`，并核对外层网关限制。
- 文件物理名称统一使用 UUID 和安全后缀，原名称只用于元数据与下载展示；普通上传和分片上传复用同一命名规则，避免 Linux 可接受的特殊字符及过长后缀导致 Windows 存储失败。
- 普通 HTTP 地址下，上传请求标识使用 `crypto.getRandomValues` 生成 32 位十六进制字符串，不依赖仅在安全上下文中可用的 `crypto.randomUUID`，保持与项目现有 HTTP 登录兼容方案一致。[Web Crypto 标准](https://w3c.github.io/webcrypto/#crypto-interface)

上述为代码和部署配置约束，不能代替目标 Linux 环境上的实际上传、暂停继续、取消、合并及重启恢复验证。当前页面任务未持久化到浏览器，刷新页面或关闭浏览器后不会自动恢复队列。

## HTTPS 和前后端通信

同一 Docker 网络内，前端通过 `whim-api:8089` 访问后端；从宿主机或其他服务器访问后端时使用 `9192`。跨服务器部署时，前端的 `BACKEND_ADDRESS` 应改为后端私网地址，例如 `10.0.0.10:9192`。

当前支持通过 HTTP 域名或服务器 IP 访问和登录。浏览器提供 SubtleCrypto 时使用原生加密；普通 HTTP 页面按需加载 JavaScript 兼容实现，保持 `RSA-OAEP-256` + `A256GCM` 的 JWE 协议及后端强制加密校验，随机数仍来自浏览器 `crypto.getRandomValues`。线路机继续通过 HTTP 转发到源站 `9191`，无需为本次修复调整线路机配置。已有 HTTPS 网关也可转发到该端口；仓库 `whim-vue/deploy/nginx/https.conf.example` 保留为可选配置示例。

## 后续构建和配置更新

本地开发和构建镜像时，前端继续使用 `whim-vue/.env` 中的公钥、标题和接口前缀：

```bash
# 在仓库根目录构建后端
docker compose build backend
docker image save -o whim-backend.tar whim-backend:local

# 在前端目录构建前端
cd whim-vue
docker compose build frontend
docker image save -o whim-frontend.tar whim-frontend:local
```

更换请求加密密钥必须同步更新后端 `application.yml` 私钥和前端 `.env` 公钥，并重新构建、导出和部署前后端镜像。也可以明确通过后端环境变量 `WHIM_REQUESTENCRYPTION_PRIVATEKEY` 配置对应的新私钥，但绝不能设置为空字符串；空值会覆盖镜像中原有的有效配置。现有配对密钥可以直接沿用，不要只为了部署或重启而重新生成。

仅修改 Compose 中的数据库连接、端口、网络或其他运行参数时，重新上传对应 Compose 并执行 `up -d --no-build --pull never` 即可。仅修改服务器 Nginx 模板时重启前端容器；修改镜像时先导入新镜像，再重建对应容器。

## 参考

[Compose 文件命名](https://docs.docker.com/compose/intro/compose-application-model/#the-compose-file)、[Compose 目录挂载](https://docs.docker.com/reference/compose-file/services/#volumes)、[Compose 启动参数](https://docs.docker.com/reference/cli/docker/compose/up/)、[Spring Boot 外部化配置](https://docs.spring.io/spring-boot/reference/features/external-config.html)。
