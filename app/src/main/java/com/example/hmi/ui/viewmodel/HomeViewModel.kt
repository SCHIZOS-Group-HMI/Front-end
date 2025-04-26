package com.example.hmi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    fun onMenuClicked() {
        Log.d("HomeViewModel", "Menu clicked")
        // TODO: Handle menu logic
    }

    fun onScanClicked() {
        Log.d("HomeViewModel", "Scan environment clicked")
    }

    fun onCalmingExercisesClicked() {
        Log.d("HomeViewModel", "Calming exercises clicked")
    }
}