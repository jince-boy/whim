package com.whim;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * @author Jince
 * @date 2026/11/15
 * @description 应用启动与任务调度入口。
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
public class WhimApplication {
    /**
     * 启动应用并输出访问地址。
     *
     * @param args 启动参数
     */
    @SneakyThrows
    static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(WhimApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        log.info("""
                        
                        ------------------------------------------
                        本地地址: \thttp://localhost:{}
                        外部地址: \thttp://{}:{}
                        ------------------------------------------
                        """,
                environment.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                environment.getProperty("server.port"));
    }

}
