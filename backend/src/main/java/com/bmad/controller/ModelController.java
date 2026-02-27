package com.bmad.controller;

import com.bmad.service.ModelService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 模型控制层：对外暴露 RESTful 接口。
 * 
 * 职责：
 * 1. 暴露 REST 接口，让前端（React）能够通过 HTTP 调用。
 * 2. 处理 HTTP 请求，调用 ModelService 的业务逻辑。
 * 3. 将结果封装为标准的 JSON 格式返回给前端。
 */
@RestController
@RequestMapping("/api/model")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * 接口：获取模型当前加载状态。
     * @return 包含加载状态的 JSON 响应
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("isLoaded", modelService.isLoaded());
        response.put("handshake", modelService.checkStatus());
        return response;
    }

    /**
     * 接口：请求加载模型。
     * @return 包含加载结果信息的 JSON 响应
     */
    @PostMapping("/load")
    public Map<String, String> loadModel() {
        Map<String, String> response = new HashMap<>();
        String message = modelService.loadModel();
        response.put("message", message);
        return response;
    }

    /**
     * 接口：请求卸载模型。
     * @return 包含卸载结果信息的 JSON 响应
     */
    @PostMapping("/unload")
    public Map<String, String> unloadModel() {
        Map<String, String> response = new HashMap<>();
        String message = modelService.unloadModel();
        response.put("message", message);
        return response;
    }
}
