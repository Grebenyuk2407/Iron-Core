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

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises

    private val _currentExercise = MutableLiveData<Exercise>()
    val currentExercise: LiveData<Exercise> = _currentExercise

    private val _setCompleted = MutableLiveData<Boolean>()
    val setCompleted: LiveData<Boolean> = _setCompleted

    private val _repsCount = MutableLiveData<Int>()
    val repsCount: LiveData<Int> = _repsCount

    private val _workoutCompleted = MutableLiveData<Boolean>()  // Новый флаг завершения тренировки
    val workoutCompleted: LiveData<Boolean> = _workoutCompleted



    // Счетчик для отслеживания выполненных повторений
    private var completedReps = 0

    // Установка списка упражнений
    fun setExercises(exerciseList: List<Exercise>) {
        _exercises.value = exerciseList
    }

    // Установка текущего упражнения
    fun setCurrentExercise(exercise: Exercise) {
        completedReps = 0 // Сбрасываем количество повторений при начале нового упражнения
        _currentExercise.value = exercise
    }

    // Обработка данных с камеры
    fun processPose(pose: Pose) {
        val currentExercise = _currentExercise.value ?: return
        val currentExerciseName = currentExercise.name

        val isRepCompleted = exerciseAnalyzer.analyzePose(pose, currentExerciseName)
        if (isRepCompleted) {
            completedReps += 1
            _repsCount.value = completedReps

            if (completedReps >= currentExercise.reps ?: 0) {
                completedReps = 0 // Сбрасываем счетчик для следующего подхода
                currentExercise.currentSet += 1

                // Если завершены все подходы, отмечаем упражнение как завершенное
                if (currentExercise.currentSet > currentExercise.sets) {
                    currentExercise.isLastSetCompleted = true
                    currentExercise.isCompleted = true
                    _setCompleted.value = true
                } else {
                    _setCompleted.value = true // Подход завершен, но упражнение не закончено
                }

                _currentExercise.value = currentExercise
            }
        }
    }


    fun startNewSet() {
        completedReps = 0
        _repsCount.value = completedReps
    }

    fun resetSetCompletionFlag() {
        _setCompleted.value = false
    }

}



