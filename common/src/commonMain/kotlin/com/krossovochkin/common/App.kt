package com.krossovochkin.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun App() {
    var screen: ProblemType? by remember { mutableStateOf(null) }

    when (screen) {
        null -> SelectScreen { screen = it }
        ProblemType.Simple -> ProblemScreen(ProblemGenerator::generateSimpleProblem) { screen = null }
        ProblemType.Complex -> ProblemScreen(ProblemGenerator::generateComplexProblem) { screen = null }
    }
}

@Composable
private fun SelectScreen(onClick: (ProblemType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { onClick(ProblemType.Simple) }) { Text("Simple") }
        Button(onClick = { onClick(ProblemType.Complex) }) { Text("Complex") }
    }
}

@Composable
private fun ProblemScreen(generate: () -> Problem, onBack: () -> Unit) {
    var problem by remember { mutableStateOf(generate()) }
    var input by remember { mutableStateOf("") }
    var inputColor by remember { mutableStateOf(Color.Black) }

    LaunchedEffect(input) {
        if (input.toIntOrNull() == problem.result) {
            inputColor = Color.Green
            delay(300L)
            problem = generate()
            input = ""
            inputColor = Color.Black
        }
    }

    val onInput: (Int) -> Unit = {
        input = "$input$it"
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(120.dp))

        Row {
            Text("${problem.text}", fontSize = 24.sp)
            Text(input, color = inputColor, fontSize = 24.sp)
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
private fun InputButton(value: Int, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(value) },
    ) {
        Text("$value")
    }
}