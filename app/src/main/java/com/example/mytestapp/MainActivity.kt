package com.example.mytestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.mytestapp.databinding.ActivityMainBinding
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.pow
import java.util.Locale
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.time.measureTime
import java.io.File

data class SieveAlgorithm(
    val language: String,
    val type: String,
    val function: KFunction<Int>
)

data class ResultRow(
    val language: String,
    val type: String,
    val n: Int,
    val timeMs: Long,
    val primeCount: Int,
    val correct: Boolean
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val iterations = 5
    private val warmupIterations = 5

    private val listN : MutableList<Int> = mutableListOf(
        pow(2.0, 21.0).toInt(), // 2_097_152
//        pow(2.0, 22.0).toInt(), // 4_194_304
//        pow(2.0, 23.0).toInt(), // 8_388_608
//        pow(2.0, 24.0).toInt(), // 16_777_216
//        pow(2.0, 25.0).toInt(), // 33_554_432
//        pow(2.0, 26.0).toInt(), // 67_108_864
//        pow(2.0, 27.0).toInt(), // 134_217_728
    )

    private val algorithms : MutableList<SieveAlgorithm> = mutableListOf(
        SieveAlgorithm("Kotlin", "Default", ::sieveKotlin),
        SieveAlgorithm("Cpp", "Default", ::sieveCpp),
        SieveAlgorithm("C", "Default", ::sieveC),
        SieveAlgorithm("Kotlin", "Parallel", ::sieveParallelKotlin),
        SieveAlgorithm("Cpp", "Parallel", ::sieveParallelCpp),
        SieveAlgorithm("C", "Parallel", ::sieveParallelC),
    )

    private val resultsCSV = mutableListOf<ResultRow>()

    private suspend fun runWarmup() {
        println("\n\nWarmup ...")
        val algoNPair = listN.flatMap { n -> algorithms.map { n to it } }
        println(String.format(Locale.getDefault(), "%-50s | %10s", "Algorithm", "N primes"))
        for ((n, algorithm) in algoNPair) {
            println(String.format(Locale.getDefault(), "%-50s | %10d ", algorithm, n))
            repeat(warmupIterations) {
                algorithm.function.callSuspend(n)
            }
        }
    }

    private suspend fun runTests() {
        println("\n\nTesting ...")
        for (n in listN) {
            val execTimes = mutableListOf<Long>()
            val primeCount1 = sieveParallelC(n)

            for (algorithm in algorithms) {
                println("\n\nTest: ${algorithm.function.name} \tN: $n ")
                println(String.format(Locale.getDefault(), "%-10s | %10s", "Time", "Prime Count"))

                // Run the algorithm multiple times and check if the prime count is correct
                val correct = BooleanArray(iterations) { false }
                execTimes.clear()
                repeat(iterations) {
                    var primesCount = 0
                    val time = measureTime {
                        primesCount = algorithm.function.callSuspend(n)
                    }.inWholeMilliseconds
                    if (primesCount != primeCount1)
                        Log.e("PRIME COUNT", "Incorrect prime count: $primesCount")
                    else
                        correct[it] = true
                    execTimes.add(time)
                    println(String.format(Locale.getDefault(), "%-10d | %10d | %10s", time, primesCount, correct[it]))
                }

                // Calculate average execution time and add to results
                val avgExecTime = execTimes.average().toLong()
                println("Average time: $avgExecTime")
                resultsCSV.add(ResultRow(algorithm.language, algorithm.type, n, avgExecTime, primeCount1, correct.all { it }))
            }
        }

        writeResultsToCSV()
    }

    private fun writeResultsToCSV() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val csvFile = File(dir, "sieve_results.csv")
        csvWriter().open(csvFile) {
            writeRow("Language", "Type", "n", "Time (ms)", "PrimeCount", "Correct")
            resultsCSV.forEach { row ->
                writeRow(
                    row.language,
                    row.type,
                    row.n,
                    row.timeMs,
                    row.primeCount,
                    row.correct
                )
            }
        }
        println("Results written to: ${csvFile.absolutePath}")
    }

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

            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    startSievePerformanceTests(warmupIterations, iterations,
                        timesKotlin, timesCpp, timesC,
                        timesResultsKotlin, timesResultsCpp, timesResultsC,
                        timesParallelKotlin, timesParallelCpp, timesParallelC,
                        timesResultsParallelCpp, timesResultsParallelC)
                }

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

                println("\n\n\n")
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

                withContext(Dispatchers.Main){
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
        }

        lifecycleScope.launch {
            runWarmup()
            runTests()
        }
    }


    private suspend fun startSievePerformanceTests(warmupIterations:Int, iterations:Int,
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

        println("\n\nWarmup ...")
        // default
        repeat(warmupIterations) {
            val timeKotlin = measureTime {
                primesCount = sieveKotlin(primeN)
            }
            println("Kotlin: Found $primesCount primes in ${timeKotlin.inWholeMilliseconds} ms")
        }
        repeat(warmupIterations) {

            val timeCpp = measureTime {
                primesCount = sieveCpp(primeN)
            }
            println("C++: Found $primesCount primes in ${timeCpp.inWholeMilliseconds} ms")
        }
        repeat(warmupIterations) {
            val timeC = measureTime {
                primesCount = sieveC(primeN)
            }
            println("C: Found $primesCount primes in ${timeC.inWholeMilliseconds} ms")
        }

        // results

        // parallel
        repeat(warmupIterations) {
            val timeKotlin2 = measureTime {
                primesCount = sieveParallelKotlin(primeN)
            }
            println("Kotlin: Found $primesCount primes in ${timeKotlin2.inWholeMilliseconds} ms")
        }
        repeat(warmupIterations) {
            val timeCpp2 = measureTime {
                primesCount = sieveParallelCpp(primeN)
            }
            println("C++: Found $primesCount primes in ${timeCpp2.inWholeMilliseconds} ms")
        }
        repeat(warmupIterations) {
            val timeC2 = measureTime {
                primesCount = sieveParallelC(primeN)
            }
            println("C: Found $primesCount primes in ${timeC2.inWholeMilliseconds} ms")
        }
        // results + parallel

        // default tests: kotlin, cpp, c
        println("\n\nTesting (default) ...")
        repeat(iterations) {
            val time1 = measureTime {
                primesCount = sieveKotlin(primeN)
            }
            println("Kotlin: Found $primesCount primes in ${time1.inWholeMilliseconds} ms")
            timesKotlin += time1.inWholeMilliseconds
        }
        repeat(iterations) {
            val time2 = measureTime {
                primesCount = sieveCpp(primeN)
            }
            println("Cpp: Found $primesCount primes in ${time2.inWholeMilliseconds} ms")
            timesCpp += time2.inWholeMilliseconds
        }
        repeat(iterations) {
            val time3 = measureTime {
                primesCount = sieveC(primeN)
            }
            println("C: Found $primesCount primes in ${time3.inWholeMilliseconds} ms")
            timesC += time3.inWholeMilliseconds
        }

        // results tests: kotlin, cpp, c

        // parallel tests: kotlin, cpp, c
        println("\n\nTesting (parallel) ...")
        repeat(iterations) {
            val time11 = measureTime {
                primesCount = sieveParallelKotlin(primeN)
            }
            println("Kotlin: Found $primesCount primes in ${time11.inWholeMilliseconds} ms")
            timesParallelKotlin += time11.inWholeMilliseconds
        }

        repeat(iterations) {
            val time4 = measureTime {
                primesCount = sieveParallelCpp(primeN)
            }
            println("Cpp: Found $primesCount primes in ${time4.inWholeMilliseconds} ms")
            timesParallelCpp += time4.inWholeMilliseconds
        }

        repeat(iterations) {
            val time5 = measureTime {
                primesCount = sieveParallelC(primeN)
            }
            println("C: Found $primesCount primes in ${time5.inWholeMilliseconds} ms")
            timesParallelC += time5.inWholeMilliseconds
        }

        // results + parallel: cpp, c

    }

    companion object {
        // Used to load the 'mytestapp' library on application startup.
        init {
            System.loadLibrary("mytestapp")
        }
    }
}