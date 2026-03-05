# Local-RAG: 多模态本地知识库检索系统

Local-RAG 是一个高性能、端到端的本地知识库系统，采用 **Spring Boot + React + Lucene 9** 架构开发。它不仅支持传统的文档（PDF, DOCX, Markdown）检索，还具备强大的**图片语义检索**与**多模态视觉预览**功能。

## ✨ 核心特性

- **🖼️ 视觉检索增强**：支持 JPG, PNG 图片及 PDF 内部图片的自动提取与索引。通过向量匹配，实现“以词搜图”并提供即时视觉预览。
- **🔍 混合搜索架构**：结合 Lucene 9 的 KnnVectorField（向量搜索）与 TextField（关键词搜索），通过 MultiFieldQueryParser 实现精准的混合召回。
- **🚀 原生加速**：通过 JNI 技术集成 llama.cpp 核心逻辑，在本地利用 C++ 极速生成 384 维文本/图片嵌入向量。
- **🔒 隐私安全**：所有计算（包括向量生成与数据索引）均在本地完成，无需连接外部 API，确保数据不离开你的机器。
- **📂 全格式支持**：内置对 PDF, Word (DOCX), PowerPoint (PPTX), Markdown, TXT 等主流文档格式的解析能力。

## 🛠️ 技术栈

- **后端**: Java 17, Spring Boot 3.4, Apache Lucene 9.9
- **前端**: React 18, Vite, TypeScript
- **原生层**: C++, CMake, JNI (Java Native Interface)
- **构建工具**: Gradle, Node.js

## 🚀 快速开始

### 1. 环境准备
确保你的开发环境已安装以下工具：
- **JDK 17+**
- **Node.js 18+**
- **CMake 3.10+** (用于编译原生库)
- **Visual Studio 2022+** (Windows 环境下的编译器)

### 2. 初始化环境
项目根目录下提供了一个环境检查脚本：
```powershell
./setup.ps1
```

### 3. 启动项目

#### 启动后端 (Spring Boot)
```bash
./gradlew bootRun
```
*注意：首次运行会自动触发 `buildNative` 任务编译 C++ 动态链接库。*

#### 启动前端 (Vite)
```bash
cd frontend
npm install
npm run dev
```

## 📸 功能展示

1. **文件上传**：将包含图片或文字的 PDF/文档拖入系统。
2. **搜索体验**：
   - 输入“猫”，系统不仅会召回含有“猫”字的文档，还会展示匹配到的猫咪图片。
   - 支持通配符搜索，如 `*工作*`。
3. **视觉预览**：搜索结果中直接呈现图片缩略图，点击可查看详情。

## 📁 项目结构

```text
├── backend                 # Spring Boot 后端
│   ├── src/main/cpp        # C++ 原生向量生成逻辑
│   ├── src/main/java       # 业务逻辑与 Lucene 索引管理
│   └── lucene_index        # 本地索引存储 (已在 .gitignore 忽略)
├── frontend                # React 前端界面
└── setup.ps1               # 环境配置脚本
```

## 📄 开源协议

本项目采用 MIT 协议开源。
