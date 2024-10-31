package dev.androidbroadcast.ironcore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.androidbroadcast.ironcore.databinding.FragmentWorkoutBinding
import java.io.IOException
import java.io.InputStreamReader

class WorkoutsFragment : Fragment() {

    private lateinit var binding: FragmentWorkoutBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var workoutAdapter: WorkoutAdapter
    private var currentDay: Int = 1 // Стартовый день 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Загружаем данные о пользователе, включая программу тренировок и прогресс
        loadUserProgressAndWorkoutData()
    }

    // Загружаем уровень сложности, прогресс и программу тренировок из Firestore
    private fun loadUserProgressAndWorkoutData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val difficultyLevel = document.getString("difficultyLevel")
                        val dayProgress = document.getLong("currentDay")?.toInt() ?: 1
                        currentDay = dayProgress

                        // Обновляем текст кнопки
                        binding.btnStartDay.text = "Start Day $currentDay"

                        val workoutProgram = document.get("workoutProgram") as? Map<String, Any>
                        if (workoutProgram != null) {
                            // Загружаем тренировки из Firestore
                            loadWorkoutsFromFirestore(workoutProgram)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load progress: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Обработка данных программы тренировок из Firestore
    private fun loadWorkoutsFromFirestore(workoutProgram: Map<String, Any>) {
        val exercises = workoutProgram["exercises"] as? List<Map<String, Any>> ?: listOf()

        // Получаем список всех дней и упражнений
        val dayExerciseItems = mutableListOf<DayExerciseItem>()
        exercises.forEachIndexed { index, exerciseData ->
            val dayNumber = index / 3 + 1  // Например, если три упражнения на день
            if (index % 3 == 0) {
                // Добавляем заголовок "Day X"
                dayExerciseItems.add(DayExerciseItem.DayHeader("Day $dayNumber"))
            }
            val exerciseName = exerciseData["name"] as? String ?: ""
            val sets = (exerciseData["sets"] as? Long)?.toInt() ?: 0
            val reps = (exerciseData["reps"] as? Long)?.toInt()
            val sec = (exerciseData["sec"] as? Long)?.toInt()

            // Добавляем упражнение в список
            dayExerciseItems.add(DayExerciseItem.ExerciseItem(Exercise(exerciseName, reps, sec, sets, ""))) // Пока пустой videoUrl
        }

        setupRecyclerView(dayExerciseItems)

        // Настраиваем клик по кнопке Start
        binding.btnStartDay.setOnClickListener {
            startWorkoutForDay(currentDay)
        }
    }

    private fun startWorkoutForDay(day: Int) {
        // Переход к тренировочному процессу
        Toast.makeText(requireContext(), "Starting Day $day", Toast.LENGTH_SHORT).show()

        // Логика перехода на следующий фрагмент с первым упражнением
    }

    private fun setupRecyclerView(dayExerciseItems: List<DayExerciseItem>) {
        workoutAdapter = WorkoutAdapter(dayExerciseItems)
        binding.recyclerViewWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }
}


