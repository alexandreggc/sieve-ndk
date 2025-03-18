#include <jni.h>
#include <unistd.h>
#include <omp.h>
#include <math.h>

#include "macros.h"
#include "bitter.h"


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCKt_sieveC(
        JNIEnv *env,
        jclass clazz,
        jlong n ){

    char* isPrime = (char*)malloc((n + 1) * sizeof(char));
    if (isPrime == NULL) return NULL;

    for (long long i = 0; i <= n; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    unsigned long long limit = sqrt((double)n);

    for (unsigned long long i = 2; i <= limit; i++) {
        if (isPrime[i]) {
            for (unsigned long long j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    long long primesCount = 0;
    for (unsigned long long i = 2; i <= n; i++) {
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
        jlong n) {

    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (isPrime == 0) return 0;

    for (unsigned long long i = 0; i <= n; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    unsigned long long limit = sqrt((double)n);

    for (unsigned long long i = 2; i <= limit; i++) {
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

    for (long long i=2; i<n; i++) {
        if (isPrime[i]) {
            jobject primeLongObj = env->NewObject(longClass, longConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeLongObj);

            env->DeleteLocalRef(primeLongObj);
        }
    }

    free(isPrime);

    return arrayListObj;
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCKt_sieveParallelC(
        JNIEnv *env,
        jclass clazz,
        jlong n) {
    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (unsigned long long i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    unsigned long long limit = sqrt((double)n);

#pragma omp parallel for schedule(dynamic)
    for (unsigned long long i = 2; i <= limit; i++) {
        if (isPrime[i]) {
            for (unsigned long long j = i * i; j <= n; j += i) {
                isPrime[j] = false;
            }
        }
    }

    long long primeCount = 0;
#pragma omp parallel for reduction(+:primeCount)
    for (unsigned long long i = 2; i <= n; i++) {
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
        jlong n) {

    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (unsigned long long i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    unsigned long long limit = sqrt((double)n);

#pragma omp parallel for schedule(dynamic)
    for (unsigned long long i = 2; i <= limit; i++) {
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

    for (long long i=2; i<n; i++) {
        if (isPrime[i]) {
            jobject primeLongObj = env->NewObject(longClass, longConstructor, i);

            env->CallBooleanMethod(arrayListObj, addMethod, primeLongObj);

            env->DeleteLocalRef(primeLongObj);
        }
    }

    free(isPrime);

    return arrayListObj;
}


#define index(i) (i/2)
#define odd(i) (i%2)

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCKt_sieveEvenRemovedC(JNIEnv *env, jclass clazz, jlong n) {
    unsigned long long nEven = n / 2;

    char* isPrime = (char*)malloc((nEven + 1) * sizeof(char));
    if (isPrime == NULL) return NULL;

    for (unsigned long long i = 0; i <= nEven; i++) {
        isPrime[i] = true;
    }

    if (n >= 0) isPrime[0] = false;

    unsigned long long limit = sqrt((double)n);

    for (unsigned long long value = 1; value <= limit; value+=2) {
        if (isPrime[index(value)]) {
            for (unsigned long long mulValue = value * value; mulValue <= n; mulValue += value) {
                if (odd(mulValue))
                    isPrime[index(mulValue)] = false;
            }
        }
    }

    long long primeCount = 0;
    for (unsigned long long i = 1; i < nEven+1; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    free(isPrime);

    return primeCount;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCKt_sieveEvenRemovedParallelC(JNIEnv *env, jclass clazz, jlong n) {
    unsigned long long nEven = n / 2;

    char* isPrime = (char*)malloc((nEven + 1) * sizeof(char));
    if (isPrime == NULL) return NULL;

    for (unsigned long long i = 0; i <= nEven; i++) {
        isPrime[i] = 1;
    }

    if (n >= 0) isPrime[0] = 0;

    unsigned long limit = sqrt((double)n);

    #pragma omp parallel for schedule(dynamic)
    for (unsigned long long value = 1; value <= limit; value+=2) {
        if (isPrime[value / 2]) {
            for (unsigned long long mulValue = value * value; mulValue <= n; mulValue += value) {
                if (mulValue % 2)
                    isPrime[mulValue / 2] = 0;
            }
        }
    }

    long long primeCount = 0;
    for (unsigned long long i = 1; i <= nEven; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    free(isPrime);

    return primeCount;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_mytestapp_SieveCKt_sieveBitArrayC(JNIEnv *env, jclass clazz, jlong n) {

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

#pragma omp parallel for schedule(dynamic)
    for (unsigned long long i = 3; i <= sqrtn; i += 2) {
        // printf("[%d] Found %lld to be prime. %lld is a seed: %d.\n",
        // omp_get_thread_num(), i, i, i < sqrt(n));
        if (getbit(b, i / 2)) {
//#pragma omp parallel for
            for (unsigned long long j = i * i; j <= n; j += 2 * i) {
                setbit(b, j / 2, 0);
                // printf("[%d] marking %lld as non prime.\n",
                // omp_get_thread_num(), j);
            }
        }
    }

    long long primeCount = 0;
    for (unsigned long long i = 1; i <= n; i += 2) {
        if (getbit(b, i / 2)) {
            primeCount++;
        }
    }
    delete_bitter(b);

    return primeCount;
}