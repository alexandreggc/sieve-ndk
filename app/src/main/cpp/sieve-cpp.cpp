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
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCppKt_sieveCpp(
        JNIEnv *env,
        jclass clazz,
        jlong n ) {

    std::vector<bool> isPrime(n + 1, true);

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    auto limit = static_cast<unsigned long long>(std::sqrt(n));

    for (unsigned long long i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            for (unsigned long long j = i * i; j <= n; j += i) {
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
        jlong n) {
    std::vector<bool> isPrime(n + 1, true);

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    auto limit = static_cast<unsigned long long>(std::sqrt(n));
    for (unsigned long long i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            for (unsigned long long j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListObj = env->NewObject(arrayListClass, arrayListConstructor);

    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass longClass = env->FindClass("java/lang/Long");
    jmethodID longConstructor = env->GetMethodID(longClass, "<init>", "(J)V");

    for (long long i = 2; i <= n; ++i) {
        if (isPrime[i]) {
            jobject primeLongObj = env->NewObject(longClass, longConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeLongObj);

            env->DeleteLocalRef(primeLongObj);
        }
    }

    return arrayListObj;
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCppKt_sieveParallelCpp(
        JNIEnv *env,
        jclass clazz,
        jlong n) {

    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    auto limit = static_cast<unsigned long long>(std::sqrt(n));

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

    return static_cast<long long>(std::count(isPrime.begin(), isPrime.end(), true));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_mytestapp_SieveCppKt_sieveResultsParallelCpp(
        JNIEnv *env,
        jclass clazz,
        jlong n) {

    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    auto limit = static_cast<unsigned long long>(std::sqrt(n));

#pragma omp parallel for schedule(dynamic)
    for (unsigned long long i = 2; i <= limit; ++i) {
        if (isPrime[i]) {
            for (unsigned long long j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    // build the resultant int array with JNI functions
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListObj = env->NewObject(arrayListClass, arrayListConstructor);

    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass longClass = env->FindClass("java/lang/Long");
    jmethodID longConstructor = env->GetMethodID(longClass, "<init>", "(J)V");

    for (long long i = 2; i <= n; i++) {
        if (isPrime[i]) {
            jobject primeLongObj = env->NewObject(longClass, longConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeLongObj);

            env->DeleteLocalRef(primeLongObj);
        }
    }

    return arrayListObj;
}
