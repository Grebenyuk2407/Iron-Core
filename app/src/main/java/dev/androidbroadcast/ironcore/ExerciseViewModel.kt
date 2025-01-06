package dev.androidbroadcast.ironcore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.pose.Pose
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseAnalyzer: ExerciseAnalyzer,
    private val firestore: FirebaseFirestore,  // Добавляем Firestore для сохранения данных
    private val auth: FirebaseAuth  // Добавляем FirebaseAuth для получения текущего пользователя
) : ViewModel() {

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises

    private val _currentExercise = MutableLiveData<Exercise>()
    val currentExercise: LiveData<Exercise> = _currentExercise

    private val _setCompleted = MutableLiveData<Boolean>()
    val setCompleted: LiveData<Boolean> = _setCompleted

    private val _repsCount = MutableLiveData<Int>()
    val repsCount: LiveData<Int> = _repsCount

    private val _workoutCompleted = MutableLiveData<Boolean>()
    val workoutCompleted: LiveData<Boolean> = _workoutCompleted

    // Счетчик для отслеживания выполненных повторений
    private var completedReps = 0

    // Установка списка упражнений
    fun setExercises(exerciseList: List<Exercise>) {
        exerciseList.forEach { it.isCurrent = false }

        if (exerciseList.isNotEmpty()) {
            exerciseList[0].isCurrent = true
            setCurrentExercise(exerciseList[0])
        }

        _exercises.value = exerciseList
    }

    // Установка текущего упражнения
    fun setCurrentExercise(exercise: Exercise) {
        completedReps = 0
        _currentExercise.value = exercise
    }

    fun moveToNextExercise() {
        val exercises = _exercises.value ?: return
        val currentExercise = _currentExercise.value ?: return

        val currentIndex = exercises.indexOf(currentExercise)

        if (currentIndex != -1 && currentIndex < exercises.size - 1) {
            currentExercise.isCurrent = false

            val nextExercise = exercises[currentIndex + 1]
            nextExercise.isCurrent = true

            _exercises.value = exercises
            setCurrentExercise(nextExercise)
        } else {
            completeWorkout()  // Все упражнения завершены
        }
    }

    // Обработка завершения всей тренировки
    private fun completeWorkout() {
        _workoutCompleted.value = true

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = firestore.collection("users").document(currentUser.uid)

            // Получаем текущее значение дня и инкрементируем его
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentDay = document.getLong("currentDay") ?: 1
                    val newDay = currentDay + 1

                    // Обновляем currentDay в Firestore
                    userRef.update("currentDay", newDay).addOnSuccessListener {
                        Log.d("ExerciseViewModel", "currentDay успешно обновлен до $newDay")
                    }.addOnFailureListener { e ->
                        Log.e("ExerciseViewModel", "Ошибка обновления currentDay", e)
                    }
                }
            }
        }
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
                completedReps = 0
                currentExercise.currentSet += 1

                if (currentExercise.currentSet > currentExercise.sets) {
                    currentExercise.isLastSetCompleted = true
                    currentExercise.isCompleted = true
                    _setCompleted.value = true

                    moveToNextExercise()
                } else {
                    _setCompleted.value = true
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




