package com.bmad.controller;

import com.bmad.model.DocumentSegment;
import com.bmad.service.DocumentService;
import com.bmad.service.IndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器。
 * 
 * 职责：
 * 1. 接收前端上传的文档并进行解析。
 * 2. 将解析后的内容持久化到 Lucene 索引库。
 * 3. 提供关键词检索接口。
 */
@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private final DocumentService documentService;
    private final IndexService indexService;

    public DocumentController(DocumentService documentService, IndexService indexService) {
        this.documentService = documentService;
        this.indexService = indexService;
    }

    /**
     * 批量上传、解析并索引文档。
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocuments(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorLog = new StringBuilder();

        for (MultipartFile file : files) {
            try {
                // 1. 解析
                List<DocumentSegment> segments = documentService.parseDocument(file);
                
                // 2. 索引
                String filename = file.getOriginalFilename();
                if (filename == null) {
                    filename = "unknown";
                }
                indexService.indexDocument(filename, segments);
                successCount++;
            } catch (Exception e) {
                e.printStackTrace(); // 打印完整堆栈以便调试
                failCount++;
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                errorLog.append(file.getOriginalFilename()).append(": ").append(errorMsg).append("; ");
            }
        }

        response.put("success", failCount == 0);
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        if (failCount > 0) {
            response.put("error", errorLog.toString());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 接口：根据关键词搜索知识库。
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchDocs(@RequestParam("q") String query) {
        try {
            List<Map<String, Object>> results = indexService.search(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace(); // Log error for debugging
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 清空索引库。
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearIndex() {
        Map<String, Object> response = new HashMap<>();
        try {
            indexService.deleteAll();
            response.put("success", true);
            response.put("message", "索引库已成功清空");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
