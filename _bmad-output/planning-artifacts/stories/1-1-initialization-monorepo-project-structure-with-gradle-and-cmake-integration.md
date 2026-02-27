# Story 1.1: Initialization Monorepo Project Structure with Gradle and CMake Integration

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a Developer,
I want to set up a Monorepo project structure with Gradle and CMake integration,
so that I can manage both Java and Native code in a single, consistent build environment.

## Acceptance Criteria

1. **Given** a new project directory `Bmad-method`.
2. **When** I run the initialization script.
3. **Then** a `backend/` directory is created with Spring Boot 3.4.x and a `frontend/` directory with React 18+ (Vite).
4. **And** the `backend/build.gradle` is configured to include a CMake task for building native libraries.
5. **And** running `./gradlew build` successfully compiles both Java and placeholder C++ code.

## Tasks / Subtasks

- [x] 初始化 Monorepo 根目录结构 (AC: 1, 2)
  - [x] 创建 `backend/` 和 `frontend/` 目录
  - [x] 配置根目录 `settings.gradle` 包含后端模块
- [x] 配置 Spring Boot 3.4.x 后端 (AC: 3)
  - [x] 生成基础 Spring Boot 项目结构 (Java 17/21)
  - [x] 配置 `backend/build.gradle` 基础依赖
- [x] 配置 React 18+ (Vite) 前端 (AC: 3)
  - [x] 手动初始化 React 项目结构 (Vite 模式)
  - [x] 确保前端代码位于 `frontend/` 目录
- [x] 集成 CMake 构建原生库 (AC: 4)
  - [x] 在 `backend/src/main/cpp` 创建占位符 C++ 代码和 `CMakeLists.txt`
  - [x] 在 `backend/build.gradle` 中添加构建任务
- [x] 验证全流程构建 (AC: 5)
  - [x] 已完成 Gradle + CMake 构建验证

## Dev Notes

- **Architecture Compliance**: 使用 Spring Boot 3.4.x, React 18+, Gradle (Kotlin DSL 或 Groovy), CMake. [Source: _bmad-output/planning-artifacts/architecture.md#Starter Technology Stack]
- **Monorepo Strategy**: 根目录管理 Gradle wrapper, `backend/` 管理 Java/C++, `frontend/` 管理 React.
- **JNI Bridge**: 原生库名称暂定为 `bmad_native`, 编译产物需放在 `backend/src/main/resources/lib` 或指定路径以供 Java 加载.

### Project Structure Notes

- **Backend**: `e:/Bmad-method/backend`
- **Frontend**: `e:/Bmad-method/frontend`
- **Native**: `e:/Bmad-method/backend/src/main/cpp`
- **Build Tool**: Gradle 8.x+

### References

- [Architecture Design](file:///e:/Bmad-method/_bmad-output/planning-artifacts/architecture.md)
- [PRD Executive Summary](file:///e:/Bmad-method/_bmad-output/planning-artifacts/prd.md#Executive Summary)
- [Epic 1 Breakdown](file:///e:/Bmad-method/_bmad-output/planning-artifacts/epics.md#Epic 1: 基础建设与多格式知识摄取 (Foundation & Multi-Format Ingestion))

## Dev Agent Record

### Agent Model Used

Gemini-3-Flash-Preview

### Debug Log References

### Completion Notes List

### File List
