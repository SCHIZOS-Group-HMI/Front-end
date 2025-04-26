package com.example.hmi.ui.screens

import androidx.compose.foundation.background
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
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Menu button at top left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { viewModel.onMenuClicked() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_more),
                        contentDescription = "Menu",
                        tint = Color.Black
                    )
                }
            }

            // Spacer to add some space
            Spacer(modifier = Modifier.height(16.dp))

            // Scan environment button
            Button(
                onClick = {
                    viewModel.onScanClicked()
                    onScanClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = "scan environment",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            // Calming exercises button
            Button(
                onClick = {
                    viewModel.onCalmingExercisesClicked()
                    onCalmingExercisesClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = "calming exercises",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}