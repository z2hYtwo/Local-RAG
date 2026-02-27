---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories', 'step-04-final-validation']
inputDocuments: ['_bmad-output/planning-artifacts/prd.md', '_bmad-output/planning-artifacts/architecture.md']
workflowType: 'epics-stories'
---

# Bmad-method - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Bmad-method, decomposing the requirements from the PRD and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

- **FR-1.1**: 原生解析 Markdown, Plain Text, PDF, Word (.docx), PowerPoint (.pptx)。
- **FR-1.2**: 支持基于文档结构（标题、层级、页码）的自适应语义分块。
- **FR-1.3**: 提取并关联文档元数据作为语义锚点。
- **FR-1.4**: 提供文件夹批量导入与拖拽交互。
- **FR-2.1**: 支持向量相似度语义搜索。
- **FR-2.2**: 集成本地 LLM，基于检索上下文生成答案。
- **FR-2.3**: 提供可追溯的原文引用链接。
- **FR-2.4**: 支持流式 (Streaming) 答案生成。
- **FR-3.1**: 维护请求队列，调度 5-10 个并发查询。
- **FR-3.2**: 根据硬件资源动态调整推理 Batch 大小。
- **FR-3.3**: 资源过载时提供排队降级提示。
- **FR-4.1**: 极简 Web 搜索界面。
- **FR-4.2**: 实时显示处理进度与推理状态。
- **FR-4.3**: 支持 PWA 特性。
- **FR-5.1**: 文档处理与推理 100% 本地运行。

### NonFunctional Requirements

- **NFR-1.1**: 10,000 分块库内语义检索延迟 < 100ms。
- **NFR-1.2**: 并发场景首字生成 (TTFT) P95 < 2s。
- **NFR-1.3**: 稳定处理 20 次/分钟 并发查询吞吐。
- **NFR-2.1**: 非推理状态内存总占用 < 6GB。
- **NFR-2.2**: 高并发推理内存总占用 < 10GB。
- **NFR-2.3**: 动态均衡 CPU 核心使用，防止系统卡死。
- **NFR-3.1**: JNI 崩溃自动重启，不中断 Web 服务。
- **NFR-3.2**: 索引原子性，防止非法关机导致本地库损坏。
- **NFR-4.1**: 零配置启动，单个可执行文件或脚本即可运行。

### Additional Requirements (from Architecture)

- **ARCH-01**: 使用 Spring Boot 3.4.x (Java 17/21) 和 React 18+ (Vite)。
- **ARCH-02**: JNI + llama.cpp (GGUF 格式) 实现本地推理。
- **ARCH-03**: 使用 Lucene 9.x (HNSW) 进行向量检索。
- **ARCH-04**: 使用 Apache POI, PDFBox, Flexmark 进行多格式文档解析。
- **ARCH-05**: Off-heap 优先分配策略 (DirectByteBuffer)。
- **ARCH-06**: 异步 Batch 推理调度。
- **ARCH-07**: 父子块关联索引 (Parent-Child Indexing)。
- **ARCH-08**: Monorepo 工程结构，Gradle 管理后端（集成 CMake）。
- **ARCH-09**: 外部模型路径加载配置。
- **ARCH-10**: SSE 流式反馈协议。
- **ARCH-11**: JNI 安全断路器。

### FR Coverage Map

- **FR-1.1 (解析多格式)**: Epic 1 - 实现 POI/PDFBox/Flexmark 集成
- **FR-1.2 (语义分块)**: Epic 1 - 实现自适应阈值分块逻辑
- **FR-1.3 (语义锚点)**: Epic 1 - 元数据与分块关联
- **FR-1.4 (批量导入)**: Epic 1 - 实现文件夹扫描与拖拽
- **FR-2.1 (向量搜索)**: Epic 2 - Lucene HNSW 索引与检索
- **FR-2.2 (LLM 生成)**: Epic 3 - llama.cpp 推理集成
- **FR-2.3 (引用链接)**: Epic 2 - 基于锚点的原文回溯
- **FR-2.4 (流式生成)**: Epic 3 - SSE 协议实现
- **FR-3.1 (请求队列)**: Epic 4 - 并发请求管理
- **FR-3.2 (动态 Batch)**: Epic 4 - 推理性能优化
- **FR-3.3 (过载提示)**: Epic 4 - 资源饱和降级反馈
- **FR-4.1 (极简 UI)**: Epic 3 - 核心搜索界面
- **FR-4.2 (状态显示)**: Epic 3 - 实时进度反馈
- **FR-4.3 (PWA 支持)**: Epic 5 - 离线化与桌面安装
- **FR-5.1 (100% 本地)**: Epic 1 - 环境隔离与验证

## Epic List

### Epic 1: 基础建设与多格式知识摄取 (Foundation & Multi-Format Ingestion) [COMPLETED]
用户能够批量导入不同格式的文档（MD, PDF, Word, PPT），系统通过 JNI 调用 llama.cpp 生成向量并进行语义分块。
**FRs covered:** FR-1.1, FR-1.2, FR-1.3, FR-1.4, FR-5.1
**Completed Stories:**
- [Story 1.1: JNI llama.cpp Vector Generation Integration](file:///e:/Bmad-method/_bmad-output/planning-artifacts/stories/1-1-jni-llama-cpp-vector-generation-integration.md)
- [Story 1.2: IndexService for Vector and Metadata Management](file:///e:/Bmad-method/_bmad-output/planning-artifacts/stories/1-2-index-service-for-vector-and-metadata-management.md)
- [Story 1.3: Multi-Format Document Parsing Engine Integration](file:///e:/Bmad-method/_bmad-output/planning-artifacts/stories/1-3-multi-format-document-parsing-engine-integration-md-pdf-word-ppt.md)
- [Story 1.4: Adaptive Threshold Semantic Chunking and Metadata Extraction](file:///e:/Bmad-method/_bmad-output/planning-artifacts/stories/1-4-adaptive-threshold-semantic-chunking-and-metadata-extraction.md)
- [Story 1.5: Folder Batch Import and Drag-and-Drop Interaction UI](file:///e:/Bmad-method/_bmad-output/planning-artifacts/stories/1-5-folder-batch-import-and-drag-and-drop-interaction-ui.md)

### Epic 2: 高性能向量检索与溯源 (High-Performance Vector Search & Retrieval)
用户可以通过语义搜索快速从数万个文档块中找到相关内容，响应延迟低于 100ms。
**FRs covered:** FR-2.1, FR-2.3

### Epic 3: 本地 LLM 推理与极简流式交互 (Local LLM Reasoning & Streaming Interface)
用户可以在极简 Web 界面提问，并实时看到流式生成的答案及其参考来源。
**FRs covered:** FR-2.2, FR-2.4, FR-4.1, FR-4.2

### Epic 4: 企业级并发调度与资源管理 (Enterprise Concurrency & Resource Scheduling)
5-10 名用户并发查询时，系统通过异步 Batch 调度和内存感知机制保持稳定运行。
**FRs covered:** FR-3.1, FR-3.2, FR-3.3

### Epic 5: 系统健壮性与 PWA 交付 (Robustness & PWA Delivery)
系统支持 PWA 安装，并具备 JNI 自动重启等自愈能力，确保 100% 本地运行的稳健性。
**FRs covered:** FR-4.3

## Epic 1: 基础建设与多格式知识摄取 (Foundation & Multi-Format Ingestion)

用户能够批量导入不同格式的文档（MD, PDF, Word, PPT），系统通过 JNI 调用 llama.cpp 生成向量并进行语义分块。

### Story 1.1: 初始化 Monorepo 项目结构与 Gradle 构建环境

As a Developer,
I want to set up a Monorepo project structure with Gradle and CMake integration,
So that I can manage both Java and Native code in a single, consistent build environment.

**Acceptance Criteria:**

**Given** a new project directory `Bmad-method`.
**When** I run the initialization script.
**Then** a `backend/` directory is created with Spring Boot 3.4.x and a `frontend/` directory with React 18+ (Vite).
**And** the `backend/build.gradle` is configured to include a CMake task for building native libraries.
**And** running `./gradlew build` successfully compiles both Java and placeholder C++ code.

### Story 1.2: 实现本地模型路径加载与 JNI 基础桥接

As a Developer,
I want to configure the system to load external GGUF models and establish a JNI bridge,
So that I can leverage native llama.cpp capabilities from the Java backend.

**Acceptance Criteria:**

**Given** a valid GGUF model file at an external path.
**When** the Spring Boot application starts with `model.path` configured in `application.yml`.
**Then** the system successfully loads the model via JNI.
**And** a JNI "handshake" test returns a successful status from the C++ layer.
**And** memory allocation for the model is verified to occur in Off-heap memory.

### Story 1.3: 多格式文档解析引擎集成 (MD, PDF, Word, PPT)

As a User,
I want to import various document formats including Markdown, PDF, Word, and PowerPoint,
So that I can build a comprehensive knowledge base from my existing files.

**Acceptance Criteria:**

**Given** a set of documents in .md, .pdf, .docx, and .pptx formats.
**When** I trigger the ingestion process.
**Then** Apache POI, PDFBox, and Flexmark successfully extract text content from each file.
**And** structural elements like headers and tables are preserved in the extracted text.

### Story 1.4: 自适应阈值语义分块与元数据提取

As a User,
I want my documents to be intelligently split into meaningful chunks with preserved metadata,
So that retrieval can precisely locate relevant sections with context.

**Acceptance Criteria:**

**Given** extracted text from documents.
**When** the semantic chunking engine processes the text.
**Then** chunks are created based on statistical breakpoints and structural markers.
**And** each chunk is tagged with "semantic anchors" including source filename, page number, and heading level.

### Story 1.5: 文件夹批量导入与拖拽交互 UI

As a User,
I want a simple drag-and-drop interface to import folders of documents,
So that I can quickly populate my knowledge base without complex menus.

**Acceptance Criteria:**

**Given** the Web application interface.
**When** I drag a folder containing supported files into the browser.
**Then** the UI displays a minimalist progress bar showing ingestion status.
**And** the backend recursively scans the folder and initiates the parsing pipeline for all valid files.

## Epic 2: 高性能向量检索与溯源 (High-Performance Vector Search & Retrieval)

用户可以通过语义搜索快速从数万个文档块中找到相关内容，响应延迟低于 100ms。

### Story 2.1: Lucene HNSW 向量索引构建与检索

As a Developer,
I want to use Lucene 9.x to index document chunks with HNSW vectors,
So that I can perform high-speed semantic searches within the local knowledge base.

**Acceptance Criteria:**

**Given** document chunks and their corresponding embedding vectors.
**When** I execute a search query.
**Then** Lucene returns the most relevant chunks using HNSW approximate nearest neighbor search.
**And** search latency for a 10,000-chunk index is verified to be under 100ms.

### Story 2.2: 实现父子块关联检索 (Parent-Child Indexing)

As a User,
I want search results to provide both precise matches and enough surrounding context,
So that I can fully understand the retrieved information.

**Acceptance Criteria:**

**Given** a query matching a specific sub-chunk.
**When** the retrieval engine processes the result.
**Then** it returns the sub-chunk for precision and its associated parent-chunk for broader context.
**And** the UI correctly displays the relationship between the matched fragment and its context.

### Story 2.3: 基于语义锚点的原文精确溯源

As a User,
I want to click on a search result and see exactly where it came from in the original document,
So that I can verify the information source.

**Acceptance Criteria:**

**Given** a retrieved chunk with metadata (semantic anchors).
**When** I click the "Source" link in the UI.
**Then** the application provides the specific filename, page number (for PDF/PPT), or heading (for Word/MD).
**And** the user is directed to the correct location in a preview or file reference.

## Epic 3: 本地 LLM 推理与极简流式交互 (Local LLM Reasoning & Streaming Interface)

用户可以在极简 Web 界面提问，并实时看到流式生成的答案及其参考来源。

### Story 3.1: JNI llama.cpp 推理集成与提示词模板

As a Developer,
I want to integrate llama.cpp's inference capabilities with optimized RAG prompts,
So that the LLM can generate accurate answers based on retrieved context.

**Acceptance Criteria:**

**Given** a user query and retrieved context chunks.
**When** the inference engine is called.
**Then** the system constructs a prompt using a predefined template and sends it to the native LLM via JNI.
**And** the LLM generates a response constrained by the provided context.

### Story 3.2: SSE 流式响应与极简问答 UI

As a User,
I want to see the LLM's answer being generated in real-time on a clean interface,
So that I don't have to wait for the entire response to be finished before reading.

**Acceptance Criteria:**

**Given** an active inference task.
**When** tokens are generated by the LLM.
**Then** the backend pushes tokens to the frontend using Server-Sent Events (SSE).
**And** the React UI displays the tokens as they arrive in a minimalist, distraction-free chat interface.

### Story 3.3: 实时处理进度与推理状态反馈

As a User,
I want to see clear status updates while the system is parsing documents or generating answers,
So that I know the system is working and how much progress has been made.

**Acceptance Criteria:**

**Given** an ongoing background task (ingestion or inference).
**When** the task status changes.
**Then** the UI displays a non-intrusive status indicator (e.g., "Parsing PDF...", "Thinking...").
**And** users receive visual confirmation when a task completes successfully or encounters an error.

## Epic 4: 企业级并发调度与资源管理 (Enterprise Concurrency & Resource Scheduling)

5-10 名用户并发查询时，系统通过异步 Batch 调度和内存感知机制保持稳定运行。

### Story 4.1: 后端请求队列与异步调度器

As a Developer,
I want to implement a request queue to manage concurrent user queries,
So that the system doesn't crash when multiple users submit questions simultaneously.

**Acceptance Criteria:**

**Given** multiple simultaneous query requests from different users.
**When** the requests reach the backend.
**Then** they are placed in a `LinkedBlockingQueue` and processed sequentially or in batches.
**And** users in the queue receive a "Waiting in queue..." status update via SSE.

### Story 4.2: JNI 异步 Batch 推理调度优化

As a Developer,
I want to optimize the JNI layer to process inference requests in batches,
So that I can maximize throughput while staying within memory limits.

**Acceptance Criteria:**

**Given** multiple pending requests in the queue.
**When** the native layer is available.
**Then** the JNI bridge submits requests as a batch to llama.cpp if supported by the model configuration.
**And** average response latency for concurrent users is minimized.

### Story 4.3: 内存感知与过载降级保护

As a User,
I want the system to remain responsive even under heavy load,
So that my work isn't interrupted by system crashes.

**Acceptance Criteria:**

**Given** a high-memory or high-CPU load scenario.
**When** resource thresholds (e.g., 10GB RAM) are approached.
**Then** the system automatically throttles new ingestion tasks and prioritizes active inference.
**And** a clear "System under heavy load, please wait" message is shown to new users.

## Epic 5: 系统健壮性与 PWA 交付 (Robustness & PWA Delivery)

系统支持 PWA 安装，并具备 JNI 自动重启等自愈能力，确保 100% 本地运行的稳健性。

### Story 5.1: JNI 安全断路器与自动重启机制

As a Developer,
I want a watchdog mechanism for the JNI layer,
So that the Web service remains available even if the native code crashes.

**Acceptance Criteria:**

**Given** a native crash or fatal error in the llama.cpp layer.
**When** the Java watchdog detects the failure.
**Then** it logs the error, clears native resources, and attempts to re-initialize the JNI bridge.
**And** active Web sessions are notified of the temporary interruption and recovery.

### Story 5.2: PWA 配置与桌面端独立运行体验

As a User,
I want to install the application on my desktop and use it like a native app,
So that I can access my local knowledge base quickly from my taskbar.

**Acceptance Criteria:**

**Given** the Web application running in a browser.
**When** I select "Install App".
**Then** the application is installed as a Progressive Web App (PWA).
**And** the app launches in its own window without browser chrome and provides a native-like icon on the desktop.
