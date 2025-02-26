package com.example.mytestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mytestapp.databinding.ActivityMainBinding
import kotlin.math.sqrt
import kotlin.time.measureTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonExec.setOnClickListener {
            val iterations = 10
            val warmupIterations = 5

            val timesSDK = mutableListOf<Long>()
            val timesNDK = mutableListOf<Long>()
            val timesNDKC = mutableListOf<Long>()
            val timesParallelNDK = mutableListOf<Long>()
            val timesParallelNDKC = mutableListOf<Long>()

            startSievePerformanceTests(warmupIterations, iterations,
                timesSDK, timesNDK, timesNDKC, timesParallelNDK, timesParallelNDKC)

            val averageSDK = timesSDK.average()
            val averageNDK = timesNDK.average()
            val averageNDKC = timesNDKC.average()
            val averageParallelNDK = timesParallelNDK.average()
            val averageParallelNDKC = timesParallelNDKC.average()

            println("SDK average time (ms): $averageSDK")
            println("NDK average time (ms): $averageNDK")
            println("NDK_C average time (ms): $averageNDKC")
            println("NDK_C parallel average time (ms): $averageParallelNDK")
            println("NDK_C parallel average time (ms): $averageParallelNDKC")

            binding.execTimeSDK.text = "SDK: ${averageSDK}"
            binding.execTimeNDK.text = "NDK: ${averageNDK}"
            binding.execTimeNDKC.text = "NDK_C: ${averageNDKC}"
            binding.execTimeParallelNDK.text = "NDK parallel: ${averageParallelNDK}"
            binding.execTimeParallelNDKC.text = "NDK_C parallel: ${averageParallelNDKC}"
        }
    }


    private fun startSievePerformanceTests(warmupIterations:Int, iterations:Int,
                                           timesSDK:MutableList<Long>,
                                           timesNDK:MutableList<Long>,
                                           timesNDKC:MutableList<Long>,
                                           timesParallelNDK:MutableList<Long>,
                                           timesParallelNDKC:MutableList<Long>) {

        val primeN = binding.nPrimes.text.toString().toInt()

        repeat(warmupIterations){
            sieveOfEratosthenesSDK(primeN)
            sieveOfEratosthenesNDK(primeN)
            sieveOfEratosthenesNDKC(primeN)
            sieveOfEratosthenesParallelNDK(primeN)
            sieveOfEratosthenesParallelNDKC(primeN)
        }

        // Now measure multiple times
        repeat(iterations) {
            System.gc()
            val time1 = measureTime {
                val primes = sieveOfEratosthenesSDK(primeN)
                println(primes)
            }
            timesSDK += time1.inWholeMilliseconds
        }

        repeat(iterations) {
            System.gc()
            val time2 = measureTime {
                val primes = sieveOfEratosthenesNDK(primeN)
                println(primes)
            }
            timesNDK += time2.inWholeMilliseconds
        }

        repeat(iterations) {
            System.gc()
            val time3 = measureTime {
                val primes = sieveOfEratosthenesNDKC(primeN)
                println(primes)
            }
            timesNDKC += time3.inWholeMilliseconds
        }

        repeat(iterations) {
            System.gc()
            val time4 = measureTime {
                val primes = sieveOfEratosthenesParallelNDK(primeN)
                println(primes)
            }
            timesParallelNDK += time4.inWholeMilliseconds
        }

        repeat(iterations) {
            System.gc()
            val time5 = measureTime {
                val primes = sieveOfEratosthenesParallelNDKC(primeN)
                println(primes)
            }
            timesParallelNDKC += time5.inWholeMilliseconds
        }
    }

    private external fun sieveOfEratosthenesNDK(n: Int): Int

    private external fun sieveOfEratosthenesParallelNDK(n: Int): Int

    private external fun sieveOfEratosthenesNDKC(n: Int): Int

    private external fun sieveOfEratosthenesParallelNDKC(n: Int): Int

    private fun sieveOfEratosthenesSDK(n: Int): Int {
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

        /*val primes = ArrayList<Int>()
        for (i in 2..n) {
            if (isPrime[i]) {
                primes.add(i)
            }
        }*/

        val primesCount = isPrime.count { it }

        return primesCount
    }

    companion object {
        // Used to load the 'mytestapp' library on application startup.
        init {
            System.loadLibrary("mytestapp")
        }
    }
}