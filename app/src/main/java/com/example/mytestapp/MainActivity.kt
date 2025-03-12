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
        pow(2.0, 19.0).toInt(), // 524_288
        pow(2.0, 20.0).toInt(), // 1_048_576
        pow(2.0, 21.0).toInt(), // 2_097_152
        pow(2.0, 22.0).toInt(), // 4_194_304
        pow(2.0, 23.0).toInt(), // 8_388_608
//        pow(2.0, 24.0).toInt(), // 16_777_216
//        pow(2.0, 25.0).toInt(), // 33_554_432
//        pow(2.0, 26.0).toInt(), // 67_108_864
//        pow(2.0, 27.0).toInt(), // 134_217_728
    )

    private val algorithms : MutableList<SieveAlgorithm> = mutableListOf(
        SieveAlgorithm("Kotlin", "default", ::sieveKotlin),
        SieveAlgorithm("Cpp", "default", ::sieveCpp),
        SieveAlgorithm("C", "default", ::sieveC),
        SieveAlgorithm("Kotlin", "parallel", ::sieveParallelKotlin),
        SieveAlgorithm("Cpp", "parallel", ::sieveParallelCpp),
        SieveAlgorithm("C", "parallel", ::sieveParallelC),
    )

    private val resultsCSV = mutableListOf<ResultRow>()

    private suspend fun runWarmup() {
        println("\n\nWarmup ...")
        val algoNPair = listN.flatMap { n -> algorithms.map { n to it } }

        println(String.format(Locale.getDefault(), "%-50s | %10s", "Algorithm", "N primes"))
        for ((n, algorithm) in algoNPair) {
            println(String.format(Locale.getDefault(), "%-50s | %10d ", algorithm.function.name, n))
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
                println(String.format(Locale.getDefault(),"%-10s | %10s | %10s", "Time", "Prime Count", "Correct"))

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
            val nInputInt = nInput.toIntOrNull()

            if (nInputInt != 0 && nInputInt != null && !listN.contains(nInputInt)){
                listN.add(nInputInt)
            }

            if (listN.isNotEmpty()){
                // reset results list
                resultsCSV.clear()

                // run tests and write results to CSV / UI
                lifecycleScope.launch {
                    withContext(Dispatchers.Default) {
                        runWarmup()
                        runTests()
                        writeResultsToCSV()
                        writeResultsToUI()
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