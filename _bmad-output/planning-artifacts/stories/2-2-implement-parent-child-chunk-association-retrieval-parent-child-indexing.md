# Story 2.2: Implement Parent-Child Chunk Association Retrieval

## Background
为了提高 RAG (Retrieval-Augmented Generation) 的生成质量，检索到的文档片段需要包含足够的上下文信息。单纯的切片（Chunking）如果太小，可能丢失上下文；如果太大，向量的语义可能被稀释。Parent-Child Indexing 是一种平衡方案：使用小切片（Child Chunk）进行精准的向量检索，但返回其所属的大切片（Parent Chunk）作为上下文。

## Status: done

## User Story
作为一个用户，我希望系统检索到的内容包含完整的上下文段落，而不仅仅是零散的句子，以便模型能更准确地回答我的问题。

## Acceptance Criteria
- [ ] 实现 Parent-Child 切分逻辑：
  - **Parent Chunk**: 较大粒度（如 800-1000 字符），作为返回给 LLM 的上下文。
  - **Child Chunk**: 较小粒度（如 200-300 字符），用于生成向量索引。
- [ ] 索引结构更新：
  - Lucene Document 存储 Child Chunk 的向量。
  - Lucene Document 存储 Parent Chunk 的文本内容 (`content` 字段)。
  - 建立 Child 到 Parent 的映射关系（或直接在 Child Document 中存储 Parent 内容）。
- [ ] 检索逻辑更新：
  - 对 Query 生成向量。
  - 搜索 Child Chunk 的向量索引。
  - 返回对应的 Parent Chunk 文本。
- [ ] 验证：检索结果的文本长度应显著大于用于匹配的向量片段长度，且语义连贯。

## Technical Notes
- **Strategy**: Small-to-Big Retrieval.
- **Implementation**:
  - 修改 `DocumentService` 的切分逻辑。
  - 每个 Parent Chunk 切分为多个 Child Chunks。
  - 每个 Child Chunk 作为一个 Lucene Document 索引。
  - Field `vector`: Child Chunk 的 Embedding。
  - Field `content`: Parent Chunk 的 Text (Stored)。
  - Field `child_content`: Child Chunk 的 Text (Stored, Optional for debug)。
  - Field `parent_id`: Parent Chunk ID (Optional).
- **Parameters**:
  - Parent Size: ~1000 chars.
  - Child Size: ~200-300 chars.
  - Overlap: ~50 chars.

## Tasks
- [ ] 修改 `DocumentService` 实现 Parent-Child 切分算法。
- [ ] 更新 `IndexService` 的索引构建逻辑，适配新的字段结构。
- [ ] 重新索引现有文档（或提供重建立索引的工具/接口）。
- [ ] 验证检索结果是否返回了更完整的上下文。
