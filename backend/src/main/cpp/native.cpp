/**
 * @file bmad_native.cpp
 * @brief BMAD 模型操作的 JNI (Java Native Interface) 实现。
 * 
 * 此源文件实现了 com.bmad.nativeapi.LlamaNative 类中声明的原生方法。
 * 它负责处理高性能任务，如模型内存管理、直接硬件交互等，这些任务更适合使用 C++ 实现。
 */

#include <jni.h>
#include <cstdlib>
#include <iostream>
#include <string>
#include <cmath>
#include <functional>

/**
 * @brief 全局指针，用于跟踪模拟的模型内存地址。
 * 
 * 在生产环境（例如集成 llama.cpp）中，这将指向一个结构化的上下文对象。
 * 我们在这里使用全局指针来演示手动堆外内存管理，并防止 JNI 调用之间的内存泄漏。
 */
static void* g_model_ptr = nullptr;

extern "C" {

/**
 * LlamaNative.handshake() 的实现
 * 
 * 用于验证原生库是否已正确加载以及 JNI 桥接是否功能正常。
 * 
 * @param env 指向 JNI 环境的指针。
 * @param clazz 调用此方法的 Java 类对象。
 * @return 返回确认连接状态的 jstring。
 */
JNIEXPORT jstring JNICALL
Java_com_bmad_nativeapi_LlamaNative_handshake(JNIEnv *env, jclass clazz) {
    // 抑制未使用的 'clazz' 参数导致的编译器警告
    (void)clazz;
    
    std::string message = " Native Handshake: Connection Secure!";
    return env->NewStringUTF(message.c_str());
}

/**
 * LlamaNative.loadModel(String path) 的实现
 * 
 * 模拟加载 GGUF 或其他二进制模型文件的过程。
 * 1. 安全地将 Java 字符串转换为原生 C 字符串。
 * 2. 实现“重复加载保护”：如果已经加载了模型，则先释放现有内存。
 * 3. 分配 1MB 的堆外内存来模拟模型驻留。
 * 4. 确保在返回前释放 JNI 字符串资源。
 * 
 * @param env 指向 JNI 环境的指针。
 * @param clazz Java 类对象。
 * @param path 模型文件的文件系统路径。
 * @return 模拟加载成功返回 1，否则返回 0。
 */
JNIEXPORT jint JNICALL
Java_com_bmad_nativeapi_LlamaNative_loadModel(JNIEnv *env, jclass clazz, jstring path) {
    (void)clazz;
    
    // 将 jstring 转换为原生 const char*
    const char *model_path = env->GetStringUTFChars(path, nullptr);
    if (model_path == nullptr) return 0;

    std::cout << "[Native] 信息: 尝试从以下路径加载模型: " << model_path << std::endl;
    
    // 重复加载保护：如果存在之前的分配，则先清理
    if (g_model_ptr != nullptr) {
        std::cout << "[Native] 信息: 在重新加载前释放现有的模型内存。" << std::endl;
        std::free(g_model_ptr);
        g_model_ptr = nullptr;
    }
    
    // 为模型模拟堆外内存分配 (1MB)
    g_model_ptr = std::malloc(1024 * 1024);
    if (g_model_ptr != nullptr) {
        std::cout << "[Native] 成功: 模型已加载到模拟内存地址: " << g_model_ptr << std::endl;
    } else {
        std::cerr << "[Native] 错误: 模型内存分配失败。" << std::endl;
    }

    // 重要：将用于路径字符串的内存释放回 JVM
    env->ReleaseStringUTFChars(path, model_path);
    
    return g_model_ptr != nullptr ? 1 : 0;
}

/**
 * LlamaNative.freeModel() 的实现
 * 
 * 安全地释放 g_model_ptr 占用的堆外内存。
 * 即使多次调用也是安全的。
 */
JNIEXPORT void JNICALL
Java_com_bmad_nativeapi_LlamaNative_freeModel(JNIEnv *env, jclass clazz) {
    (void)env;
    (void)clazz;
    
    if (g_model_ptr != nullptr) {
        std::cout << "[Native] 信息: 正在显式释放模型内存: " << g_model_ptr << std::endl;
        std::free(g_model_ptr);
        g_model_ptr = nullptr;
    }
}

/**
 * LlamaNative.getEmbedding(String text) 的实现
 * 
 * 模拟将文本转化为语义向量的过程。
 * 现实场景中，这会调用 llama_cpp 的向量化 API。
 * 我们在这里根据输入文本的哈希值生成一个伪随机的 128 维向量（Normalized）。
 */
JNIEXPORT jfloatArray JNICALL
Java_com_bmad_nativeapi_LlamaNative_getEmbedding(JNIEnv *env, jclass clazz, jstring text) {
    (void)clazz;
    
    const char *input = env->GetStringUTFChars(text, nullptr);
    if (input == nullptr) return nullptr;

    // 向量维度：我们暂定 128 维
    const int dim = 128;
    jfloatArray result = env->NewFloatArray(dim);
    if (result == nullptr) return nullptr;

    float vector[dim];
    std::string s(input);
    std::size_t hash = std::hash<std::string>{}(s);

    // 基于哈希值生成伪随机向量并归一化（模拟语义空间的分布）
    float sum_sq = 0.0f;
    for (int i = 0; i < dim; i++) {
        vector[i] = (float)((hash + i * 13) % 1000) / 1000.0f;
        sum_sq += vector[i] * vector[i];
    }
    
    float norm = std::sqrt(sum_sq);
    for (int i = 0; i < dim; i++) {
        vector[i] /= norm;
    }

    env->SetFloatArrayRegion(result, 0, dim, vector);
    env->ReleaseStringUTFChars(text, input);

    return result;
}

}
