package com.whim;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;

@SpringBootApplication
@Slf4j
public class WhimApplication {
    @SneakyThrows
    public static void main(String[] args) {
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
        System.out.println("=================Whim 启动成功=================");
    }
}
