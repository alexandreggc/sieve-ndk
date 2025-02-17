#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveOfEratosthenesNDK(
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

    /*
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
    */

    auto primesCount = std::count(isPrime.begin(), isPrime.end(), true);

    return primesCount;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveOfEratosthenesNDKC(
        JNIEnv *env,
        jobject thiz,
        jint n ){

    int primeCount = 0;
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

    /*
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
    */

    free(isPrime);

    return primesCount;
}