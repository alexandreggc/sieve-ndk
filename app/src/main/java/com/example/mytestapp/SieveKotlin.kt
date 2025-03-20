package com.example.mytestapp

import com.example.mytestapp.bitset.NonBlockingConcurrentBitSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.BitSet
import kotlin.math.sqrt

suspend fun sieveKotlin(n: Int): Int = withContext(Dispatchers.Default) {
    val isPrime = BooleanArray(n + 1) { true }

    if (n >= 0) isPrime[0] = false
    if (n >= 1) isPrime[1] = false

    val limit = sqrt(n.toDouble()).toInt()

    for (i in 2..limit) {
        if (isPrime[i]) {
            for (j in i * i..n step i) {
                isPrime[j] = false
            }
        }
    }

    val primesCount = isPrime.count { it }

    return@withContext primesCount
}

suspend fun sieveResultsKotlin(n: Int): ArrayList<Int> = withContext(Dispatchers.Default) {
    val isPrime = BooleanArray(n + 1) { true }

    if (n >= 0) isPrime[0] = false
    if (n >= 1) isPrime[1] = false

    val limit = sqrt(n.toDouble()).toInt()

    for (i in 2..limit) {
        if (isPrime[i]) {
            for (j in i * i..n step i) {
                isPrime[j] = false
            }
        }
    }

    val primes = ArrayList<Int>()
    for (i in 2..n) {
        if (isPrime[i]) {
            primes.add(i)
        }
    }

    return@withContext primes
}

suspend fun sieveParallelKotlin(n: Int): Int = withContext(Dispatchers.Default) {
    val isPrime = BooleanArray(n + 1) { true }

    if (n >= 0) isPrime[0] = false
    if (n >= 1) isPrime[1] = false

    val limit = sqrt(n.toDouble()).toInt()
//    val numThreads = Runtime.getRuntime().availableProcessors()
//    val chunkSize = (limit - 2 + 1) / numThreads

//    val jobs = (0 until numThreads).map { threadIndex ->
//        val start = 2 + threadIndex * chunkSize
//        val end = if (threadIndex == numThreads - 1) limit else start + chunkSize - 1
//
//        launch(Dispatchers.Default) {
//            for (i in start..end) {
//                if (isPrime[i]) {
//                    for (j in i * i..n step i) {
//                        isPrime[j] = false
//                    }
//                }
//            }
//        }
//    }


    val jobs = (2..limit).map { i ->
        launch(Dispatchers.Default) {
            if (isPrime[i]) {
                for (j in i * i..n step i) {
                    isPrime[j] = false
                }
            }
        }
    }

    jobs.forEach { it.join() }


    val primesCount = isPrime.count { it }

    return@withContext primesCount
}

suspend fun sieveEvenRemovedKotlin(n: Int): Int = withContext(Dispatchers.Default){
    val nEven = n / 2
    val isPrime = BooleanArray(nEven + 1) { true }

    if (n >= 0) isPrime[0] = false

    val limit = sqrt(n.toDouble()).toInt()

    for (value in 1..limit step 2) {
        if (isPrime[value / 2]) {
            for (mulValue in value * value..n step value) {
                if (mulValue % 2 != 0) {
                    isPrime[mulValue / 2] = false
                }
            }
        }
    }

    val primesCount = isPrime.count { it }

    return@withContext primesCount
}

suspend fun sieveEvenRemovedParallelKotlin(n: Int): Int = withContext(Dispatchers.Default) {
    val nEven = n / 2
    val isPrime = BooleanArray(nEven + 1) { true }

    if (n >= 0) isPrime[0] = false

    val limit = sqrt(n.toDouble()).toInt()

    val jobs = (1..limit step 2).map { value ->
        launch {
            if (isPrime[value / 2]) {
                for (mulValue in value * value..n step value) {
                    if (mulValue % 2 != 0) {
                        isPrime[mulValue / 2] = false
                    }
                }
            }
        }
    }
    jobs.forEach { it.join() }

    val primesCount = isPrime.count { it }

    return@withContext primesCount
}

suspend fun sieveBitArrayKotlin(n: Int): Int = withContext(Dispatchers.Default) {
    val nEven = n / 2

    val isPrime = BitSet(nEven + 1)
//    val isPrime = NonBlockingConcurrentBitSet(nEven + 1)

    for (i in 0..nEven) {
        isPrime.set(i)
    }

    if (n >= 0) isPrime.clear(0)

    val limit = sqrt(n.toDouble()).toInt()
    val numThreads = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
    val rangeSize = ((limit - 1) / 2 + 1) / numThreads

    val mutex = Mutex()
    val jobs = (0 until numThreads).map { threadIndex ->
        val start = 1 + 2 * threadIndex * rangeSize
        val end = if (threadIndex == numThreads - 1) limit else start + 2 * rangeSize - 1

        launch (Dispatchers.Default){
            for (value in start..end step 2) {
                if (isPrime[value / 2]) {
                    for (mulValue in value * value..n step value) {
                        if (mulValue % 2 != 0) {
                            mutex.withLock {
                                isPrime.clear(mulValue / 2)
                            }
//                             isPrime.clear(mulValue / 2)
                        }
                    }
                }
            }
        }
    }
    jobs.forEach { it.join() }


//    var count = 0
//    for (i in 0..nEven) {
//        if (isPrime.get(i)) {
//            count++
//        }
//    }
//    return@withContext count
    return@withContext isPrime.cardinality()
}
