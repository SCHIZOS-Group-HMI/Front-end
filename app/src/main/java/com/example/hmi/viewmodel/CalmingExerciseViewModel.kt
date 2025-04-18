package com.example.hmi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalmingExerciseViewModel : ViewModel() {
    private val _instruction = MutableStateFlow("Breathe In")
    val instruction: StateFlow<String> = _instruction.asStateFlow()

    init {
        startBreathingCycle()
    }

    private fun startBreathingCycle() {
        viewModelScope.launch {
            while (true) {
                _instruction.value = "Breathe In"
                delay(4000) // 4s inhale
                _instruction.value = "Hold"
                delay(2000) // 2s hold
                _instruction.value = "Breathe Out"
                delay(4000) // 4s exhale
            }
        }
    }

    fun onBackClicked() {
        Log.d("CalmingExerciseViewModel", "Back clicked")
        // No state reset needed since navigation will handle it
    }
}