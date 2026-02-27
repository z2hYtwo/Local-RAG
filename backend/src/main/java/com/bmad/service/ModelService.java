package com.bmad.service;

import com.bmad.config.ModelConfig;
import com.bmad.nativeapi.LlamaNative;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 模型业务服务类。
 * 
 * 职责：
 * 1. 封装 LlamaNative 的 JNI 调用，提供更易用的业务接口。
 * 2. 状态管理：记录模型是否已加载。
 * 3. 资源回收：确保应用关闭时释放 Native 内存。
 */
@Service
public class ModelService {

    private final ModelConfig modelConfig;
    private final AtomicBoolean isModelLoaded = new AtomicBoolean(false);

    public ModelService(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }

    /**
     * 加载模型。
     * 路径由 ModelConfig 从 application.yml 中自动读取。
     * 
     * @return 状态信息
     */
    public String loadModel() {
        if (isModelLoaded.get()) {
            return "模型已处于加载状态，请勿重复加载。";
        }

        String path = modelConfig.getPath();
        int result = LlamaNative.loadModel(path);

        if (result == 1) {
            isModelLoaded.set(true);
            return "模型加载成功: " + path;
        } else {
            return "模型加载失败，请检查路径: " + path;
        }
    }

    /**
     * 卸载模型并释放堆外内存。
     * 
     * @return 状态信息
     */
    public String unloadModel() {
        if (!isModelLoaded.get()) {
            return "当前未加载任何模型，无需卸载。";
        }

        LlamaNative.freeModel();
        isModelLoaded.set(false);
        return "模型已卸载，原生资源已释放。";
    }

    /**
     * 握手测试。
     * 用于快速验证 JNI 链路是否通畅。
     * 
     * @return Native 层返回的握手信息
     */
    public String checkStatus() {
        return LlamaNative.handshake();
    }

    /**
     * 检查模型当前加载状态。
     * 
     * @return 是否已加载
     */
    public boolean isLoaded() {
        return isModelLoaded.get();
    }

    /**
     * 生命周期管理：在 Spring Bean 销毁前（应用关闭前）
     * 强制执行内存释放，防止原生层出现内存泄漏。
     */
    @PreDestroy
    public void cleanup() {
        if (isModelLoaded.get()) {
            System.out.println("[Service] 应用即将关闭，正在释放模型资源...");
            unloadModel();
        }
    }
}
