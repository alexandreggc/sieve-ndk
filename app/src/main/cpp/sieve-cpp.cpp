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

#include "macros.h"

// C++ implementations: default, results, parallel, results + parallel

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_SieveCppKt_sieveCpp(
        JNIEnv *env,
        jclass clazz,
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
Java_com_example_mytestapp_SieveCppKt_sieveResultsCpp(
        JNIEnv *env,
        jclass clazz,
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
Java_com_example_mytestapp_SieveCppKt_sieveParallelCpp(
        JNIEnv *env,
        jclass clazz,
        jint n) {

    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));

    /*LOGI("Num Procs: %d", omp_get_num_procs());
    LOGI("Num Threads: %d", omp_get_num_threads());*/

    std::mutex mtx;

#pragma omp parallel for schedule(dynamic)
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
Java_com_example_mytestapp_SieveCppKt_sieveResultsParallelCpp(
        JNIEnv *env,
        jclass clazz,
        jint n) {

    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));

#pragma omp parallel for schedule(dynamic)
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
