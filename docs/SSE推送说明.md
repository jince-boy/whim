# SSE 推送

`whim-sse` 将业务事件发送给在线的系统用户。`whim-start` 提供登录订阅入口，业务服务或应用中的事件监听器可以注入 `SseEventPublisher` 发布事件。

## 订阅

通过 `GET /system/sse/subscribe` 建立连接，用 `Authorization: Bearer <登录令牌>` 传递当前用户身份。浏览器可使用支持自定义请求头的流式 Fetch 客户端。前端经过 Nginx 时沿用项目的 API 前缀。

在 PowerShell 中，可以用已有的登录令牌观察本机事件流。

```powershell
curl.exe -N -H "Authorization: Bearer $env:WHIM_ACCESS_TOKEN" -H "Accept: text/event-stream, application/json" http://localhost:8089/system/sse/subscribe
```

成功后返回 `200 text/event-stream`。有效连接的第一帧是 `:connected` 注释，空闲时默认每 25 秒发送一帧 `:heartbeat`。业务帧使用标准 SSE 的 `event` 和 `data` 字段，`data` 为 JSON。一次网络读取可能只包含半帧，也可能包含多帧，客户端应使用 SSE 解析器。

关闭页面或中止请求即可取消本次订阅。客户端在网络中断后可退避重连，并在每次连接成功后通过业务查询接口同步当前状态。初次连接或重连返回 401 时结束订阅并进入登录流程，429 表示同一用户在该实例的连接数已达上限。

撤销会话或退出全部会话成功后，服务端立即唤醒该账号的在线连接，按每条连接建立时的令牌复核。已失效的连接收到 `sessionInvalidated` 事件（`data` 为 `{}`）后结束，有效连接继续订阅。建立订阅、业务投递和心跳时也会复核。客户端收到该事件后清除对应会话并返回登录页；旧连接的通知不能清除后来登录的会话。

## 发布

以下调用演示事件命名和负载格式，业务自行定义事件含义。

```java
sseEventPublisher.sendToUser(userId, "profileChanged", Map.of("refresh", true));
sseEventPublisher.broadcast("siteChanged", Map.of("refresh", true));
```

定向事件发往该用户的所有在线标签页和设备。广播对所有在线系统用户可见，涉及数据权限的内容由接收方调用已授权的业务接口读取。事务内的变更应在提交成功后发布，例如使用 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`。离线期间的事件和断线前积压的事件由业务查询补齐，服务端不保存事件历史。

## 部署

| 配置 | Whim 默认值 | 用途 |
| --- | --- | --- |
| `whim.sse.enabled` | `true` | 启用模块和订阅入口 |
| `whim.sse.heartbeat-seconds` | `25` | 空闲心跳间隔，反向代理的读取超时应大于此值 |
| `whim.sse.max-connections-per-user` | `12` | 每个实例允许单个用户建立的连接数 |
| `whim.sse.queue-capacity` | `64` | 每条连接的待写事件数，积压超限后断开该连接 |
| `whim.sse.cluster-enabled` | `false` | 启用后通过应用现有的 Redisson 客户端跨节点推送 |
| `whim.sse.cluster-topic` | `${spring.application.name}:${spring.profiles.active}:sse:system:events` | 同一集群使用相同频道，共享 Redis 的独立部署使用不同频道 |

多实例部署时在每个实例上开启集群模式。发布节点和其他节点都从同一频道接收一次事件，各自只向本机连接投递。客户端重连时可连接任意实例。订阅响应带有 `X-Accel-Buffering: no`，供项目的 Nginx 代理即时转发消息。
