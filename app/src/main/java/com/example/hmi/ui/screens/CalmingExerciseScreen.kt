package com.example.hmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hmi.ui.theme.HMITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hmi.viewmodel.CalmingExerciseViewModel

@Composable
fun CalmingExerciseScreen(
    viewModel: CalmingExerciseViewModel = viewModel(),
    onBackClicked: () -> Unit
) {
    val instruction by viewModel.instruction.collectAsState()

    HMITheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                text = "Breathing Exercise",
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )

            // Breathing instruction
            Text(
                text = instruction,
                fontSize = 32.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Back button
            Button(
                onClick = {
                    viewModel.onBackClicked()
                    onBackClicked()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.padding(16.dp)
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