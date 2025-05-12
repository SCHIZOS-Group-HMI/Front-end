package com.example.hmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hmi.ui.theme.HMITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hmi.ui.viewmodel.CalmingExerciseViewModel
import com.example.hmi.ui.viewmodel.ExerciseType
import androidx.compose.foundation.shape.RoundedCornerShape

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
                .background(Color(0xFFF5F6FA))
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
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (uiState.currentExercise == ExerciseType.BREATHING) Color(0xFF1976D2) else Color.Gray)
                ) {
                    Button(
                        onClick = { viewModel.selectExercise(ExerciseType.BREATHING) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Breathing",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily
                        )
                    }
                }
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (uiState.currentExercise == ExerciseType.GROUNDING) Color(0xFF1976D2) else Color.Gray)
                ) {
                    Button(
                        onClick = { viewModel.selectExercise(ExerciseType.GROUNDING) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Grounding",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily
                        )
                    }
                }
            }

            // Exercise content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.White, Color(0xFFE3F2FD))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState.currentExercise) {
                        ExerciseType.BREATHING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Breathing Exercise",
                                    fontSize = 26.sp,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.padding(16.dp),
                                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                                )
                                Text(
                                    text = uiState.instruction,
                                    fontSize = 32.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    fontFamily = MaterialTheme.typography.displayMedium.fontFamily
                                )
                            }
                        }
                        ExerciseType.GROUNDING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Grounding Exercise (5-4-3-2-1)",
                                    fontSize = 26.sp,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.padding(16.dp),
                                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                                )
                                Text(
                                    text = uiState.instruction,
                                    fontSize = 24.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
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
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Next",
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                fontFamily = MaterialTheme.typography.labelLarge.fontFamily
                                            )
                                        }
                                    }
                                    if (uiState.groundingStep == 5) {
                                        Button(
                                            onClick = { viewModel.resetGrounding() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Restart",
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                fontFamily = MaterialTheme.typography.labelLarge.fontFamily
                                            )
                                        }
                                    }
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                ) {
                    Text(
                        text = "BACK",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontFamily = MaterialTheme.typography.labelLarge.fontFamily
                    )
                }
            }
        }
    }
}