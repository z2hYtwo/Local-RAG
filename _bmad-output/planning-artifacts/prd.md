---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-02b-vision', 'step-02c-executive-summary', 'step-03-success', 'step-04-journeys', 'step-05-domain', 'step-06-innovation', 'step-07-project-type', 'step-08-scoping', 'step-09-functional', 'step-10-nonfunctional', 'step-11-polish', 'step-12-complete']
inputDocuments: ['_bmad-output/brainstorming/brainstorming-session-2026-02-26.md']
workflowType: 'prd'
projectClassification:
  type: 'Web Application'
  domain: 'Scientific/AI'
  context: 'Greenfield'
  complexity: 'Medium'
  target_hardware: '16GB RAM Laptop'
  supported_formats: ['Markdown', 'Word (.doc, .docx)', 'PDF', 'PowerPoint (.ppt, .pptx)', 'Plain Text']
  ui_style: 'Minimalist'
  concurrency_requirement: '5-10 simultaneous users'
---

# Product Requirements Document - Bmad-method

**Author:** z2h
**Date:** 2026-02-26

## 1. Executive Summary (执行摘要)

### 1.1 Product Vision (产品愿景)
构建轻量级、高性能的本地 RAG 独立 Web 应用，面向 5-10 人研发团队。采用**极简主义 (Minimalist)** UI，支持**多用户并发查询**，确保在 16GB 内存笔记本上流畅运行，实现企业级私有知识库的语义检索与问答。

### 1.2 Core Value Proposition (核心价值主张)
- **极速本地推理**: JNI 深度集成 llama.cpp，支持多线程并发请求调度。
- **高精度语义处理**: 自适应阈值语义分块（Semantic Chunking）与结构化锚点技术。
- **多格式原生支持**: 原生支持 Markdown、Word、PDF 以及 PowerPoint (PPT/PPTX)。
- **零拷贝内存优化**: Java `DirectByteBuffer` 与 C++ 共享内存技术，优化 16GB 环境内存布局。
- **极简交互体验**: 界面无冗余，提供直观、无干扰的知识获取路径。

## 2. Success Criteria (成功标准)

### 2.1 Technical Success (技术成功)
- **内存占用**: 加载 7B 参数量化模型（如 Q4_K_M）并发运行时，内存占用稳定在 **10GB 以内**。
- **并发性能**: 
  - 支持 **5-10 个并发查询**，通过请求队列与 Batch 推理优化，维持响应时间稳定。
  - Embedding 获取：并发场景单句延迟 P95 < **200ms**。
- **稳定性**: JNI 熔断机制在内存压力大或并发过载时，自动进入排队模式，确保系统不崩溃。

### 2.2 User Success (用户成功)
- **极简体验**: 无广告、无冗余功能，极简搜索路径。
- **检索质量**: 多格式混合库前 3 个结果语义相关度 > **80%**。
- **并发透明**: 高并发期间提供明确进度提示，维持流畅心理预期。

### 2.3 Business Success (业务成功)
- **私有化验证**: 5-10 人团队实现 100% 本地文档检索，消除 API 成本与隐私风险。

## 3. User Journeys (用户旅程)

### 3.1 Journey 1: 快速构建知识库 (0 门槛上手)
- **启动**: 用户启动独立 Web 应用。
- **导入**: 拖拽文件夹（含 MD, Doc, PDF, PPT）至浏览器。
- **解析**: 系统后台通过 JNI 快速分块并生成向量，UI 显示极简进度条。
- **完成**: 进度条消失，搜索框自动激活。

### 3.2 Journey 2: 高效协作查询 (并发与准确度)
- **提问**: 团队成员 A 和 B 同时在搜索框输入技术问题。
- **调度**: 系统识别并发请求，进入 JNI 并发调度队列。
- **反馈**: 界面即时显示“正在思考...”，利用语义分块精准定位文档片段。
- **结果**: 极简答案呈现，点击来源直接定位至具体页码或章节。

## 4. Domain Requirements (领域特定需求)

### 4.1 AI & Scientific Computing (AI 与科学计算)
- **模型格式**: 支持 GGUF 格式以兼容 llama.cpp。
- **多模型支持**: 可配置不同 Embedding 模型（如 BGE-Small/Base-ZH）以平衡性能与内存。
- **资源调度**: 建立“内存感知”调度器，动态调整 Batch 推理大小。
- **可解释性**: 生成答案附带原文片段引用及语义相似度得分。

## 5. Innovation Focus (创新重点)

### 5.1 自适应阈值语义分块 (Adaptive Semantic Chunking)
结合 Flexmark 解析 Markdown 结构（标题、列表），利用统计断点（Statistical Breakpoint）动态决定分块边界。

### 5.2 JNI 零拷贝高性能桥接 (Zero-Copy JNI Bridge)
使用 `DirectByteBuffer` 映射内存，消除 Java 与 C++ 序列化开销，降低 GC 压力。

### 5.3 结构化语义锚点 (Structured Semantic Anchors)
将文档元数据（PPT 页码、Word 标题层级）作为锚点注入向量空间，实现精准来源追溯。

## 6. Project-Type Requirements (项目类型需求)

### 6.1 Web Application (Web 应用)
- **架构**: SPA (Single Page Application) 架构。
- **浏览器支持**: 现代浏览器（Chrome/Edge/Firefox），利用 WebAssembly 潜能。
- **实时反馈**: SSE (Server-Sent Events) 实现流式反馈。
- **PWA 增强**: 支持 PWA，桌面端独立运行。

## 7. Development Roadmap (开发路线图)

### 7.1 Phase 1: MVP (核心价值与多格式支持)
- **核心引擎**: JNI 零拷贝桥接、自适应语义分块引擎。
- **全格式支持**: 原生支持 Markdown, Plain Text, PDF, Word (.docx), PowerPoint (.pptx)。
- **高性能检索**: 基础向量检索与结构化锚点注入。
- **并发雏形**: 基础请求队列调度，支持 5-10 人并发测试。
- **UI 交付**: 极简 Web 搜索界面与 SSE 实时反馈。

### 7.2 Phase 2: Growth (优化与增强)
- **资源调度优化**: 深度集成“内存感知”调度器，动态调整 Batch 推理。
- **PWA 交付**: 完整的 PWA 支持，实现桌面端独立运行体验。
- **可解释性增强**: 完善引用的相似度得分展示与文档定位精度。

### 7.3 Phase 3: Vision (稳定与扩展)
- **混沌工程自愈**: JNI 崩溃自动重启与内存泄露主动防御。
- **多模型管理**: UI 支持热切换不同的 Embedding 模型。
- **协作增强**: 团队搜索历史管理与知识库版本控制。

## 8. Functional Requirements (功能需求契约)

### 8.1 知识摄取与处理 (Knowledge Ingestion)
- **FR-1.1**: 原生解析 Markdown, Plain Text, PDF, Word (.docx), PowerPoint (.pptx)。
- **FR-1.2**: 支持基于文档结构（标题、层级、页码）的自适应语义分块。
- **FR-1.3**: 提取并关联文档元数据作为语义锚点。
- **FR-1.4**: 提供文件夹批量导入与拖拽交互。

### 8.2 语义搜索与推理 (Semantic Search & Inference)
- **FR-2.1**: 支持向量相似度语义搜索。
- **FR-2.2**: 集成本地 LLM，基于检索上下文生成答案。
- **FR-2.3**: 提供可追溯的原文引用链接。
- **FR-2.4**: 支持流式 (Streaming) 答案生成。

### 8.3 并发与资源调度 (Concurrency & Resource Management)
- **FR-3.1**: 维护请求队列，调度 5-10 个并发查询。
- **FR-3.2**: 根据硬件资源动态调整推理 Batch 大小。
- **FR-3.3**: 资源过载时提供排队降级提示。

### 8.4 用户交互与展现 (User Experience)
- **FR-4.1**: 极简 Web 搜索界面。
- **FR-4.2**: 实时显示处理进度与推理状态。
- **FR-4.3**: 支持 PWA 特性。

### 8.5 隐私与安全 (Privacy & Security)
- **FR-5.1**: 文档处理与推理 100% 本地运行。

## 9. Non-Functional Requirements (非功能性需求)

### 9.1 性能指标 (Performance)
- **NFR-1.1**: 10,000 分块库内语义检索延迟 < 100ms (基于 APM 监控)。
- **NFR-1.2**: 并发场景首字生成 (TTFT) P95 < 2s。
- **NFR-1.3**: 稳定处理 20 次/分钟 并发查询吞吐。

### 9.2 资源效率 (Resource Efficiency)
- **NFR-2.1**: 非推理状态内存总占用 < 6GB。
- **NFR-2.2**: 高并发推理内存总占用 < 10GB。
- **NFR-2.3**: 动态均衡 CPU 核心使用，防止系统卡死。

### 9.3 可靠性与稳健性 (Reliability & Robustness)
- **NFR-3.1**: JNI 崩溃自动重启，不中断 Web 服务。
- **NFR-3.2**: 索引原子性，防止非法关机导致本地库损坏。

### 9.4 易用性 (Usability)
- **NFR-4.1**: 零配置启动，单个可执行文件或脚本即可运行。
