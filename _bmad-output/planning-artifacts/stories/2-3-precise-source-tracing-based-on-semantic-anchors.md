# Story 2.3: Precise Source Tracing based on Semantic Anchors

## Background
当前检索结果只返回文件名和文本内容。用户难以快速定位到原文的具体位置（如 PDF 页码、PPT 幻灯片编号）。我们需要在解析阶段提取这些“语义锚点” (Semantic Anchors)，并随索引存储，最终在检索结果中展示。

## Status: done

## User Story
作为一个用户，我希望在查看搜索结果时，能看到该结果出自文档的第几页或第几张幻灯片，以便我快速翻阅原文进行核对。

## Acceptance Criteria
- [ ] 重构 `DocumentService` 解析逻辑，支持提取位置元数据：
  - **PDF**: 提取页码 (Page Number)。
  - **PPTX**: 提取幻灯片编号 (Slide Index)。
  - **DOCX**: 提取段落索引或章节标题 (Paragraph Index / Section Header)。
- [ ] 更新 `IndexService` 索引结构：
  - 新增字段 `page_number` (IntField) 或 `anchor_text` (StringField, e.g. "Page 5", "Slide 3").
- [ ] 检索结果包含位置信息：
  - 搜索接口返回 `anchor` 字段。
- [ ] 前端展示优化：
  - 在搜索结果卡片中显示来源位置 (e.g., "prd.pdf (Page 5)").

## Technical Notes
- **Refactoring**: Change `DocumentService.parseDocument` return type from `String` to `List<DocumentSegment>`.
- **Data Structure**:
  ```java
  class DocumentSegment {
      String content;
      int pageNumber; // -1 if not applicable
      String sectionHeader;
  }
  ```
- **PDFBox**: Override `PDFTextStripper.writeString` or process page-by-page.
- **POI**: Loop slides/paragraphs and create segments.

## Tasks
- [x] 定义 `DocumentSegment` 类.
- [x] 重构 `DocumentService` 的解析方法 (PDF, PPTX, DOCX) 以返回 `List<DocumentSegment>`.
- [x] 更新 `IndexService.indexDocument` 接受 `List<DocumentSegment>`.
- [x] 修改 `IndexService.search` 返回元数据.
- [x] 验证元数据准确性.
