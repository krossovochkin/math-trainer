package com.krossovochkin.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
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

    var isComplexityExpanded by remember { mutableStateOf(false) }
    var complexity: ProblemComplexity by remember { mutableStateOf(ProblemComplexity.Simple) }

    var isTrainingTypeExpanded by remember { mutableStateOf(false) }
    var trainingType: TrainingType by remember { mutableStateOf(TrainingType.Simple) }
    var simpleTrainingCount: Int by remember { mutableStateOf(10) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(isOperatorsEncoded, onCheckedChange = { isOperatorsEncoded = !isOperatorsEncoded })
            Text("enable operator encoding", modifier = Modifier.clickable { isOperatorsEncoded = !isOperatorsEncoded })
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
            }
        }

        if (trainingType == TrainingType.Simple) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Count: ")
                TextField(simpleTrainingCount.toString(), onValueChange = { simpleTrainingCount = it.toInt() })
            }
        }

        Spacer(Modifier.weight(1f))

        Button(onClick = {
            val generator = when (complexity) {
                ProblemComplexity.Simple -> SimpleProblemGenerator(isOperatorsEncoded)
                ProblemComplexity.Complex -> ComplexProblemGenerator(isOperatorsEncoded)
            }
            val training = when (trainingType) {
                TrainingType.Simple -> SimpleTraining(generator, simpleTrainingCount)
                TrainingType.Infinite -> InfiniteTraining(generator)
                TrainingType.TimeAttack -> InfiniteTraining(generator) // TODO: add support
            }
            onClick(training)
        }) { Text("Start") }
    }
}

@Composable
private fun ProblemScreen(training: Training, onBack: () -> Unit, onComplete: (TrainingResult) -> Unit) {
    var problem by remember { mutableStateOf(training.nextProblem!!) }
    var input by remember { mutableStateOf("") }
    var inputColor by remember { mutableStateOf(Color.Black) }

    LaunchedEffect(input) {
        if (input.toIntOrNull() == problem.result) {
            inputColor = Color.Green
            delay(300L)

            val nextProblem = training.nextProblem
            if (nextProblem != null) {
                problem = nextProblem
                input = ""
                inputColor = Color.Black
            } else {
                onComplete(training.trainingResult)
            }
        }
    }

    val onInput: (Int) -> Unit = {
        input = "$input$it"
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                onClick = { onBack() }
            ) { Text("close") }
        }
    }
}

@Composable
private fun FinishScreen(trainingResult: TrainingResult, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Finished: ${trainingResult.result}")
        Button(onClick = { onFinish() }) { Text("Finish") }
    }
}

@Composable
private fun InputButton(value: Int, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(value) },
    ) {
        Text("$value")
    }
}