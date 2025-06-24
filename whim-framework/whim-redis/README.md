### 集群配置yml示例：
```yaml
# redis 集群配置(单机与集群只能开启一个另一个需要注释掉)
spring.data:
  redis:
    cluster:
      nodes:
        - 192.168.0.100:6379
        - 192.168.0.101:6379
        - 192.168.0.102:6379
    # 密码
    password:
    # 连接超时时间
    timeout: 10s
    # 是否开启ssl
    ssl.enabled: false

redisson:
  # 线程池数量
  threads: 16
  # Netty线程池数量
  nettyThreads: 32
  # 集群配置
  clusterServersConfig:
    # 客户端名称
    clientName: whim
    # master最小空闲连接数
    masterConnectionMinimumIdleSize: 32
    # master连接池大小
    masterConnectionPoolSize: 64
    # slave最小空闲连接数
    slaveConnectionMinimumIdleSize: 32
    # slave连接池大小
    slaveConnectionPoolSize: 64
    # 连接空闲超时，单位：毫秒
    idleConnectionTimeout: 10000
    # 命令等待超时，单位：毫秒
    timeout: 3000
    # 发布和订阅连接池大小
    subscriptionConnectionPoolSize: 50
    # 读取模式
    readMode: "SLAVE"
    # 订阅模式
    subscriptionMode: "MASTER"
```