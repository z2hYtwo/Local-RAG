package com.bmad.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 模型配置类：负责映射和管理 application.yml 中以 "bmad.model" 为前缀的配置项。
 * 
 * 通过 @ConfigurationProperties 注解，Spring Boot 会自动将配置文件中的属性
 * 绑定到该类的同名字段上。这种方式比 @Value 更具结构化且支持类型检查。
 */
@Configuration
@ConfigurationProperties(prefix = "bmad.model")
public class ModelConfig {
    /**
     * 模型文件的绝对或相对路径。
     * 该路径会被传递给 JNI 层进行实际的文件加载。
     */
    private String path;

    /**
     * 是否启用堆外内存 (Off-heap) 模式加载模型。
     * - true: 模型将加载到直接内存中，减少 JVM GC 对大模型内存的影响。
     * - false: 默认行为（取决于本地层实现）。
     */
    private boolean offHeap;

    /**
     * 获取配置的模型路径。
     * @return 模型文件路径字符串。
     */
    public String getPath() { return path; }

    /**
     * 设置模型路径。通常由 Spring 在初始化时自动调用。
     * @param path 模型文件路径。
     */
    public void setPath(String path) { this.path = path; }

    /**
     * 检查是否配置了堆外内存模式。
     * @return 如果启用堆外内存则返回 true。
     */
    public boolean isOffHeap() { return offHeap; }

    /**
     * 设置堆外内存模式开关。
     * @param offHeap 布尔值，控制是否使用堆外内存。
     */
    public void setOffHeap(boolean offHeap) { this.offHeap = offHeap; }
}
