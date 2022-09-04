package com.krossovochkin.common

import kotlin.random.Random
import kotlin.random.nextInt

private val operationEncondings = listOf(
    "■",
    "□",
    "▣",
    "▨",
    "▲",
    "△",
    "▶",
    "▷",
    "◆",
    "◇",
    "◉",
    "○",
    "●",
    "◐",
    "◢",
    "◧",
    "★",
    "☆",
    "☀",
    "☁",
    "☒",
    "☢",
    "☺",
    "☾",
    "♫",
    "♯",
    "⚑",
)

interface TrainingResult {
    val result: String
}

interface Training {
    val nextProblem: Problem?
    val trainingResult: TrainingResult
}

class InfiniteTraining(
    private val generator: ProblemGenerator,
) : Training {

    override val nextProblem: Problem
        get() = generator.generate()

    override val trainingResult: TrainingResult
        get() = object : TrainingResult {
            override val result: String = ""
        }
}

class SimpleTraining(
    private val generator: ProblemGenerator,
    private val count: Int,
) : Training {

    private var currentCount: Int = 0
    private val startTimestamp = System.currentTimeMillis()

    override val nextProblem: Problem?
        get() {
            currentCount++
            return if (currentCount <= count) {
                generator.generate()
            } else {
                null
            }
        }

    override val trainingResult: TrainingResult
        get() {
            return object : TrainingResult {
                override val result: String
                    get() {
                        val timeSeconds = (System.currentTimeMillis() - startTimestamp) / 1000
                        return "Solved in ${timeSeconds}s"
                    }
            }
        }
}

interface Problem {
    val text: String
    val result: Int
    val operationEnconding: Map<Operation, String>
}

interface ProblemGenerator {

    fun generate(): Problem
}

class SimpleProblemGenerator(
    isOperatorsEncoded: Boolean
) : ProblemGenerator {

    private val operationEncoding = if (isOperatorsEncoded) {
        Operation.values().zip(operationEncondings.shuffled()).toMap()
    } else {
        emptyMap()
    }

    override fun generate(): Problem {
        val operation = Operation.values().random()

        val first = if (operation != Operation.Subtraction) {
            Random.nextInt(1..9)
        } else {
            Random.nextInt(2..9)
        }
        val second = if (operation != Operation.Subtraction) {
            Random.nextInt(1..9)
        } else {
            Random.nextInt(1 until first)
        }
        val result = when (operation) {
            Operation.Addition -> first + second
            Operation.Subtraction -> first - second
            Operation.Multiplication -> first * second
        }

        return SimpleProblem(
            first = first,
            second = second,
            result = result,
            operation = operation,
            operationEnconding = operationEncoding,
        )
    }
}

class ComplexProblemGenerator(
    isOperatorsEncoded: Boolean,
) : ProblemGenerator {

    private val operationEncoding = if (isOperatorsEncoded) {
        Operation.values().zip(operationEncondings.shuffled()).toMap()
    } else {
        emptyMap()
    }

    override fun generate(): Problem {
        val operations = Operation.values().filter { it != Operation.Multiplication }
        val firstOperation = operations.random()
        val secondOperation = operations.random()

        while (true) {
            val first = Random.nextInt(1..9)
            val second = Random.nextInt(1..9)
            val third = Random.nextInt(1..9)

            val result = first +
                    (if (firstOperation == Operation.Addition) second else -second) +
                    (if (secondOperation == Operation.Addition) third else -third)

            if (result > 0) {
                return ComplexProblem(
                    first = first,
                    second = second,
                    third = third,
                    firstOperation = firstOperation,
                    secondOperation = secondOperation,
                    result = result,
                    operationEnconding = operationEncoding,
                )
            }
        }
    }
}

enum class TrainingType {
    Simple,
    Infinite,
    TimeAttack,
}

enum class ProblemComplexity {
    Simple,
    Complex,
}

data class SimpleProblem(
    val first: Int,
    val second: Int,
    val operation: Operation,
    override val operationEnconding: Map<Operation, String>,
    override val result: Int,
) : Problem {

    override val text: String = "$first ${operationEnconding[operation] ?: operation.value} $second = "
}

data class ComplexProblem(
    val first: Int,
    val second: Int,
    val firstOperation: Operation,
    val third: Int,
    val secondOperation: Operation,
    override val operationEnconding: Map<Operation, String>,
    override val result: Int,
) : Problem {
    override val text: String =
        "$first ${operationEnconding[firstOperation] ?: firstOperation.value} $second ${operationEnconding[secondOperation] ?: secondOperation.value} $third = "
}

enum class Operation(val value: String) {
    Addition("+"),
    Subtraction("-"),
    Multiplication("x"),
}