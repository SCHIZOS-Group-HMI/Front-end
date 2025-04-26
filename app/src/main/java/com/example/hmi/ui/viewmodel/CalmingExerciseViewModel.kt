package com.example.hmi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExerciseType {
    BREATHING, GROUNDING
}

data class CalmingExerciseUiState(
    val currentExercise: ExerciseType = ExerciseType.BREATHING,
    val instruction: String = "Breathe In",
    val groundingStep: Int = 0 // 0: Not started, 1-5: Steps of 5-4-3-2-1
)

class CalmingExerciseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalmingExerciseUiState())
    val uiState: StateFlow<CalmingExerciseUiState> = _uiState.asStateFlow()
    private var breathingJob: Job? = null

    init {
        startBreathingCycle()
    }

    private fun startBreathingCycle() {
        breathingJob?.cancel() // Cancel any existing job
        breathingJob = viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(instruction = "Breathe In")
                delay(4000) // 4s inhale
                _uiState.value = _uiState.value.copy(instruction = "Hold")
                delay(2000) // 2s hold
                _uiState.value = _uiState.value.copy(instruction = "Breathe Out")
                delay(4000) // 4s exhale
            }
        }
    }

    fun selectExercise(exercise: ExerciseType) {
        if (_uiState.value.currentExercise != exercise) {
            breathingJob?.cancel()
            if (exercise == ExerciseType.BREATHING) {
                _uiState.value = CalmingExerciseUiState(
                    currentExercise = ExerciseType.BREATHING,
                    instruction = "Breathe In",
                    groundingStep = 0
                )
                startBreathingCycle()
            } else {
                _uiState.value = CalmingExerciseUiState(
                    currentExercise = ExerciseType.GROUNDING,
                    instruction = "Look around and find 5 things you can see",
                    groundingStep = 1
                )
            }
            Log.d("CalmingExerciseViewModel", "Selected exercise: $exercise")
        }
    }

    fun nextGroundingStep() {
        val currentStep = _uiState.value.groundingStep
        if (_uiState.value.currentExercise == ExerciseType.GROUNDING && currentStep < 5) {
            val nextStep = currentStep + 1
            val instruction = when (nextStep) {
                2 -> "Touch 4 things you can feel"
                3 -> "Listen for 3 things you can hear"
                4 -> "Smell 2 things you can smell"
                5 -> "Taste 1 thing you can taste"
                else -> "Completed!"
            }
            _uiState.value = _uiState.value.copy(
                groundingStep = nextStep,
                instruction = instruction
            )
            Log.d("CalmingExerciseViewModel", "Grounding step: $nextStep")
        }
    }

    fun resetGrounding() {
        if (_uiState.value.currentExercise == ExerciseType.GROUNDING) {
            _uiState.value = _uiState.value.copy(
                groundingStep = 1,
                instruction = "Look around and find 5 things you can see"
            )
            Log.d("CalmingExerciseViewModel", "Reset grounding exercise")
        }
    }


    fun onBackClicked() {
        Log.d("CalmingExerciseViewModel", "Back clicked")
        breathingJob?.cancel()
        _uiState.value = CalmingExerciseUiState() // Reset to initial state
    }

    override fun onCleared() {
        super.onCleared()
        breathingJob?.cancel()
    }
}