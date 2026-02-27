# Story 1.4: Adaptive Threshold Semantic Chunking and Metadata Extraction

Status: done

## Story

As a User,
I want my documents to be intelligently split into meaningful chunks with preserved metadata,
So that retrieval can precisely locate relevant sections with context.

## Acceptance Criteria

1. **Given** extracted text from documents.
2. **When** the semantic chunking engine processes the text.
3. **Then** chunks are created based on statistical breakpoints (e.g., paragraphs, sentence limits) rather than fixed-length cuts.
4. **And** each chunk is tagged with metadata including source filename and potentially heading level or page number.
5. **And** the system ensures chunk overlap to maintain semantic continuity.

## Tasks / Subtasks

- [x] 设计自适应分块算法 (基于段落和句子)
- [x] 实现元数据提取与存储逻辑
- [x] 在 IndexService 中集成高级分块器
- [x] 验证分块后的语义一致性与检索准确度

## Dev Notes

- **Chunking Strategy**: 优先按段落（`\n\n`）分割，如果段落过长，则按句子（`.`, `?`, `!`）进一步切分。
- **Overlap**: 每个分块建议保留 10-20% 的重叠内容。
- **Metadata**: 在 Lucene `Document` 中添加更多字段，如 `parent_id` (用于后续 Epic 2.2)。

## References

- [Epic 1: Foundation & Multi-Format Ingestion](file:///e:/Bmad-method/_bmad-output/planning-artifacts/epics.md#Epic 1: 基础建设与多格式知识摄取 (Foundation & Multi-Format Ingestion))
- [IndexService.java: Current Chunking Logic](file:///e:/Bmad-method/backend/src/main/java/com/bmad/service/IndexService.java)
