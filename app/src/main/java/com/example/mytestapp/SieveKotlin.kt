package com.example.mytestapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val numThreads = Runtime.getRuntime().availableProcessors()
    val chunkSize = (limit - 2 + 1) / numThreads

    val jobs = (0 until numThreads).map { threadIndex ->
        val start = 2 + threadIndex * chunkSize
        val end = if (threadIndex == numThreads - 1) limit else start + chunkSize - 1

        launch(Dispatchers.Default) {
            for (i in start..end) {
                if (isPrime[i]) {
                    for (j in i * i..n step i) {
                        isPrime[j] = false
                    }
                }
            }
        }
    }

    jobs.forEach { it.join() }

    val primesCount = isPrime.count { it }

    return@withContext primesCount
}