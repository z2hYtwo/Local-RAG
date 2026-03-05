package com.bmad.service;

import com.bmad.nativeapi.LlamaNative;
import org.springframework.stereotype.Service;

/**
 * 本地 Embedding 服务。
 * 
 * 职责：
 * 1. 封装 LlamaNative 的 JNI 调用，提供文本向量化接口。
 * 2. 这里目前使用本地 Llama 模型的向量生成能力。
 */
@Service
public class EmbeddingService {

    /**
     * 将文本转换为向量。
     * 
     * @param text 输入文本
     * @return 特征向量 (384维，根据 bmad_native.cpp 实现)
     */
    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[384];
        }
        
        try {
            return LlamaNative.getEmbedding(text);
        } catch (Exception e) {
            System.err.println("[EmbeddingService] 向量生成失败: " + e.getMessage());
            return new float[384];
        }
    }
}
