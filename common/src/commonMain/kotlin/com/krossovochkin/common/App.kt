package com.krossovochkin.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.ui.draw.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun App() {
    var training: Training? by remember { mutableStateOf(null) }
    var trainingResult: TrainingResult? by remember { mutableStateOf(null) }

    if (trainingResult == null) {
        if (training == null) {
            SelectScreen { training = it }
        } else {
            ProblemScreen(training!!, { training = null }, {
                training = null
                trainingResult = it
            })
        }
    } else {
        FinishScreen(trainingResult!!) {
            trainingResult = null
        }
    }
}

@Composable
private fun SelectScreen(onClick: (Training) -> Unit) {
    var isOperatorsEncoded by remember { mutableStateOf(false) }
    var isDuplicateOperatorsAllowed by remember { mutableStateOf(false) }

    var isComplexityExpanded by remember { mutableStateOf(false) }
    var complexity: ProblemComplexity by remember { mutableStateOf(ProblemComplexity.Simple) }

    var isTrainingTypeExpanded by remember { mutableStateOf(false) }
    var trainingType: TrainingType by remember { mutableStateOf(TrainingType.Simple) }
    var simpleTrainingCountText: String by remember { mutableStateOf("10") }
    val simpleTrainingCount: Int? by remember { derivedStateOf { simpleTrainingCountText.toIntOrNull() }}
    var timeTrainingTimeSecondsText: String by remember { mutableStateOf("30") }
    val timeTrainingTimeMillis: Long? by remember { derivedStateOf { timeTrainingTimeSecondsText.toLongOrNull()?.let { it * 1000 } }}
    var timeTrainingIncrementMillisText: String by remember { mutableStateOf("300") }
    val timeTrainingIncrementMillis: Long? by remember { derivedStateOf { timeTrainingIncrementMillisText.toLongOrNull() }}

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopAppBar(
            title = { Text("Math Trainer") }
        )

        Spacer(Modifier.height(10.dp))
        
        Box(modifier = Modifier.padding(10.dp)) {
            Text("Type: $trainingType", modifier = Modifier.clickable { isTrainingTypeExpanded = true })
            DropdownMenu(isTrainingTypeExpanded, { isTrainingTypeExpanded = false }) {
                DropdownMenuItem(onClick = {
                    trainingType = TrainingType.Simple
                    isTrainingTypeExpanded = false
                }) {
                    Text("Simple")
                }
                DropdownMenuItem(onClick = {
                    trainingType = TrainingType.Infinite
                    isTrainingTypeExpanded = false
                }) {
                    Text("Infinite")
                }
                DropdownMenuItem(onClick = {
                    trainingType = TrainingType.TimeAttack
                    isTrainingTypeExpanded = false
                }) {
                    Text("Time Attack")
                }
            }
        }

        if (trainingType == TrainingType.Simple) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Count: ")
                TextField(
                    simpleTrainingCountText,
                    modifier = Modifier.width(200.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { simpleTrainingCountText = it.filter { it.isDigit() } }
                )
            }
        }

        if (trainingType == TrainingType.TimeAttack) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total time (s):")
                TextField(
                    timeTrainingTimeSecondsText,
                    modifier = Modifier.width(200.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { timeTrainingTimeSecondsText = it.filter { it.isDigit() }}
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Increment (millis):")
                TextField(
                    timeTrainingIncrementMillisText,
                    modifier = Modifier.width(200.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { timeTrainingIncrementMillisText = it.filter { it.isDigit() }}
                )
            }
        }

        Box(modifier = Modifier.padding(10.dp)) {
            Text("Complexity: $complexity", modifier = Modifier.clickable { isComplexityExpanded = true })
            DropdownMenu(isComplexityExpanded, { isComplexityExpanded = false }) {
                DropdownMenuItem(onClick = {
                    complexity = ProblemComplexity.Simple
                    isComplexityExpanded = false
                }) {
                    Text("Simple")
                }
                DropdownMenuItem(onClick = {
                    complexity = ProblemComplexity.Complex
                    isComplexityExpanded = false
                }) {
                    Text("Complex")
                }
            }
        }

        if (complexity == ProblemComplexity.Complex) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(isDuplicateOperatorsAllowed, onCheckedChange = { isDuplicateOperatorsAllowed = !isDuplicateOperatorsAllowed })
                Text("allow duplicate operators", modifier = Modifier.clickable { isDuplicateOperatorsAllowed = !isDuplicateOperatorsAllowed })
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(isOperatorsEncoded, onCheckedChange = { isOperatorsEncoded = !isOperatorsEncoded })
            Text("enable operator encoding", modifier = Modifier.clickable { isOperatorsEncoded = !isOperatorsEncoded })
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val generator = when (complexity) {
                    ProblemComplexity.Simple -> SimpleProblemGenerator(isOperatorsEncoded)
                    ProblemComplexity.Complex -> ComplexProblemGenerator(isOperatorsEncoded, isDuplicateOperatorsAllowed)
                }
                val training = when (trainingType) {
                    TrainingType.Simple -> SimpleTraining(generator, simpleTrainingCount!!)
                    TrainingType.Infinite -> InfiniteTraining(generator)
                    TrainingType.TimeAttack -> TimeTraining(generator, timeTrainingTimeMillis!!, timeTrainingIncrementMillis!!)
                }
                onClick(training)
            },
            enabled = when (trainingType) {
                TrainingType.Simple -> {
                    simpleTrainingCountText.isNotEmpty()
                }
                TrainingType.TimeAttack -> {
                    timeTrainingTimeSecondsText.isNotEmpty() && timeTrainingIncrementMillisText.isNotEmpty()
                }
                TrainingType.Infinite -> { true }
            }
        ) { Text("Start") }
    }
}

@Composable
private fun ProblemScreen(training: Training, onBack: () -> Unit, onComplete: (TrainingResult) -> Unit) {
    var problem by remember { mutableStateOf(training.nextProblem!!) }
    var input by remember { mutableStateOf("") }
    var inputColor by remember { mutableStateOf(Color.Black) }
    var timeRemainingMillis by remember { mutableStateOf(if (training is TimeTraining) training.timeMillis else 0L) }
    val timeIncrementMillis by remember { mutableStateOf(if (training is TimeTraining) training.timeIncrementMillis else 0L) }
    val tickMillis by remember { mutableStateOf(100L) }

    val onNextProblem = {
        val nextProblem = training.nextProblem
        if (nextProblem != null) {
            problem = nextProblem
            input = ""
            inputColor = Color.Black
        } else {
            onComplete(training.trainingResult)
        }
    }

    if (training is TimeTraining) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(tickMillis)
                timeRemainingMillis -= tickMillis
                if (timeRemainingMillis <= 0) {
                    onComplete(training.trainingResult)
                }
            }
        }
    }

    if (training !is SimpleTraining) {
        LaunchedEffect(input) {
            if (input.toIntOrNull() == problem.result) {
                inputColor = Color.Green
                delay(300L)

                timeRemainingMillis += timeIncrementMillis

                onNextProblem()
            }
        }
    }

    val onInput: (Int) -> Unit = {
        input = "$input$it"
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(
            title = { Text("Math Trainer") },
            navigationIcon = { 
                Text(" X ", modifier = Modifier.clickable { onBack() })
            }
        )

        if (training is TimeTraining) {
            Row {
                Spacer(Modifier.weight(1f))
                Text("${(timeRemainingMillis / 100).toFloat() / 10}s", fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(120.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(problem.text, fontSize = 24.sp, fontFamily = FontFamily.Monospace)
            Text(input, color = inputColor, fontSize = 24.sp, fontFamily = FontFamily.Monospace)
        }

        Spacer(Modifier.height(10.dp))

        if (problem.operationEnconding.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                problem.operationEnconding
                    .filter {
                        problem is SimpleProblem || it.key != Operation.Multiplication
                    }
                    .forEach { (operation, encoding) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
                            Text(operation.value, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                            Text(encoding, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
            }
        }

        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InputButton(1, onInput)
            InputButton(2, onInput)
            InputButton(3, onInput)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InputButton(4, onInput)
            InputButton(5, onInput)
            InputButton(6, onInput)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InputButton(7, onInput)
            InputButton(8, onInput)
            InputButton(9, onInput)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (input.isNotEmpty()) {
                        input = input.substring(0, input.length - 1)
                    }
                }
            ) {
                Text("<")
            }
            InputButton(0, onInput)
            Button(
                modifier = Modifier
                    .alpha(if (training is SimpleTraining) 1f else 0f),
                enabled = input.isNotEmpty(),
                onClick = { 
                    training.answer(input.toInt())
                    onNextProblem()
                }
            ) { Text("ok") }
        }
    }
}

@Composable
private fun FinishScreen(trainingResult: TrainingResult, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Math Trainer") },
            navigationIcon = { 
                Text(" X ", modifier = Modifier.clickable { onFinish() })
            }
        )
        LazyColumn(modifier = Modifier.padding(10.dp)) {
            item {
                Text(
                    "Finished: ${trainingResult.result}", 
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun InputButton(value: Int, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(value) },
    ) {
        Text("$value", fontFamily = FontFamily.Monospace)
    }
}