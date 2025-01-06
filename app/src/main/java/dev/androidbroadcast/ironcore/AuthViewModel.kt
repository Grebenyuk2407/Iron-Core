package dev.androidbroadcast.ironcore

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> = _loginState

    private val _registerState = MutableLiveData<Boolean>()
    val registerState: LiveData<Boolean> = _registerState

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun login(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = true

                // Сохранение состояния "Запомнить меня"
                val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                if (rememberMe) {
                    editor.putBoolean("rememberMe", true)
                    editor.putString("email", email)
                    editor.putString("password", password)
                } else {
                    editor.putBoolean("rememberMe", false)
                    editor.remove("email")
                    editor.remove("password")
                }

                editor.apply()

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
                _loginState.value = false
            }
        }
    }


    fun register(email: String, password: String, userName: String, difficultyLevel: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()

                // Получаем ID пользователя
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    // Загружаем тренировочные данные
                    val workoutData = loadWorkoutData(difficultyLevel)
                    saveUserData(userId, email, userName, difficultyLevel, workoutData)
                }

                _registerState.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registration failed"
                _registerState.value = false
            }
        }
    }

    // Метод для загрузки программы тренировок
    private fun loadWorkoutData(difficultyLevel: String): WorkoutLevel? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.workout_data)
            val reader = InputStreamReader(inputStream)

            val gson = Gson()
            val workoutDataType = object : TypeToken<WorkoutData>() {}.type
            val workoutData: WorkoutData = gson.fromJson(reader, workoutDataType)
            workoutData.workoutLevels.firstOrNull { it.level == difficultyLevel }
        } catch (e: IOException) {
            Log.e("AuthViewModel", "Error loading workout data: ${e.message}")
            null
        }
    }

    // Метод для сохранения данных пользователя в Firestore
    private fun saveUserData(userId: String, email: String, userName: String, difficultyLevel: String, workoutLevel: WorkoutLevel?) {
        val workoutMap = workoutLevel?.let {
            val exerciseMapList = it.weeks.flatMap { week ->
                week.days.flatMap { day ->
                    day.exercises.map { exercise ->
                        mapOf(
                            "name" to exercise.name,
                            "sets" to exercise.sets,
                            "reps" to exercise.reps,
                            "sec" to exercise.sec,
                            "videoUrl" to exercise.videoUrl
                        )
                    }
                }
            }
            mapOf(
                "level" to it.level,
                "exercises" to exerciseMapList
            )
        }

        val userMap = hashMapOf(
            "email" to email,
            "difficultyLevel" to difficultyLevel,
            "username" to userName,
            "workoutProgram" to workoutMap,
            "currentDay" to 1
        )

        firestore.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Error saving user data: ${e.message}")
            }
    }
}
