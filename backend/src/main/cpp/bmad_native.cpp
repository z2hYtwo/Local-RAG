#include <jni.h>
#include <string>
#include <vector>

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_bmad_nativeapi_LlamaNative_handshake(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF("Native Handshake: Connection Secure!");
}

JNIEXPORT jint JNICALL
Java_com_bmad_nativeapi_LlamaNative_loadModel(JNIEnv *env, jclass clazz, jstring path) {
    return 1; // Mock success
}

JNIEXPORT void JNICALL
Java_com_bmad_nativeapi_LlamaNative_freeModel(JNIEnv *env, jclass clazz) {
    // Mock free
}

JNIEXPORT jfloatArray JNICALL
Java_com_bmad_nativeapi_LlamaNative_getEmbedding(JNIEnv *env, jclass clazz, jstring text) {
    const char *nativeString = env->GetStringUTFChars(text, 0);
    std::string str(nativeString);
    env->ReleaseStringUTFChars(text, nativeString);

    // Generate a deterministic mock hash-based embedding (384 dimensions)
    int dim = 384;
    std::vector<float> vec(dim, 0.0f);
    size_t hash = std::hash<std::string>{}(str);
    for (int i = 0; i < dim; ++i) {
        vec[i] = static_cast<float>((hash >> (i % 32)) & 1);
    }

    jfloatArray result = env->NewFloatArray(dim);
    env->SetFloatArrayRegion(result, 0, dim, vec.data());
    return result;
}

}
