package dev.androidbroadcast.ironcore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.pose.Pose
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseAnalyzer: ExerciseAnalyzer
) : ViewModel() {

    private val _currentExercise = MutableLiveData<Exercise>()
    val currentExercise: LiveData<Exercise> = _currentExercise

    private val _setCompleted = MutableLiveData<Boolean>()
    val setCompleted: LiveData<Boolean> = _setCompleted

    private val _repsCount = MutableLiveData<Int>()
    val repsCount: LiveData<Int> = _repsCount

    // Обработка данных с камеры
    fun processPose(pose: Pose) {
        val currentExercise = _currentExercise.value ?: return
        val currentExerciseName = currentExercise.name

        val isSetCompleted = exerciseAnalyzer.analyzePose(pose, currentExerciseName)
        if (isSetCompleted) {
            currentExercise.currentSet += 1
            _setCompleted.value = true
        }

        val currentReps = exerciseAnalyzer.getRepsForExercise(currentExerciseName)
        currentExercise.currentReps = currentReps
        _currentExercise.value = currentExercise
    }

    // Установка текущего упражнения
    fun setCurrentExercise(exercise: Exercise) {
        _currentExercise.value = exercise
    }
}


