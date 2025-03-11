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


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_SieveCKt_sieveC(
        JNIEnv *env,
        jclass clazz,
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
Java_com_example_mytestapp_SieveCKt_sieveResultsC(
        JNIEnv *env,
        jclass clazz,
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
Java_com_example_mytestapp_SieveCKt_sieveParallelC(
        JNIEnv *env,
        jclass clazz,
        jint n) {
    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);

#pragma omp parallel for schedule(dynamic)
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
Java_com_example_mytestapp_SieveCKt_sieveResultsParallelC(
        JNIEnv *env,
        jclass clazz,
        jint n) {

    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);

#pragma omp parallel for schedule(dynamic)
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

