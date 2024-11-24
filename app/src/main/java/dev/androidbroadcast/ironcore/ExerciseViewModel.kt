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

    private val _workoutCompleted = MutableLiveData<Boolean>()  // Новый флаг завершения тренировки
    val workoutCompleted: LiveData<Boolean> = _workoutCompleted

    // Индекс текущего упражнения
    private var currentExerciseIndex = 0

    // Список упражнений
    private lateinit var exercises: List<Exercise>

    // Счетчик для отслеживания выполненных повторений
    private var completedReps = 0

    // Установка списка упражнений
    fun setExercises(exerciseList: List<Exercise>) {
        exercises = exerciseList
        setCurrentExercise(exercises[0])  // Устанавливаем первое упражнение
    }

    // Установка текущего упражнения
    private fun setCurrentExercise(exercise: Exercise) {
        completedReps = 0 // Сбрасываем количество повторений при начале нового упражнения
        _currentExercise.value = exercise
    }

    // Обработка данных с камеры
    fun processPose(pose: Pose) {
        val currentExercise = _currentExercise.value ?: return
        val currentExerciseName = currentExercise.name

        // Проверка, завершено ли одно повторение
        val isRepCompleted = exerciseAnalyzer.analyzePose(pose, currentExerciseName)
        if (isRepCompleted) {
            completedReps += 1
            _repsCount.value = completedReps

            // Если выполнено нужное количество повторений, завершить подход
            if (completedReps >= currentExercise.reps ?: 0) {
                completedReps = 0 // сбрасываем счетчик для следующего подхода
                currentExercise.currentSet += 1
                _currentExercise.value = currentExercise

                // Проверяем, завершены ли все подходы
                if (currentExercise.currentSet > currentExercise.sets) {
                    _setCompleted.value = true
                    moveToNextExercise()  // Переход к следующему упражнению
                }
            }
        }
    }

    // Переход к следующему упражнению
    private fun moveToNextExercise() {
        if (currentExerciseIndex < exercises.size - 1) {
            currentExerciseIndex++
            setCurrentExercise(exercises[currentExerciseIndex])
        } else {
            _workoutCompleted.value = true  // Все упражнения завершены
        }
    }
}



