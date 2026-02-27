cmake_minimum_required(VERSION 3.10)
project(bmad_native)

# C++17 标准用于 JNI/本地桥接实现
set(CMAKE_CXX_STANDARD 17)

# 强制 MSVC 使用 UTF-8 编码读取源文件，解决中文注释导致的编译错误 (C4819, C2001)
if(MSVC)
    add_compile_options(/utf-8)
endif()

# 手动指定 JAVA_HOME，避免 FindJNI 在 Windows 下定位失败
set(JAVA_HOME "D:/AndroidStudio/.jdks/openjdk-23.0.2")

# JNI 头文件目录（JDK include 与平台子目录）
set(JNI_INCLUDE_DIRS 
    "${JAVA_HOME}/include"
    "${JAVA_HOME}/include/win32"
)

# 构建前校验 JNI 头文件目录是否存在
foreach(dir ${JNI_INCLUDE_DIRS})
    if(NOT EXISTS "${dir}")
        message(FATAL_ERROR "JNI Include directory not found: ${dir}")
    endif()
endforeach()

include_directories(${JNI_INCLUDE_DIRS})

# 生成 JNI 动态库供 Java 层加载
add_library(bmad_native SHARED bmad_native.cpp)

# Windows 下去除 lib 前缀并固定 dll 后缀
set_target_properties(bmad_native PROPERTIES
    PREFIX ""
    SUFFIX ".dll"
)
