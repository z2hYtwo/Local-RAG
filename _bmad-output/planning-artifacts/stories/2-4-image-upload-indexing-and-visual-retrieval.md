# Story 2.4: 支持图片上传、索引与可视化检索

## Context
用户希望上传图片（或从 PDF 中提取图片）后，能够通过关键词检索到相似度高的图片，并在搜索结果中直观地看到图片预览。

## Acceptance Criteria
- [ ] **DocumentService** 扩展:
  - 支持上传 `jpg`, `png`, `jpeg` 格式图片。
  - 支持从 `pdf` 文件中提取嵌入的图片。
  - 将图片转换为 Base64 字符串存储在 `DocumentSegment` 中。
- [ ] **IndexService** 扩展:
  - 在 Lucene 索引中新增 `image_data` (Stored, 不 Indexed) 字段。
  - 为图片提供“描述文本”以便关键词检索（初始使用文件名或简单的 OCR，如果没有 OCR 库则模拟描述）。
- [ ] **前端展示**:
  - 如果搜索结果包含 `image_data`，在卡片中显示图片预览。
- [ ] **检索功能**:
  - 通过关键词检索到图片描述时，能返回图片本身。

## Tasks
- [x] 创建此 Story 文档。
- [ ] 修改 `DocumentSegment` 以支持 `image_data` 字段。
- [ ] 在 `DocumentService` 中实现图片文件的读取逻辑。
- [ ] 在 `DocumentService` 中实现从 PDF 提取图片的逻辑 (使用 PDFBox)。
- [ ] 更新 `IndexService` 的索引和检索逻辑，返回 `image_data` 给前端。
- [ ] 在前端 `App.tsx` 中增加图片渲染逻辑。
- [ ] 验证功能。
