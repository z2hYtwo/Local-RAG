---
stepsCompleted: ['step-01-init', 'step-02-context', 'step-03-starter', 'step-04-decisions', 'step-05-patterns', 'step-06-structure', 'step-07-validation', 'step-08-complete']
inputDocuments: ['_bmad-output/planning-artifacts/prd.md', '_bmad-output/brainstorming/brainstorming-session-2026-02-26.md']
workflowType: 'architecture'
project_name: 'Bmad-method'
user_name: 'z2h'
date: '2026-02-26'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## 1. Project Context Analysis (项目背景分析)

### 1.1 核心需求与约束 (Requirements & Constraints)
- **并发与资源 (Concurrency & Resources)**: 16GB 内存笔记本运行 7B 模型，支持 5-10 人并发查询。内存占用红线为 10GB。
- **高性能桥接 (High-Performance Bridge)**: JNI 零拷贝技术，利用 `DirectByteBuffer` 映射内存，消除 Java/C++ 序列化开销。
- **多格式支持 (Multi-format Support)**: 原生支持 MD, PDF, Word, PPT，通过自适应语义分块保证上下文完整性。
- **实时反馈 (Real-time Feedback)**: 极简 SPA 架构，利用 SSE (Server-Sent Events) 实现流式状态反馈。

### 1.2 架构挑战 (Architectural Challenges)
- **挑战 1: 内存精算 (Memory Budgeting)**: 模型占用约 5GB，Java 堆与 Off-heap 必须精简在 5GB 内，避免 16GB 系统 swap。
- **挑战 2: 并发调度 (Concurrency Orchestration)**: 需要在 C++ 层实现 Batch 推理调度，平衡响应延迟与系统吞吐。
- **挑战 3: 跨语言稳定性 (Cross-language Stability)**: JNI 崩溃风险管理与自动重启机制。

## 2. Starter Technology Stack (基础技术栈选型)

### 2.1 核心框架 (Core Frameworks)
- **后端 (Backend)**: **Spring Boot 3.4.x** (Java 17/21)
  - 理由: 强大的集成能力与社区支持。通过 `-Xmx` 限制堆内存并优化 Off-heap 使用。
- **前端 (Frontend)**: **React 18+ (Vite)**
  - 理由: 极简 SPA 开发体验，原生支持 SSE 流式接收。

### 2.2 关键组件 (Key Components)
- **跨语言桥接**: **JNI + llama.cpp** (GGUF 格式)。
- **向量检索 (Vector Store)**: **Lucene 9.x (HNSW)**
  - 理由: 嵌入式 Java 库，无需额外进程，支持 HNSW 向量检索，极低内存占用。
- **多格式解析**: **Apache POI** (Word/PPT), **PDFBox** (PDF), **Flexmark** (Markdown)。

## 3. Core Architectural Decisions (核心架构决策)

### 3.1 内存与并发 (Memory & Concurrency)
- **[AD-01] Off-heap 优先分配策略**: 
  - **决策**: Java 堆内存 (`-Xmx`) 严格限制在 4GB 以内，大内存分配（模型加载、推理缓存）通过 `DirectByteBuffer` 在 Off-heap 完成。
  - **理由**: 确保 16GB 环境下，操作系统有足够余量避免 Swap，同时减少 JVM GC 对推理性能的干扰。
- **[AD-02] 异步 Batch 推理调度**: 
  - **决策**: Java 端使用 `LinkedBlockingQueue` 接收请求，JNI 层实现单实例 Batch 处理。
  - **理由**: 5-10 人并发时通过排队机制保护内存不崩盘，同时利用 Batch 提升吞吐量。

### 3.2 深度文档解析 (Deep Document Parsing)
- **[AD-03] 结构化多模态解析策略**: 
  - **决策**: 对 PPT 和 Word 中的表格采用“Markdown Table 转换”，对图片采用“元数据+OCR/Caption 提取”。
  - **实现**: 使用 Apache POI 遍历形状 (Shapes) 和表格 (Tables)，将非文本元素转化为结构化文本描述后并入语义分块。
  - **理由**: 满足用户对 PPT/Word 中非文本内容的支持需求。

### 3.3 检索增强 (Retrieval Strategy)
- **[AD-04] 父子块关联索引 (Parent-Child Indexing)**: 
  - **决策**: Lucene 存储子块向量 (300-500 tokens) 用于匹配，存储父块 (1000+ tokens) 用于提供背景。
  - **理由**: 平衡检索精度与生成上下文的连贯性。

## 4. Implementation Patterns & Consistency (设计模式与一致性)

### 4.1 接口与数据 (API & Data)
- **[CP-01] 统一 RESTful 响应规范**: 
  - 采用全小写、中划线分隔的端点命名。统一响应体包含 `success`, `data`, `error` (code/message) 和 `timestamp`。
- **[CP-02] 轻量级状态管理**: 
  - 前端使用 **Zustand** 管理全局状态（如当前文档、推理状态），替代繁重的 Redux。

### 4.2 通信与异常 (Communication & Exceptions)
- **[CP-03] 流式反馈协议**: 
  - 使用 SSE 统一推送 `status-update` (解析进度) 和 `inference-token` (生成流)。
- **[CP-04] JNI 安全断路器**: 
  - 实现 Java 层监控，当原生代码长时间无响应或触发致命错误时，自动尝试安全重启 JNI 实例，确保 Web 服务不中断。

## 5. Project Structure & Organization (项目结构与组织)

### 5.1 目录结构 (Directory Tree)
- **[PS-01] Monorepo 混合工程结构**:
  ```text
  bmad-method/
  ├── backend/                # Spring Boot (Gradle)
  │   ├── src/main/java/      # Java 源代码 (Core, JNI, Lucene)
  │   ├── src/main/cpp/       # Native 源代码 (llama.cpp 桥接)
  │   ├── build.gradle        # Gradle 配置 (含 CMake 编译集成)
  │   └── ...
  ├── frontend/               # React (Vite)
  │   ├── src/                # UI 组件, Zustand Stores
  │   ├── package.json
  │   └── ...
  └── _bmad-output/           # 规划文档
  ```

### 5.2 配置管理 (Configuration)
- **[PS-02] 外部模型路径加载**:
  - **决策**: 模型文件不打包进 jar，通过环境变量或 `application.yml` 配置文件中的 `model.path` 指定外部路径。
  - **理由**: 7B 模型体积巨大，且方便用户根据硬件环境灵活更换模型版本。

## 6. Architecture Validation (架构验证)

- **[V-01] 内存精算验证**: 模型(5GB) + Java(4GB) = 9GB < 10GB 限制，通过。
- **[V-02] 并发吞吐验证**: Batch 调度 + 排队机制可平滑 5-10 人负载，通过。
- **[V-03] 深度解析验证**: POI 结构化提取支持 PPT/Word 非文本内容，通过。
