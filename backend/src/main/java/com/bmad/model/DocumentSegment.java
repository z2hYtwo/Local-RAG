package com.bmad.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档片段模型。
 * 用于存储从文档中提取的文本内容及其关联的元数据（如页码、幻灯片编号、章节标题等）。
 */
public class DocumentSegment {
    
    private String content;
    private String imageData; // Base64 encoded image data
    private Map<String, Object> metadata;

    public DocumentSegment() {
        this.metadata = new HashMap<>();
    }

    public DocumentSegment(String content) {
        this.content = content;
        this.metadata = new HashMap<>();
    }

    public DocumentSegment(String content, String imageData) {
        this.content = content;
        this.imageData = imageData;
        this.metadata = new HashMap<>();
    }

    public DocumentSegment(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
}
