package com.example.hmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hmi.ui.theme.HMITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hmi.ui.viewmodel.CalmingExerciseViewModel
import com.example.hmi.ui.viewmodel.ExerciseType

@Composable
fun CalmingExerciseScreen(
    viewModel: CalmingExerciseViewModel = viewModel(),
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HMITheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Exercise selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.selectExercise(ExerciseType.BREATHING) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.currentExercise == ExerciseType.BREATHING) Color.Black else Color.Gray
                    )
                ) {
                    Text(
                        text = "Breathing",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { viewModel.selectExercise(ExerciseType.GROUNDING) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.currentExercise == ExerciseType.GROUNDING) Color.Black else Color.Gray
                    )
                ) {
                    Text(
                        text = "Grounding",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            // Exercise content
            when (uiState.currentExercise) {
                ExerciseType.BREATHING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Breathing Exercise",
                            fontSize = 24.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = uiState.instruction,
                            fontSize = 32.sp,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                ExerciseType.GROUNDING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Grounding Exercise (5-4-3-2-1)",
                            fontSize = 24.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = uiState.instruction,
                            fontSize = 24.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (uiState.groundingStep in 1..4) {
                                Button(
                                    onClick = { viewModel.nextGroundingStep() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                                ) {
                                    Text(
                                        text = "Next",
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            if (uiState.groundingStep == 5) {
                                Button(
                                    onClick = { viewModel.resetGrounding() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                ) {
                                    Text(
                                        text = "Restart",
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        viewModel.onBackClicked()
                        onBackClicked()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                ) {
                    Text(
                        text = "BACK",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}