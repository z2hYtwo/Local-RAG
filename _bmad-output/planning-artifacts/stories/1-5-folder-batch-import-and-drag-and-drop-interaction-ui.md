# Story 1.5: Folder Batch Import and Drag-and-Drop Interaction UI

Status: done

## Story

As a User,
I want a simple drag-and-drop interface to import folders of documents,
So that I can quickly populate my knowledge base without complex menus.

## Acceptance Criteria

1. **Given** the Web application interface.
2. **When** I drag a folder containing supported files into the browser or use a folder picker.
3. **Then** the UI displays a progress bar or status indicator showing ingestion progress.
4. **And** the backend recursively scans the folder and initiates the parsing pipeline for all valid files (.md, .pdf, .docx, .pptx, .txt).
5. **And** users are notified when the batch import is complete.

## Tasks / Subtasks

- [x] 在前端实现拖拽上传区域 (Drag & Drop)
- [x] 在前端添加文件夹选择器 (webkitdirectory)
- [x] 后端添加批量上传接口或支持多文件上传
- [x] 实现前端处理进度实时显示 (已集成在状态消息中)
- [x] 验证文件夹内不同格式文件的递归解析与索引

## Dev Notes

- **Frontend**: 使用 `onDragOver` 和 `onDrop` 事件处理拖拽。对于文件夹选择，使用 `<input type="file" webkitdirectory />`。
- **Backend**: 修改 `DocumentController` 的 `uploadDocument` 接口以支持 `MultipartFile[]` 或添加专用批量接口。
- **UX**: 进度显示可以使用一个简单的列表或百分比条。

## References

- [Epic 1: Foundation & Multi-Format Ingestion](file:///e:/Bmad-method/_bmad-output/planning-artifacts/epics.md#Epic 1: 基础建设与多格式知识摄取 (Foundation & Multi-Format Ingestion))
- [App.tsx: Current Upload Logic](file:///e:/Bmad-method/frontend/src/App.tsx)
