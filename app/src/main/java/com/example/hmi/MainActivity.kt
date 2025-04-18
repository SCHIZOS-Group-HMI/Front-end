package com.example.hmi

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hmi.ui.screens.CalmingExerciseScreen
import com.example.hmi.ui.screens.HomeScreen
import com.example.hmi.ui.screens.ScanScreen
import com.example.hmi.ui.theme.HMITheme

class MainActivity : ComponentActivity() {
    private var navigateToScan: (() -> Unit)? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            navigateToScan?.invoke()
        } else {
            // TODO: Show message about needing camera permission
        }
        navigateToScan = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HMITheme {
                AppNavigation(
                    onRequestCameraPermission = { navigate ->
                        navigateToScan = navigate
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    onRequestCameraPermission: (() -> Unit) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onScanClicked = {
                    onRequestCameraPermission { navController.navigate("scan") }
                },
                onCalmingExercisesClicked = {
                    navController.navigate("calming")
                }
            )
        }
        composable("scan") {
            ScanScreen(
                onQuitClicked = { navController.popBackStack() }
            )
        }
        composable("calming") {
            CalmingExerciseScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}