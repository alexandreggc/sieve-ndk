#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>
#include <thread>
#include <atomic>
#include <mutex>

#include <unistd.h>
#include <omp.h>
#include <android/log.h>

#define LOG_TAG "NativeSieve"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)


// C++ implementations: default, results, parallel, results + parallel

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveCpp(
        JNIEnv *env,
        jobject thiz,
        jint n ) {

    std::vector<bool> isPrime(n + 1, true);

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));
    for (int i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    auto primesCount = std::count(isPrime.begin(), isPrime.end(), true);

    return primesCount;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_mytestapp_MainActivity_sieveResultsCpp(
        JNIEnv *env,
        jobject thiz,
        jint n) {
    std::vector<bool> isPrime(n + 1, true);

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));
    for (int i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListObj = env->NewObject(arrayListClass, arrayListConstructor);

    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID integerConstructor = env->GetMethodID(integerClass, "<init>", "(I)V");

    for (int i = 2; i <= n; ++i) {
        if (isPrime[i]) {
            jobject primeIntegerObj = env->NewObject(integerClass, integerConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeIntegerObj);

            env->DeleteLocalRef(primeIntegerObj);
        }
    }

    return arrayListObj;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveParallelCpp(
        JNIEnv *env,
        jobject thiz,
        jint n) {

    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));

    /*LOGI("Num Procs: %d", omp_get_num_procs());
    LOGI("Num Threads: %d", omp_get_num_threads());*/

    std::mutex mtx;

    #pragma omp parallel for
    for (int i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            mtx.lock();
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
            mtx.unlock();
        }
    }

    return static_cast<int>(std::count(isPrime.begin(), isPrime.end(), true));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_mytestapp_MainActivity_sieveResultsParallelCpp(
        JNIEnv *env,
        jobject thiz,
        jint n) {

    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));

#pragma omp parallel for
    for (int i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    // build the resultant int array with JNI functions
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListObj = env->NewObject(arrayListClass, arrayListConstructor);

    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID integerConstructor = env->GetMethodID(integerClass, "<init>", "(I)V");

    for (int i = 2; i <= n; i++) {
        if (isPrime[i]) {
            jobject primeIntegerObj = env->NewObject(integerClass, integerConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeIntegerObj);

            env->DeleteLocalRef(primeIntegerObj);
        }
    }

    return arrayListObj;
}


// C implementations: default, results, parallel, results + parallel

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveC(
        JNIEnv *env,
        jobject thiz,
        jint n ){

    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (isPrime == NULL) return NULL;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);

    for (int i = 2; i <= limit; i++) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    int primesCount = 0;
    for (int i = 2; i < n; i++) {
        if (isPrime[i]) {
            primesCount++;
        }
    }

    free(isPrime);

    return primesCount;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_mytestapp_MainActivity_sieveResultsC(
        JNIEnv *env,
        jobject thiz,
        jint n) {

    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (isPrime == NULL) return NULL;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);

    for (int i = 2; i <= limit; i++) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListObj = env->NewObject(arrayListClass, arrayListConstructor);

    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID integerConstructor = env->GetMethodID(integerClass, "<init>", "(I)V");

    for (int i=2; i<n; i++) {
        if (isPrime[i]) {
            jobject primeIntegerObj = env->NewObject(integerClass, integerConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeIntegerObj);

            env->DeleteLocalRef(primeIntegerObj);
        }
    }

    free(isPrime);

    return arrayListObj;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveParallelC(
        JNIEnv *env,
        jobject thiz,
        jint n) {
    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);

    #pragma omp parallel for
    for (int i = 2; i <= limit; i++) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    int primeCount = 0;
    #pragma omp parallel for reduction(+:primeCount)
    for (int i = 2; i <= n; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    free(isPrime);

    return primeCount;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_mytestapp_MainActivity_sieveResultsParallelC(
        JNIEnv *env,
        jobject thiz,
        jint n) {

    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);

    #pragma omp parallel for
    for (int i = 2; i <= limit; i++) {
        if (isPrime[i]) {
            for (int j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    int primeCount = 0;
    for (int i = 2; i <= n; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListObj = env->NewObject(arrayListClass, arrayListConstructor);

    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID integerConstructor = env->GetMethodID(integerClass, "<init>", "(I)V");

    for (int i=2; i<n; i++) {
        if (isPrime[i]) {
            jobject primeIntegerObj = env->NewObject(integerClass, integerConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeIntegerObj);

            env->DeleteLocalRef(primeIntegerObj);
        }
    }

    free(isPrime);

    return arrayListObj;
}

