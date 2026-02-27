---
stepsCompleted: [1, 2, 3]
inputDocuments: []
session_topic: '基于 Java 和 llama.cpp (JNI) 的本地 RAG 知识库构建'
session_goals: '探讨技术架构、JNI 调用实现细节、本地模型集成方案以及 RAG 流程优化'
selected_approach: ''
techniques_used: ['First Principles Thinking', 'SCAMPER', 'Chaos Engineering']
ideas_generated:
  - id: 1
    category: 'Architecture'
    title: 'Embedded Inference Gateway'
    concept: '将 llama.cpp 封装为高性能 C++ 共享内存网关，Java 通过文件描述符或共享内存读取结果。'
    novelty: '彻底消除 JNI 上下文切换开销，内存管理回归 C++ 精准控制。'
  - id: 2
    category: 'Performance'
    title: 'Semantic-Aware KV Cache Injection'
    concept: '在语义分块边界注入 KV Cache 信息，检索时通过热切换实现瞬间加载。'
    novelty: '将检索从文本级提升到推理状态级，大幅降低首字延迟。'
  - id: 3
    category: 'Memory Management'
    title: 'Off-Heap Pointer Registry'
    concept: 'Java 侧仅维护 C++ 内存地址的 Long 型映射，数据流不进入 JVM 堆。'
    novelty: '从根源杜绝 Java 侧 OOM，适合轻量级本地硬件。'
  - id: 4
    category: 'Chunking'
    title: 'Embedded Semantic Break Detector'
    concept: '在 llama.cpp 推理循环中加入 Hook，实时计算相似度阈值并自动分块。'
    novelty: '将分块从预处理变为推理中的动态行为。'
  - id: 5
    category: 'Infrastructure'
    title: 'In-Process Vector DB'
    concept: '在 Java 进程内嵌入 Lucene 或 HNSW 库，通过 JNI 传递向量。'
    novelty: '零运维成本，整个知识库可随文件夹迁移，适合小团队。'
  - id: 6
    category: 'Optimization'
    title: 'Small Model + Long Context Strategy'
    concept: '选用超轻量模型（如 Qwen-1.5B）配合 8K/16K 长上下文。'
    novelty: '以“量”补“质”，通过喂入更多上下文片段弥补参数量不足。'
  - id: 7
    category: 'Workflow'
    title: 'Two-Model Chunking Pipeline'
    concept: '极小 Embedding 模型负责粗筛断裂点，大模型负责精细边界调整。'
    novelty: '多模型协同平衡速度与精度。'
  - id: 8
    category: 'DevEx'
    title: 'Visual Semantic Map'
    concept: '开发 JavaFX UI 可视化展示文档分块过程中的相似度变化。'
    novelty: '直观学习语义分块逻辑，辅助算法调优。'
  - id: 9
    category: 'Accuracy'
    title: 'Bidirectional Semantic Anchors'
    concept: '分块时同时参考“块首句”和“块中心点”的相似度。'
    novelty: '解决长文本语义漂移问题，提升块内聚性。'
  - id: 10
    category: 'Efficiency'
    title: 'JNI Batch Embedding'
    concept: 'Java 侧收集句子 Batch 后一次性通过 JNI 塞入堆外内存推理。'
    novelty: '极大幅度减少 JNI 边界跨越次数，优化吞吐量。'
  - id: 11
    category: 'Innovation'
    title: 'Metadata-Injected Chunking'
    concept: '将文档标题路径（H1>H2）作为前缀合并到语义块向量计算中。'
    novelty: '让向量自带“身份属性”，解决小团队同名文档干扰。'
  - id: 12
    category: 'Efficiency'
    title: 'Differential Semantic Indexing'
    concept: '仅对文档差异部分进行语义分块和入库，检索时动态合并历史块。'
    novelty: '消除重复计算，对频繁更新的团队文档友好。'
  - id: 13
    category: 'Accuracy'
    title: 'Hierarchical Weighted Retrieval'
    concept: '在 Java 侧同时存储正文向量和标题路径向量，进行双向加权检索。'
    novelty: '即使正文匹配稍弱，标题匹配也能保证准确召回。'
  - id: 14
    category: 'Reliability'
    title: 'Dynamic Anchor Injection Prompt'
    concept: '生成阶段自动在 Prompt 中注入块的来源章节信息（如“源自XX规范”）。'
    novelty: '显著减少幻觉，提升小团队使用的信任感。'
  - id: 15
    category: 'Stability'
    title: 'JNI Safety Circuit Breaker'
    concept: '监控 JNI 调用耗时和内存水位，异常时 Java 侧强行中断 native 线程。'
    novelty: '牺牲单次请求换取系统全局稳定。'
  - id: 16
    category: 'Reliability'
    title: 'Hybrid Semantic-Keyword Fallback'
    concept: '始终维持 Lucene 索引，当语义检索得分低时自动切换到关键词检索。'
    novelty: '为轻量级模型提供可靠的保底召回。'
context_file: ''
---

# Brainstorming Session Results

**Facilitator:** z2h
**Date:** 2026-02-26

## Session Overview

**Topic:** 基于 Java 和 llama.cpp (JNI) 的本地 RAG 知识库构建
**Goals:** 探讨技术架构、JNI 调用实现细节、本地模型集成方案以及 RAG 流程优化

### Session Setup

用户希望利用 Java 技术栈，通过 JNI 调用本地部署的 llama.cpp 来实现大模型推理，并以此构建 RAG 知识库。这是一个结合了底层 C++ 推理引擎与高层 Java 应用开发的典型场景。

## Generated Ideas

### 核心架构与性能
- **Embedded Inference Gateway**: 消除 JNI 切换开销。
- **Semantic-Aware KV Cache Injection**: 推理状态级检索。
- **JNI Batch Embedding**: 吞吐量优化。

### 内存与稳定性
- **Off-Heap Pointer Registry**: 根除 JVM OOM。
- **JNI Safety Circuit Breaker**: 系统级稳定性防护。

### 语义分块与准度 (重点)
- **Metadata-Injected Chunking**: 结构化锚点注入（H1>H2 前缀）。
- **Bidirectional Semantic Anchors**: 解决语义漂移。
- **Hybrid Semantic-Keyword Fallback**: 关键词保底机制。

## Final Technical Blueprint (轻量级 RAG 架构蓝图)

### 1. 数据入库流 (Ingestion Pipeline)
- **解析**: 使用 Flexmark 解析 Markdown，维护标题堆栈。
- **分块**: 采用“结构化锚点 + 自适应阈值语义分块”。
- **向量化**: Java 侧 Batch 收集句子 -> `DirectByteBuffer` -> JNI -> llama.cpp (Q8_0 Embedding 模型)。
- **存储**: 嵌入式 Lucene (存文本+元数据) + HNSW (存向量)。

### 2. 检索与推理流 (Inference Pipeline)
- **触发**: Java 侧监控输入，进行语义预加载。
- **检索**: 双路并行（语义向量 + 关键词），加权得分。
- **生成**: llama.cpp (Q4_K_M 或 Q6_K 模型) -> 流式输出 -> Java 侧动态注入来源锚点。

### 3. 稳定性保障
- **内存**: 使用 `Cleaner` API 自动回收堆外内存。
- **并发**: 单线程队列处理 JNI 调用，防止段错误。
- **保底**: 语义得分过低时自动触发关键词搜索。
