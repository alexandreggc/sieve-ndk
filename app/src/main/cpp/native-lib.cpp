#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>

#include <thread>
#include <atomic>
#include <stdatomic.h>
#include <pthread.h>
#include <unistd.h>

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

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveOfEratosthenesParallelNDK(JNIEnv *env,
                                            jobject thiz,
                                            jint n) {
    int threadCount = (int) std::thread::hardware_concurrency();
    std::vector<bool> isPrime(n + 1, true);
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = static_cast<int>(std::sqrt(n));
    std::atomic<int> current(2);

    auto worker = [&](int) {
        while (true) {
            int i = current.fetch_add(1);
            if (i > limit) break;
            if (isPrime[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isPrime[j] = false;
                }
            }
        }
    };

    std::vector<std::thread> threads;
    threads.reserve(threadCount);
    for (int t = 0; t < threadCount; t++) {
        threads.emplace_back(worker, t);
    }
    for (auto &thr : threads) {
        thr.join();
    }

    return static_cast<int>(std::count(isPrime.begin(), isPrime.end(), true));
}

typedef struct {
    int n;
    int limit;
    bool* isPrime;
    atomic_int* current;
} WorkerArgs;

void* workerFunction(void* arg) {
    WorkerArgs* args = (WorkerArgs*)arg;

    while (true) {
        int i = atomic_fetch_add(args->current, 1);
        if (i > args->limit) break;

        if (args->isPrime[i]) {
            for (int j = i * i; j <= args->n; j += i) {
                args->isPrime[j] = false;
            }
        }
    }
    return NULL;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_mytestapp_MainActivity_sieveOfEratosthenesParallelNDKC(JNIEnv *env,
                                                                       jobject thiz,
                                                                       jint n) {
    int threadCount = (int) sysconf(_SC_NPROCESSORS_ONLN);
    bool* isPrime = (bool*)malloc((n + 1) * sizeof(bool));
    if (!isPrime) return 0;

    for (int i = 0; i <= n; i++) {
        isPrime[i] = true;
    }
    if (n >= 0) isPrime[0] = false;
    if (n >= 1) isPrime[1] = false;

    int limit = (int)sqrt((double)n);
    static atomic_int current = ATOMIC_VAR_INIT(2);

    WorkerArgs args = {n, limit, isPrime, &current};

    pthread_t* threads = (pthread_t*)malloc(threadCount * sizeof(pthread_t));
    if (!threads) {
        free(isPrime);
        return 0;
    }

    for (int t = 0; t < threadCount; t++) {
        pthread_create(&threads[t], NULL, workerFunction, &args);
    }
    for (int t = 0; t < threadCount; t++) {
        pthread_join(threads[t], NULL);
    }

    int primeCount = 0;
    for (int i = 2; i <= n; i++) {
        if (isPrime[i]) {
            primeCount++;
        }
    }

    free(threads);
    free(isPrime);

    return primeCount;
}
