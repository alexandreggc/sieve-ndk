#include <jni.h>
#include <unistd.h>
#include <omp.h>
#include <math.h>

#include "macros.h"
#include "bitter.h"


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_SieveCKt_sieveC(
        JNIEnv *env,
        jclass clazz,
        jint n ){

    char* isPrime = (char*)malloc((n + 1) * sizeof(char));
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
    for (int i = 2; i <= n; i++) {
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


#define index(i) (i/2)
#define odd(i) (i%2)

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_SieveCKt_sieveEvenRemovedC(JNIEnv *env, jclass clazz, jint n) {
    unsigned int nEven = n / 2;

    char* isPrime = (char*)malloc((nEven + 1) * sizeof(char));
    if (isPrime == NULL) return NULL;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;

    int limit = (int)sqrt((double)n);

    for (int value = 1; value <= limit; value+=2) {
        if (isPrime[index(value)]) {
            for (int mulValue = value * value; mulValue <= n; mulValue += value) {
                if (odd(mulValue))
                    isPrime[index(mulValue)] = false;
            }
        }
    }

    int primeCount = 0;
    for (int i = 1; i < nEven+1; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    free(isPrime);

    return primeCount;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_SieveCKt_sieveEvenRemovedParallelC(JNIEnv *env, jclass clazz, jint n) {
    unsigned int nEven = n / 2;

    char* isPrime = (char*)malloc((nEven + 1) * sizeof(char));
    if (isPrime == NULL) return NULL;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;

    int limit = (int)sqrt((double)n);

    #pragma omp parallel for schedule(dynamic)
    for (int value = 1; value <= limit; value+=2) {
        if (isPrime[index(value)]) {
            for (int mulValue = value * value; mulValue <= n; mulValue += value) {
                if (odd(mulValue))
                    isPrime[index(mulValue)] = false;
            }
        }
    }

    int primeCount = 0;
    for (int i = 1; i < nEven+1; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    free(isPrime);

    return primeCount;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_SieveCKt_sieveBitArrayC(JNIEnv *env, jclass clazz, jint n) {

    bitter* b = create_bitter(n / 2 + 1);

    if (b == NULL) {
        return NULL;
    }

    fprintf(stderr,
            "Using %lld bytes to store %lld bits (%lld bits unused).\n",
            b->effectiveN, b->origN, b->effectiveN * 8 - b->origN);

    fprintf(stderr, "Setting all bits to one... ");

    fill(b, 1);

    fprintf(stderr, "done.\n");

    unsigned long sqrtn = sqrt(n) + 1;

    //#pragma omp parallel for
    for (unsigned long long i = 3; i <= sqrtn; i += 2) {
        // printf("[%d] Found %lld to be prime. %lld is a seed: %d.\n",
        // omp_get_thread_num(), i, i, i < sqrt(n));
        if (getbit(b, i / 2)) {
#pragma omp parallel for
            for (unsigned long long j = i * i; j <= n; j += 2 * i) {
                setbit(b, j / 2, 0);
                // printf("[%d] marking %lld as non prime.\n",
                // omp_get_thread_num(), j);
            }
        }
    }

    int primeCount = 0;
    for (unsigned long long i = 1; i <= n; i += 2) {
        if (getbit(b, i / 2)) {
            primeCount++;
        }
    }

    return primeCount;
}