# Story 1.3: Multi-format Document Parsing Engine Integration (MD, PDF, Word, PPT)

Status: done

## Story

As a User,
I want to import various document formats including Markdown, PDF, Word, and PowerPoint,
So that I can build a comprehensive knowledge base from my existing files.

## Acceptance Criteria

1. **Given** a set of documents in .md, .pdf, .docx, and .pptx formats.
2. **When** I trigger the ingestion process via the upload API.
3. **Then** Apache POI, PDFBox, and Flexmark successfully extract text content from each file.
4. **And** structural elements like headers and tables are preserved in the extracted text.
5. **And** the system provides a preview of the parsed content.

## Tasks / Subtasks

- [x] 实现 PDF 解析器 (PDFBox 3.0)
- [x] 实现 Word (.docx) 解析器 (Apache POI)
- [x] 实现 PowerPoint (.pptx) 解析器 (Apache POI)
- [x] 实现 Markdown 解析器并优化结构提取 (Flexmark/Raw Text)
- [x] 统一文档解析入口并增强错误处理
- [x] 验证所有格式的解析效果

## Dev Notes

- **PDF**: 使用 PDFBox 3.0 的 `Loader.loadPDF`。
- **Word/PPT**: 使用 Apache POI 的 `XWPFDocument` 和 `XMLSlideShow`。
- **Markdown**: 使用 Flexmark 进行解析，保留标题层级结构。
- **Encoding**: 确保所有文本读取均使用 UTF-8。

## References

- [Epic 1: Foundation & Multi-Format Ingestion](file:///e:/Bmad-method/_bmad-output/planning-artifacts/epics.md#Epic 1: 基础建设与多格式知识摄取 (Foundation & Multi-Format Ingestion))
- [build.gradle: Document Parsing Dependencies](file:///e:/Bmad-method/backend/build.gradle)
