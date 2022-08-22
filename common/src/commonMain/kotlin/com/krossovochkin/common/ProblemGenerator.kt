package com.krossovochkin.common

import kotlin.random.Random
import kotlin.random.nextInt

interface Problem {
    val text: String
    val result: Int
}

enum class ProblemType {
    Simple,
    Complex,
}

data class SimpleProblem(
    val first: Int,
    val second: Int,
    val operation: Operation,
    override val result: Int,
) : Problem {

    override val text: String = "$first ${operation.value} $second = "
}

data class ComplexProblem(
    val first: Int,
    val second: Int,
    val firstOperation: Operation,
    val third: Int,
    val secondOperation: Operation,
    override val result: Int,
) : Problem {
    override val text: String = "$first ${firstOperation.value} $second ${secondOperation.value} $third = "
}

enum class Operation(val value: String) {
    Addition("+"),
    Subtraction("-"),
    Multiplication("x"),
}

object ProblemGenerator {

    fun generateSimpleProblem(): SimpleProblem {
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
        )
    }

    fun generateComplexProblem(): ComplexProblem {
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
                    result = result
                )
            }
        }
    }
}
