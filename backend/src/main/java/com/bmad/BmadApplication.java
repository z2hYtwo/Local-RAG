package com.bmad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用入口类，负责触发组件扫描与自动配置。
 */
@SpringBootApplication
public class BmadApplication {

    /**
     * JVM 启动点，交由 SpringApplication 启动整个应用上下文。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BmadApplication.class, args);
    }
}
