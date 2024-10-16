package dev.androidbroadcast.ironcore

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.androidbroadcast.ironcore.databinding.FragmentRegistrationBinding
import java.io.IOException
import java.io.InputStreamReader

class FragmentRegistration : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val difficultyLevel = getSelectedDifficultyLevel()
            val userName = binding.etUsername.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty() && difficultyLevel != null) {
                registerUser(email, password, userName, difficultyLevel)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Метод для получения выбранного уровня сложности
    private fun getSelectedDifficultyLevel(): String? {
        return when (binding.radioGroupDifficulty.checkedRadioButtonId) {
            R.id.rb_beginner -> "beginner"
            R.id.rb_intermediate -> "intermediate"
            R.id.rb_advanced -> "advanced"
            else -> null
        }
    }

    // Метод для регистрации пользователя и сохранения данных в Firestore
    private fun registerUser(email: String, password: String, userName: String, difficultyLevel: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Загружаем программу тренировок из JSON
                        val workoutData = loadWorkoutData(requireContext(), difficultyLevel)
                        saveUserData(userId, email, userName, difficultyLevel, workoutData)
                    }
                } else {
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Метод для загрузки программы тренировок в зависимости от уровня сложности
    private fun loadWorkoutData(context: Context, difficultyLevel: String): WorkoutLevel? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.workout_data)
            val reader = InputStreamReader(inputStream)

            val gson = Gson()
            val workoutDataType = object : TypeToken<WorkoutData>() {}.type
            val workoutData: WorkoutData = gson.fromJson(reader, workoutDataType)

            // Возвращаем уровень тренировки на основе выбранного уровня сложности
            workoutData.workoutLevels.firstOrNull { it.level == difficultyLevel }
        } catch (e: IOException) {
            Log.e("FragmentRegistration", "Error loading workout data: ${e.message}")
            null
        }
    }

    // Метод для сохранения данных пользователя в Firestore
    private fun saveUserData(userId: String, email: String, userName: String, difficultyLevel: String, workoutLevel: WorkoutLevel?) {
        // Преобразуем WorkoutLevel в Map
        val workoutMap = workoutLevel?.let {
            val exerciseMapList = it.weeks.flatMap { week ->
                week.days.flatMap { day ->
                    day.exercises.map { exercise ->
                        mapOf(
                            "name" to exercise.name,
                            "sets" to exercise.sets,
                            "reps" to exercise.reps,
                            "sec" to exercise.sec

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
            "workoutProgram" to workoutMap // Сохраняем структурированные данные
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registration_to_profile)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}



