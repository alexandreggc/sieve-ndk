package com.example.mytestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mytestapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.sqrt
import kotlin.time.measureTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonExec.setOnClickListener {
            val iterations = 5
            val warmupIterations = 5

            // default
            val timesKotlin = mutableListOf<Long>()
            val timesCpp = mutableListOf<Long>()
            val timesC = mutableListOf<Long>()

            // results
            val timesResultsKotlin = mutableListOf<Long>()
            val timesResultsCpp = mutableListOf<Long>()
            val timesResultsC = mutableListOf<Long>()

            // parallel
            val timesParallelKotlin = mutableListOf<Long>()
            val timesParallelCpp = mutableListOf<Long>()
            val timesParallelC = mutableListOf<Long>()

            // results + parallel
            val timesResultsParallelCpp = mutableListOf<Long>()
            val timesResultsParallelC = mutableListOf<Long>()

            startSievePerformanceTests(warmupIterations, iterations,
                timesKotlin, timesCpp, timesC,
                timesResultsKotlin, timesResultsCpp, timesResultsC,
                timesParallelKotlin, timesParallelCpp, timesParallelC,
                timesResultsParallelCpp, timesResultsParallelC)

            val avgDefaultKotlin = timesKotlin.average()
            val avgDefaultCpp = timesCpp.average()
            val avgDefaultC = timesC.average()

            val avgResultsKotlin = timesResultsKotlin.average()
            val avgResultsCpp = timesResultsCpp.average()
            val avgResultsC = timesResultsC.average()

            val averageParallelKotlin = timesParallelKotlin.average()
            val averageParallelCpp = timesParallelCpp.average()
            val averageParallelC = timesParallelC.average()

            val avgResultsParallelCpp = timesResultsParallelCpp.average()
            val avgResultsParallelC = timesResultsParallelC.average()

            println("(default) kotlin avg time (ms): $avgDefaultKotlin")
            println("(default) C++ avg time (ms): $avgDefaultCpp")
            println("(default) C avg time (ms): $avgDefaultC")

            println("(results) kotlin avg time (ms): $avgResultsKotlin")
            println("(results) C++ avg time (ms): $avgResultsCpp")
            println("(results) C avg time (ms): $avgResultsC")

            println("(parallel) kotlin avg time (ms): $averageParallelKotlin")
            println("(parallel) C++ avg time (ms): $averageParallelCpp")
            println("(parallel) C avg time (ms): $averageParallelC")

            println("(results + parallel) C++ avg time (ms): $avgResultsParallelCpp")
            println("(results + parallel) C avg time (ms): $avgResultsParallelC")

            // default
            binding.defaultKotlinTime.text = "$avgDefaultKotlin"
            binding.defaultCppTime.text = "$avgDefaultCpp"
            binding.defaultCTime.text = "$avgDefaultC"

            // results
            binding.resultsKotlinTime.text = "$avgResultsKotlin"
            binding.resultsCppTime.text = "$avgResultsCpp"
            binding.resultsCTime.text = "$avgResultsC"

            //parallel
            binding.parallelKotlinTime.text = "$averageParallelKotlin"
            binding.parallelCppTime.text = "$averageParallelCpp"
            binding.parallelCTime.text = "$averageParallelC"

            // results + parallel
            binding.resultsParallelCppTime.text = "$avgResultsParallelCpp"
            binding.resultsParallelCTime.text = "$avgResultsParallelC"
        }
    }


    private fun startSievePerformanceTests(warmupIterations:Int, iterations:Int,
                                           timesKotlin:MutableList<Long>,
                                           timesCpp:MutableList<Long>,
                                           timesC:MutableList<Long>,
                                           timesResultsKotlin:MutableList<Long>,
                                           timesResultsCpp: MutableList<Long>,
                                           timesResultsC: MutableList<Long>,
                                           timesParallelKotlin: MutableList<Long>,
                                           timesParallelCpp:MutableList<Long>,
                                           timesParallelC:MutableList<Long>,
                                           timesResultsParallelCpp:MutableList<Long>,
                                           timesResultsParallelC:MutableList<Long>) {

        val primeN = binding.nPrimes.text.toString().toInt()
        var primes: ArrayList<Int>
        var primesCount: Int

        repeat(warmupIterations){
            // default
            sieveKotlin(primeN)
            sieveCpp(primeN)
            sieveC(primeN)

            // results
            /*sieveResultsKotlin(primeN)
            sieveResultsCpp(primeN)
            sieveResultsC(primeN)*/

            // parallel
            sieveParallelKotlin(primeN)
            sieveParallelCpp(primeN)
            sieveParallelC(primeN)

            // results + parallel
            /*sieveResultsParallelCpp(primeN)
            sieveResultsParallelC(primeN)*/
        }

        // default tests: kotlin, cpp, c
        println("Testing (default) ...")
        repeat(iterations) {
            System.gc()
            val time1 = measureTime {
                primesCount = sieveKotlin(primeN)
            }
            println("Found $primesCount primes")
            timesKotlin += time1.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time2 = measureTime {
                primesCount = sieveCpp(primeN)
            }
            println("Found $primesCount primes")
            timesCpp += time2.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time3 = measureTime {
                primesCount = sieveC(primeN)
            }
            println("Found $primesCount primes")
            timesC += time3.inWholeMilliseconds
        }

        // results tests: kotlin, cpp, c
        /*println("Testing (results) ...")
        repeat(iterations) {
            System.gc()
            val time6 = measureTime {
                primes = sieveResultsKotlin(primeN)
            }
            println("Found ${primes.count()} primes")
            timesResultsKotlin += time6.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time7 = measureTime {
                primes = sieveResultsCpp(primeN)
            }
            println("Found ${primes.count()} primes")
            timesResultsCpp += time7.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time8 = measureTime {
                primes = sieveResultsC(primeN)
            }
            println("Found ${primes.count()} primes")
            timesResultsC += time8.inWholeMilliseconds
        }
        */

        // parallel tests: kotlin, cpp, c
        println("Testing (parallel) ...")
        repeat(iterations) {
            System.gc()
            val time11 = measureTime {
                primesCount = sieveParallelKotlin(primeN)
            }
            println("Kotlin: Found $primesCount primes")
            timesParallelKotlin += time11.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time4 = measureTime {
                primesCount  = sieveParallelCpp(primeN)
            }
            println("C++: Found $primesCount primes")
            timesParallelCpp += time4.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time5 = measureTime {
                primesCount = sieveParallelC(primeN)
            }
            println("C: Found $primesCount primes")
            timesParallelC += time5.inWholeMilliseconds
        }

        // results + parallel: cpp, c
        /*println("Testing (results + parallel) ...")
        repeat(iterations) {
            System.gc()
            val time9 = measureTime {
                primes = sieveResultsParallelCpp(primeN)
            }
            println("Found ${primes.count()} primes")
            timesResultsParallelCpp += time9.inWholeMilliseconds
        }
        repeat(iterations) {
            System.gc()
            val time10 = measureTime {
                primes = sieveResultsParallelC(primeN)
            }
            println("Found ${primes.count()} primes")
            timesResultsParallelC += time10.inWholeMilliseconds
        }*/
    }


    private external fun sieveCpp(n: Int): Int

    private external fun sieveResultsCpp(n: Int): ArrayList<Int>

    private external fun sieveParallelCpp(n: Int): Int

    private external fun sieveResultsParallelCpp(n: Int): ArrayList<Int>

    private external fun sieveC(n: Int): Int

    private external fun sieveResultsC(n: Int): ArrayList<Int>

    private external fun sieveParallelC(n: Int): Int

    private external fun sieveResultsParallelC(n: Int): ArrayList<Int>

    private fun sieveKotlin(n: Int): Int {
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

        return primesCount
    }

    private fun sieveResultsKotlin(n: Int): ArrayList<Int> {
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

        return primes
    }

    private fun sieveParallelKotlin(n: Int): Int = runBlocking{
        val isPrime = BooleanArray(n + 1) { true }

        if (n >= 0) isPrime[0] = false
        if (n >= 1) isPrime[1] = false

        val limit = sqrt(n.toDouble()).toInt()

        val jobs = (2..limit).map { i ->
            launch {
                if (isPrime[i]) {
                    for (j in i * i..n step i) {
                        isPrime[j] = false
                    }
                }
            }
        }

        jobs.forEach { it.join() }

        val primesCount = isPrime.count { it }

        return@runBlocking primesCount
    }

    companion object {
        // Used to load the 'mytestapp' library on application startup.
        init {
            System.loadLibrary("mytestapp")
        }
    }
}