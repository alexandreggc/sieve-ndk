package com.example.mytestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.mytestapp.databinding.ActivityMainBinding
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.pow
import java.util.Locale
import kotlin.reflect.full.callSuspend
import kotlin.time.measureTime
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val warmupIterations = 3
    private val iterations = 5

    private val listN : MutableList<Long> = mutableListOf(
//        pow(2.0, 13.0).toLong(), // 8_192
//        pow(2.0, 14.0).toLong(), // 16_384
//        pow(2.0, 15.0).toLong(), // 32_768
//        pow(2.0, 16.0).toLong(), // 65_536
//        pow(2.0, 17.0).toLong(), // 131_072
//        pow(2.0, 18.0).toLong(), // 262_144
//        pow(2.0, 19.0).toLong(), // 524_288
//        pow(2.0, 20.0).toLong(), // 1_048_576
        pow(2.0, 21.0).toLong(), // 2_097_152
//        pow(2.0, 22.0).toLong(), // 4_194_304
//        pow(2.0, 23.0).toLong(), // 8_388_608
//        pow(2.0, 24.0).toLong(), // 16_777_216
//        pow(2.0, 25.0).toLong(), // 33_554_432
//        pow(2.0, 26.0).toLong(), // 67_108_864
//        pow(2.0, 27.0).toLong(), // 134_217_728
//        pow(2.0, 28.0).toLong(), // 268_435_456 // MAX Kotlin evenRemovedParallel fails Realme
//        pow(2.0, 29.0).toLong(), // 536_870_912
//        pow(2.0, 30.0).toLong(), // 1_073_741_824 // kotlin evenRemovedParallel fails EMULATOR
//        pow(2.0, 31.0).toLong(), // 2_147_483_648 // kotlin evenRemovedParallel fails
//        pow(2.0, 32.0).toLong(), // 4_294_967_296 // kotlin evenRemovedParallel fails
//        pow(2.0, 33.0).toLong(),  // 8_589_934_592 // MAX for C evenRemovedParallel Realme
//        pow(2.0, 34.0).toLong(),  // 17_179_869_184
//        pow(2.0, 35.0).toLong(),  // 34_359_738_368
//        pow(2.0, 36.0).toLong(),  // 68_719_476_736
    )

    private val algorithms : MutableList<SieveAlgorithm<Number>> = mutableListOf(
        SieveAlgorithm(AlgLang.KOTLIN , AlgType.DEFAULT, ::sieveKotlin),
        SieveAlgorithm(AlgLang.KOTLIN, AlgType.PARALLEL, ::sieveParallelKotlin),
        SieveAlgorithm(AlgLang.KOTLIN, AlgType.EVEN_REMOVED, ::sieveEvenRemovedKotlin),
        SieveAlgorithm(AlgLang.KOTLIN, AlgType.EVEN_REMOVED_PARALLEL, ::sieveEvenRemovedParallelKotlin),
        SieveAlgorithm(AlgLang.KOTLIN, AlgType.BIT_ARRAY, ::sieveBitArrayKotlin),
        SieveAlgorithm(AlgLang.CPP, AlgType.DEFAULT, ::sieveCpp),
        SieveAlgorithm(AlgLang.CPP, AlgType.PARALLEL, ::sieveParallelCpp),
        SieveAlgorithm(AlgLang.C, AlgType.DEFAULT, ::sieveC),
        SieveAlgorithm(AlgLang.C, AlgType.PARALLEL, ::sieveParallelC),
        SieveAlgorithm(AlgLang.C, AlgType.EVEN_REMOVED, ::sieveEvenRemovedC),
        SieveAlgorithm(AlgLang.C, AlgType.EVEN_REMOVED_PARALLEL, ::sieveEvenRemovedParallelC),
        SieveAlgorithm(AlgLang.C, AlgType.BIT_ARRAY, ::sieveBitArrayC),
    )

    private val resultsCSV = mutableListOf<ResultRow>()

    private suspend fun runWarmup() {
        println("\n\nWarmup ...")
        val algoNPair = listN.flatMap { n -> algorithms.map { n to it } }

        println(String.format(Locale.getDefault(), "%-30s | %10s", "Algorithm", "N primes"))
        for ((n, algorithm) in algoNPair) {
            println(String.format(Locale.getDefault(), "%-30s | %10d ", algorithm.function.name, n))
            repeat(warmupIterations) {
                try {
                    if (algorithm.language == AlgLang.KOTLIN) {
                        if (n <= Int.MAX_VALUE) algorithm.function.callSuspend(n.toInt())
                        else Log.e("Warmup", "N too large for Kotlin algorithm: $n")
                    }
                    else
                        algorithm.function.callSuspend(n)
                } catch (e: Exception) {
                    Log.e("Warmup", "Error in warmup for n=$n: $e")
                }
            }
        }
    }

    private suspend fun runTests() {
        println("\n\nTesting ...")
        for (n in listN) {
            val execTimes = mutableListOf<Long>()
            val primeCount1: Long = sieveParallelC(n)

            for (algorithm in algorithms) {
                System.gc()
                Runtime.getRuntime().gc()
                println("\n\nTest: ${algorithm.function.name} \tN: $n ")
                println(String.format(Locale.getDefault(),"%-10s | %15s | %10s", "Time", "Prime Count", "Correct"))

                // Run the algorithm multiple times and check if the prime count is correct
                val correct = BooleanArray(iterations) { false }
                var primesCountError: Number = 0
                execTimes.clear()

                try {
                    repeat(iterations) {
                        val nValue: Number =
                            if (algorithm.language == AlgLang.KOTLIN) {
                                if (n <= Int.MAX_VALUE) n.toInt()
                                else Log.e("Test", "N too large for Kotlin algorithm: $n")
                            }
                            else n
                        var primesCount: Number
                        val time = measureTime {
                            primesCount = algorithm.function.callSuspend(nValue)
                        }.inWholeMilliseconds
                        if (primesCount.toLong() != primeCount1) {
                            primesCountError = primesCount
                            Log.e("PRIME COUNT", "Incorrect prime count: $primesCount Correct: $primeCount1")
                        } else
                            correct[it] = true

                        execTimes.add(time)
                        println(String.format(Locale.getDefault(), "%-10d | %15d |  %10s", time, primesCount, correct[it]))
                    }
                } catch (
                    e: Exception
                ) {
                    Log.e("Error", "Error in test for algorithm ${algorithm.function.name} and n=$n: $e")
                }

                // Calculate average execution time and add to results
                val avgExecTime = execTimes.average().toLong()
                val correctResult = correct.all { it }
                val primesCountResult: Number = if (correctResult) primeCount1 else primesCountError
                println("Average time: $avgExecTime")
                resultsCSV.add(
                    ResultRow(
                        algorithm.language.toString(),
                        algorithm.type.toString(),
                        n,
                        avgExecTime,
                        primesCountResult,
                        correctResult
                    )
                )
            }
        }
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

    private suspend fun writeResultsToUI() {
        withContext(Dispatchers.Main){
            binding.resultsTable.removeAllViews()

            // Add table headers
            val headerRow = TableRow(this@MainActivity)
            headerRow.addView(TextView(this@MainActivity).apply { text = "Language"; setPadding(8, 8, 8, 8) })
            headerRow.addView(TextView(this@MainActivity).apply { text = "Type"; setPadding(8, 8, 8, 8) })
            headerRow.addView(TextView(this@MainActivity).apply { text = "N"; setPadding(8, 8, 8, 8) })
            headerRow.addView(TextView(this@MainActivity).apply { text = "Time (ms)"; setPadding(8, 8, 8, 8) })
            headerRow.addView(TextView(this@MainActivity).apply { text = "Prime Count"; setPadding(8, 8, 8, 8) })
            headerRow.addView(TextView(this@MainActivity).apply { text = "Correct"; setPadding(8, 8, 8, 8) })
            binding.resultsTable.addView(headerRow)

            // Add results to table
            for (result in resultsCSV){
                val row = TableRow(this@MainActivity)
                row.addView(TextView(this@MainActivity).apply { text = result.language; setPadding(8, 8, 8, 8) })
                row.addView(TextView(this@MainActivity).apply { text = result.type; setPadding(8, 8, 8, 8) })
                row.addView(TextView(this@MainActivity).apply { text = result.n.toString(); setPadding(8, 8, 8, 8) })
                row.addView(TextView(this@MainActivity).apply { text = result.timeMs.toString(); setPadding(8, 8, 8, 8); textAlignment=TextView.TEXT_ALIGNMENT_TEXT_END })
                row.addView(TextView(this@MainActivity).apply { text = result.primeCount.toString(); setPadding(8, 8, 8, 8); textAlignment=TextView.TEXT_ALIGNMENT_TEXT_END })
                row.addView(TextView(this@MainActivity).apply { text = result.correct.toString(); setPadding(8, 8, 8, 8) })
                binding.resultsTable.addView(row)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonExec.setOnClickListener {
            val nInput = binding.nPrimes.text.toString()
            val nInputLong = nInput.toLongOrNull()

            if (nInputLong != null && !listN.contains(nInputLong)){
                listN.add(nInputLong)
            }

            if (listN.isNotEmpty()){
                // reset results list
                resultsCSV.clear()

                // run tests and write results to CSV / UI
                lifecycleScope.launch {
                    withContext(Dispatchers.Default) {
                        val totalTestingTime = measureTime {
                            runWarmup()
                            runTests()
                            writeResultsToCSV()
                            writeResultsToUI()
                        }
                        println("\n\nTotal testing time: ${totalTestingTime.inWholeMinutes}")
                    }
                }
            }
        }
    }

    companion object {
        // Used to load the 'mytestapp' library on application startup.
        init {
            System.loadLibrary("mytestapp")
        }
    }
}