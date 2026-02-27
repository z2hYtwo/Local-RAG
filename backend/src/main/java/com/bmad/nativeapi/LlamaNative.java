package com.bmad.nativeapi;

/**
 * JNI 桥接类 (Java Native Interface Bridge)
 * 
 * 职责：作为 Java 应用层与 C++ 原生代码层之间的通信枢纽。
 * 它声明了本地方法 (native methods)，这些方法的具体实现位于 buildNative 任务生成的 .dll/.so 动态库中。
 */
public class LlamaNative {
    static {
        /**
         * 静态加载本地库。
         * 1. 查找路径：JVM 会根据 java.library.path 系统属性查找动态库。
         * 2. 库名规范：传递 "bmad_native"，系统会自动补全为 bmad_native.dll (Windows) 
         *    或 libbmad_native.so (Linux/macOS).
         */
        System.loadLibrary("bmad_native");
    }

    /**
     * 连通性测试：执行简单的握手协议。
     * 用于在应用启动阶段验证 JNI 链路是否通畅，以及动态库是否正确导出符号。
     * 
     * @return 本地层返回的状态字符串（例如：" Native Handshake: Connection Secure!"）
     */
    public static native String handshake();

    /**
     * 模型加载：将大型语言模型加载至内存。
     * 这是一个阻塞操作，本地层会根据路径读取文件内容并分配堆外内存。
     * 
     * @param path 模型文件的全路径或相对路径。
     * @return 成功状态码：1 表示成功，0 表示失败（如路径错误或内存不足）。
     */
    public static native int loadModel(String path);

    /**
     * 资源释放：清理本地内存占用。
     * 必须显式调用！因为 Java GC 无法管理由 malloc/new 分配的堆外内存。
     * 忘记调用会导致应用的 Native 内存占用持续攀升（内存泄漏）。
     */
    public static native void freeModel();

    /**
     * 向量化 (Embedding)：将文本转换为数值向量。
     * 
     * @param text 输入文本片段。
     * @return float 数组，代表该文本的语义向量（通常为 384, 768 或 1024 维）。
     */
    public static native float[] getEmbedding(String text);
}
