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

    fun answer(value: Int)

    val trainingResult: TrainingResult
}

class InfiniteTraining(
    private val generator: ProblemGenerator,
) : Training {

    override val nextProblem: Problem
        get() = generator.generate()

    override fun answer(value: Int) {
        // do nothing
    }

    override val trainingResult: TrainingResult
        get() = object : TrainingResult {
            override val result: String = ""
        }
}

class TimeTraining(
    private val generator: ProblemGenerator,
    val timeMillis: Long,
    val timeIncrementMillis: Long,
) : Training {

    private var solvedProblems = -1

    override val nextProblem: Problem
        get() = generator.generate().also { solvedProblems++ }

    override fun answer(value: Int) {
        // do nothing
    }

    override val trainingResult: TrainingResult
        get() = object : TrainingResult {
            override val result: String = "Solved problems: $solvedProblems"
        }
}

class SimpleTraining(
    private val generator: ProblemGenerator,
    private val count: Int,
) : Training {

    private var currentCount: Int = 0
    private val startTimestamp = System.currentTimeMillis()

    private var currentProblem: Problem? = null
    private val results = mutableListOf<Pair<Problem, Int>>()

    override val nextProblem: Problem?
        get() {
            currentCount++
            return if (currentCount <= count) {
                generator.generate()
            } else {
                null
            }.also { currentProblem = it }
        }

    override fun answer(value: Int) {
        val currentProblem = currentProblem
        if (currentProblem != null) {
            results += currentProblem to value
        }
    }

    override val trainingResult: TrainingResult
        get() {
            return object : TrainingResult {
                override val result: String
                    get() {
                        val timeSeconds = (System.currentTimeMillis() - startTimestamp) / 1000
                        return buildString {
                            append("Solved in ${timeSeconds}s")
                            append("\n")
                            results.forEach { (problem, answer) ->
                                append("${problem.text} ${answer.toString().padEnd(2, ' ')} ")
                                append(if (problem.result == answer) "✔️" else "❌")
                                append("\n")
                            }
                        } 
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
    private val isDuplicateOperatorsAllowed: Boolean,
) : ProblemGenerator {

    private val operationEncoding = if (isOperatorsEncoded) {
        Operation.values().zip(operationEncondings.shuffled()).toMap()
    } else {
        emptyMap()
    }

    override fun generate(): Problem {
        val operations = Operation.values().filter { it != Operation.Multiplication }
        val firstOperation = operations.random()
        val secondOperation = if (isDuplicateOperatorsAllowed) {
            operations.random()
        } else {
            operations.filter { it != firstOperation }.random()
        }

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