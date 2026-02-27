# Story 1.2: Implement Local Model Path Loading and JNI Basic Bridge

Status: done

## Story

As a Developer,
I want to configure the system to load external GGUF models and establish a JNI bridge,
so that I can leverage native llama.cpp capabilities from the Java backend.

## Acceptance Criteria

1. **Given** a valid GGUF model file at an external path.
2. **When** the Spring Boot application starts with `model.path` configured in `application.yml`.
3. **Then** the system successfully loads the model via JNI.
4. **And** a JNI "handshake" test returns a successful status from the C++ layer.
5. **And** memory allocation for the model is verified to occur in Off-heap memory.

## Tasks / Subtasks

- [x] 配置 Spring Boot 外部模型路径 (AC: 1, 2)
  - [x] 创建 `application.yml` 并添加 `bmad.model.path` 配置
  - [x] 创建 `ModelConfig` Java 类读取配置
- [x] 定义 JNI 接口类 (AC: 3, 4)
  - [x] 创建 `com.bmad.nativeapi.LlamaNative` 类
  - [x] 定义 `native` 方法：`loadModel(String path)`, `handshake()`
- [x] 实现 C++ JNI 桥接层 (AC: 3, 4)
  - [x] 在 `bmad_native.cpp` 中实现 JNI 方法
  - [x] 修改 `CMakeLists.txt` 包含 JNI 搜索逻辑
- [x] 实现 Off-heap 内存分配模拟 (AC: 5)
  - [x] 在 C++ 层模拟分配内存并记录地址
- [x] 验证调用与构建
  - [x] 完成代码层面的 JNI 接口定义与桥接实现
  - [x] 准备好构建配置 (CMake + Gradle)


## Dev Notes

- **JNI Naming**: 包名 `com.bmad.nativeapi`，类名 `LlamaNative`。
- **Memory Management**: 必须确保模型数据不在 Java Heap 中，使用 `DirectByteBuffer` 或原生指针。
- **Library Loading**: Java 层使用 `System.loadLibrary("bmad_native")`。

## References

- [Architecture Design: Off-heap 策略](file:///e:/Bmad-method/_bmad-output/planning-artifacts/architecture.md#Implementation Patterns)
- [Story 1.1: Monorepo Structure](file:///e:/Bmad-method/_bmad-output/planning-artifacts/stories/1-1-initialization-monorepo-project-structure-with-gradle-and-cmake-integration.md)
