# Docker 部署

前端、后端作为两个项目独立构建、启动和更新。后端的构建上下文是仓库根目录，前端的构建上下文是 `whim-vue/`，可以将前端目录单独上传到服务器。

| 项目 | Dockerfile / Compose 所在目录 | 服务 | 默认宿主机端口 |
| --- | --- | --- | --- |
| 后端 | 仓库根目录 | `backend`、`mysql`、`redis` | `127.0.0.1:8089` |
| 前端 | `whim-vue/` | `frontend`，Nginx 托管 Vue 并反向代理 API | `127.0.0.1:8080` |

两套 Compose 使用不同的项目名称；更新前端不会重建数据库或后端。后端使用 JDK/JRE 25，前端使用 Node.js 26、pnpm 12.8.1。服务器需要安装 Docker Engine、BuildKit 和支持 Compose Specification 的 `docker compose` 插件，无需另外安装 Java、Maven 或 Node.js。首次构建需要联网下载镜像与依赖。

## 1. 配置后端

以下命令在 Linux 服务器执行，示例假设仓库上传到 `/srv/whim`。命令中的目录需要与实际上传位置一致。复制示例文件仅限首次部署；已有 `.env` 时直接编辑，避免覆盖现有配置。

```bash
cd /srv/whim
cp .env.example .env
chmod 600 .env
```

编辑后端 `.env`，必须填写：

| 配置 | 含义 |
| --- | --- |
| `MYSQL_PASSWORD` | 应用数据库账号 `whim` 的密码 |
| `MYSQL_ROOT_PASSWORD` | MySQL 管理员密码，与应用密码分开设置 |
| `REDIS_PASSWORD` | Redis 访问密码 |
| `SA_TOKEN_JWT_SECRET` | 独立的随机签名密钥，可使用 64 位十六进制随机字符串 |
| `REQUEST_ENCRYPTION_PRIVATE_KEY` | 与前端公钥配对的 PKCS#8 DER 单行 Base64 私钥 |

空值会让 Compose 在启动前报错。密码使用 `.env` 示例中的单引号格式，避免 `$` 被当成变量。`.env` 已被 Git 和 Docker 构建上下文排除。不要将后端 `.env` 复制到前端。

数据库地址、端口、库名、用户名分别通过 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DB`、`MYSQL_USER` 配置；Redis 通过 `REDIS_HOST`、`REDIS_PORT`、`REDIS_DB` 配置。默认服务地址 `mysql:3306`、`redis:6379` 是容器网络地址，不能改成 `localhost`。

MySQL 默认使用 TLS，后端 `MYSQL_SSL_MODE=REQUIRED` 与数据库 `--require-secure-transport=ON` 配套。内置数据库只创建数据库与应用账号，表结构由后端 Flyway 自动迁移；不要再把 Flyway SQL 挂载到 MySQL 初始化目录重复执行。

## 2. 配置前端与配对密钥

在前端项目目录首次创建配置：

```bash
cd /srv/whim/whim-vue
cp .env.example .env
chmod 600 .env
```

填写 `VITE_REQUEST_ENCRYPTION_PUBLIC_KEY`、网站标题等公开配置。同一台服务器上保留 `BACKEND_ADDRESS=whim-api:8089` 和 `VITE_API_BASE_URL=/api`；浏览器请求 `/api/system/...`，前端 Nginx 将它转发为后端 `/system/...`，浏览器不直接访问容器名。

生成部署专用密钥可在已安装 Node.js 的电脑上运行仓库已有脚本：

```bash
node scripts/generateRequestKey.mjs
```

服务器只有 Docker 时，可在后端项目目录运行：

```bash
timeout 180 docker run --rm \
  --mount type=bind,src="$PWD/scripts/generateRequestKey.mjs",dst=/generateRequestKey.mjs,readonly \
  node:26-bookworm-slim node /generateRequestKey.mjs
```

把输出中 `private-key` 的值填入后端 `.env` 的 `REQUEST_ENCRYPTION_PRIVATE_KEY`，把输出的 `VITE_REQUEST_ENCRYPTION_PUBLIC_KEY` 填入前端 `.env`。私钥不写入 `VITE_` 变量。已有配对密钥也可以沿用，更新镜像时不要重新生成。

前端 `VITE_` 变量通过构建参数注入。修改公钥、标题或接口前缀后必须重新构建前端镜像；仅修改 `BACKEND_ADDRESS` 或宿主机端口时只需重建容器。

## 3. 目录映射与网络

后端 `.env` 中可以修改以下宿主机路径，容器路径保持不变：

| 配置项 | 默认宿主机路径 | 容器路径 | 用途 |
| --- | --- | --- | --- |
| `BACKEND_CONFIG_DIR` | `./deploy/config` | `/app/config`，只读 | `application-prod.yml` 生产配置 |
| `BACKEND_FILES_DIR` | `/opt/whim/backend/files` | `/app/data/files` | 上传文件 |
| `BACKEND_LOGS_DIR` | `/opt/whim/backend/logs` | `/app/logs` | 后端滚动日志 |
| `MYSQL_DATA_DIR` | `/opt/whim/backend/mysql` | `/var/lib/mysql` | 数据库文件 |
| `REDIS_DATA_DIR` | `/opt/whim/backend/redis` | `/data` | Redis AOF / RDB 数据 |
| `REDIS_CONFIG_FILE` | `./deploy/redis/redis.conf` | `/usr/local/etc/redis/redis.conf`，只读 | Redis 配置 |

前端的 `NGINX_TEMPLATE_DIR` 默认映射当前前端项目的 `./deploy/nginx/templates` 到 `/etc/nginx/templates`，为只读挂载。修改模板后重启前端容器。前端静态资源已打入镜像，不挂载空目录覆盖它们。Nginx、MySQL 和 Redis 日志由 Docker 收集，设置了单文件 20MB、最多 5 个文件的轮转。

使用默认路径时，首次部署创建目录并给后端运行用户授予写权限：

```bash
sudo install -d -m 0750 -o 10001 -g 10001 /opt/whim/backend/files /opt/whim/backend/logs
sudo install -d -m 0750 /opt/whim/backend/mysql /opt/whim/backend/redis
timeout 30 docker network create whim-network
```

已存在 `whim-network` 时跳过创建。两套 Compose 的 `SHARED_NETWORK_NAME` 必须一致。共享网络由运维创建，两边执行 `docker compose down` 都不会删除它；数据库只连接后端的内部网络，前端不能直接连接数据库。

修改过路径时，上述创建目录的命令也要使用对应路径。切换 `BACKEND_CONFIG_DIR` 时，将仓库的 `application-prod.yml` 放入新目录并确保 UID 10001 可读。挂载目录不存在会直接报错，不会自动创建 root 所有的目录掩盖配置错误。MySQL、Redis 官方入口会为自己的数据目录设置权限；不要把它们的数据目录改成后端的 UID 10001。

## 4. 分别构建和启动

先在后端项目目录构建、启动。首次依赖下载可能较慢，构建上限设为 30 分钟，启动等待上限为 5 分钟：

```bash
cd /srv/whim
timeout 30 docker compose config --quiet
timeout 1800 docker compose build backend
timeout 360 docker compose up -d --wait --wait-timeout 300
timeout 30 docker compose ps
```

MySQL 和 Redis 通过健康检查后，后端才启动。后端会执行 Flyway 迁移，并通过只监听容器回环地址的 `9090/actuator/health` 检查数据库、Redis 等依赖。该管理端口不会发布到宿主机。

然后在前端项目目录单独构建和启动：

```bash
cd /srv/whim/whim-vue
timeout 30 docker compose config --quiet
timeout 1800 docker compose build frontend
timeout 90 docker compose up -d --wait --wait-timeout 60
timeout 30 docker compose ps
curl --connect-timeout 3 --max-time 10 http://127.0.0.1:8080/healthz
curl --connect-timeout 3 --max-time 15 http://127.0.0.1:8080/api/system/auth/captcha
```

`/healthz` 仅检查前端 Nginx；验证码接口用于确认前后端链路与 Redis 可用。Nginx 会动态解析后端容器地址，后端独立更新后不需要手动重启前端。

## 5. 服务器 HTTPS 入口

现有前端加密使用 Web Crypto，正式服务器必须通过 HTTPS 访问；`http://服务器IP` 无法正常提交登录请求。本机调试可以使用浏览器的 `http://localhost:8080`。

默认前端仅监听宿主机 `127.0.0.1:8080`。用服务器已有 Nginx、宝塔或其他 HTTPS 网关把域名转发到此地址。`whim-vue/deploy/nginx/https.conf.example` 提供完整的宿主机 Nginx 示例：替换域名、证书路径后安装到宿主机 Nginx 的配置目录，检查配置再重载：

```bash
sudo timeout 30 nginx -t
sudo timeout 30 systemctl reload nginx
```

示例同时转发页面与 `/api` 请求，只需为整个域名配置一个站点。证书由 HTTPS 网关维护，容器内的前端 Nginx 不需要读取证书。若网关不在宿主机上，根据实际网络修改 `FRONTEND_BIND_ADDRESS` 和网关上游地址。

## 6. 前后端部署在不同服务器

两边仍分别使用自己的 Dockerfile、Compose 和 `.env`。在各自 Docker 主机上创建本地 `whim-network`；该网络不会跨服务器互通。

后端将 `BACKEND_BIND_ADDRESS` 改为后端服务器的私网 IP，例如 `10.0.0.10`，保留 `BACKEND_PORT=8089`。前端将 `BACKEND_ADDRESS` 改为 `10.0.0.10:8089`，然后分别执行 `docker compose up -d`。该值只填写主机名或 IP 加端口，不带 `http://` 和路径。

确保两台服务器的私网互通，并允许前端服务器访问后端 8089。前端域名的 HTTPS 入口仍部署在前端服务器，MySQL 和 Redis 端口不需要开放。

## 7. 使用已有数据库和 Redis

在后端 `.env` 修改 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DB`、`MYSQL_USER`、`MYSQL_PASSWORD` 以及 Redis 地址、密码、库编号；填写真实服务地址。已有 MySQL 需要预先创建数据库并授予应用账号迁移表结构所需权限，TLS 配置需与服务一致。

仅启动后端，跳过 Compose 内的数据库依赖：

```bash
cd /srv/whim
timeout 1800 docker compose build backend
timeout 360 docker compose up -d --no-deps --wait --wait-timeout 300 backend
```

整份 Compose 在解析时仍校验 `MYSQL_ROOT_PASSWORD`，应保留一个非空值；使用上述命令时它不会发送到外部 MySQL，后端也不接收该变量。如果之前已启动内置数据库，确认外部服务切换完成后可单独停止内置服务，不删除其数据目录：

```bash
timeout 90 docker compose stop mysql redis
```

## 8. 更新、日志和镜像交付

更新后端时，在后端目录执行以下命令，不重建数据库：

```bash
timeout 1800 docker compose build backend
timeout 360 docker compose up -d --no-deps --wait --wait-timeout 300 backend
timeout 30 docker compose logs --tail=100 backend
```

更新前端时，在前端目录执行：

```bash
timeout 1800 docker compose build frontend
timeout 90 docker compose up -d --no-deps --wait --wait-timeout 60 frontend
timeout 30 docker compose logs --tail=100 frontend
```

修改 `.env` 后用 `docker compose up -d` 重建对应容器；单独 `restart` 不会重新载入容器环境变量。只修改已挂载的后端 YAML 或前端 Nginx 模板时，可在对应目录执行 `timeout 90 docker compose restart backend` 或 `timeout 90 docker compose restart frontend`。

也可以在构建机完成镜像构建、推送到镜像仓库，然后服务器只拉取镜像。后端 `.env` 设置 `BACKEND_IMAGE=你的镜像仓库/whim-backend:版本号`，前端 `.env` 设置 `FRONTEND_IMAGE=你的镜像仓库/whim-frontend:版本号`。各自在项目目录执行 `timeout 1800 docker compose build` 和 `timeout 600 docker compose push`；服务器保留各自 Compose、`.env` 和挂载配置，使用 `timeout 600 docker compose pull` 后执行对应的 `up -d --no-build --wait` 命令。

构建机与服务器 CPU 架构不同时，使用 Buildx 在各自项目目录构建目标平台，例如 `docker buildx build --platform linux/amd64`；前端还需传入 Compose 中相同的公开构建参数。普通同架构部署直接使用以上 Compose 命令。

重新创建容器不会清空 bind mount 数据。备份时覆盖 MySQL、Redis、上传文件、两套 `.env` 与部署配置；数据库使用一致性备份，不能把运行中的数据库目录直接复制作为可靠备份。MySQL 初始化变量只对空数据目录生效，后续修改 `.env` 中的密码不会自动修改已有数据库账号密码。

## 参考

配置依据：[Compose 启动顺序](https://docs.docker.com/compose/how-tos/startup-order/)、[环境变量插值](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/)、[MySQL 官方镜像](https://hub.docker.com/_/mysql)、[Nginx 官方镜像](https://hub.docker.com/_/nginx)及 [Nginx 动态上游解析](https://nginx.org/en/docs/http/ngx_http_upstream_module.html#resolve)。
