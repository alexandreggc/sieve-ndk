package com.example.mytestapp

import kotlin.reflect.KFunction


data class SieveAlgorithm<T : Number>(
    val language: Enum<AlgLang>,
    val type: Enum<AlgType>,
    val function: KFunction<T>
)

data class ResultRow(
    val language: String,
    val type: String,
    val n: Number,
    val timeMs: Long,
    val primeCount: Number,
    val correct: Boolean
)

enum class AlgLang {
    KOTLIN {
        override fun toString(): String {
            return "Kotlin"
        }
    },
    CPP {
        override fun toString(): String {
            return "Cpp"
        }
    },
    C {
        override fun toString(): String {
            return "C"
        }
    }
}

enum class AlgType {
    DEFAULT {
        override fun toString(): String {
            return "default"
        }
    },
    PARALLEL {
        override fun toString(): String {
            return "parallel"
        }
    },
    EVEN_REMOVED {
        override fun toString(): String {
            return "evenRemoved"
        }
    },
    EVEN_REMOVED_PARALLEL {
        override fun toString(): String {
            return "evenRemovedParallel"
        }
    },
    BIT_ARRAY {
        override fun toString(): String {
            return "bitArray"
        }
    }
}