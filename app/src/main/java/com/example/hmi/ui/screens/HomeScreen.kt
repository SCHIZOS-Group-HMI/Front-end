package com.example.hmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hmi.ui.theme.HMITheme
import com.example.hmi.ui.viewmodel.HomeViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onScanClicked: () -> Unit,
    onCalmingExercisesClicked: () -> Unit
) {
    HMITheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // App title at the top center
            Text(
                text = "Schizos Application",
                fontSize = 28.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            )


            // Scan environment card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),

            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.onScanClicked()
                            onScanClicked()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Scan Environment",
                            fontSize = 22.sp,
                            color = Color.White,
                            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                        )
                    }
                }
            }

            // Calming exercises card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.onCalmingExercisesClicked()
                            onCalmingExercisesClicked()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Calming Exercises",
                            fontSize = 22.sp,
                            color = Color.White,
                            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                        )
                    }
                }
            }
        }
    }
}