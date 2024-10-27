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

        // Загружаем уровень сложности и текущий день
        loadUserProgress()
    }

    private fun loadUserProgress() {
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

                        if (difficultyLevel != null) {
                            loadWorkoutsForDifficulty(difficultyLevel)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load progress: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadWorkoutsForDifficulty(difficultyLevel: String) {
        val workoutData = loadWorkoutDataFromJson()

        // Получаем список всех дней и упражнений для выбранного уровня сложности
        val days = workoutData?.workoutLevels
            ?.firstOrNull { it.level == difficultyLevel }
            ?.weeks?.flatMap { it.days }
            ?: listOf()

        val dayExerciseItems = mutableListOf<DayExerciseItem>()

        days.forEachIndexed { index, day ->
            // Добавляем заголовок "Day X"
            dayExerciseItems.add(DayExerciseItem.DayHeader("Day ${index + 1}"))

            // Добавляем упражнения для этого дня
            day.exercises.forEach { exercise ->
                dayExerciseItems.add(DayExerciseItem.ExerciseItem(exercise))
            }
        }

        setupRecyclerView(dayExerciseItems)

        // Настраиваем клик по кнопке Start
        binding.btnStartDay.setOnClickListener {
            startWorkoutForDay(currentDay)
        }
    }

    private fun startWorkoutForDay(day: Int) {
        // Здесь будет логика перехода на экран тренировки
        Toast.makeText(requireContext(), "Starting Day $day", Toast.LENGTH_SHORT).show()

        // Переход на экран тренировки
        // Мы можем создать новый фрагмент для отображения текущей тренировки
        // Навигация на новый фрагмент
    }

    private fun loadWorkoutDataFromJson(): WorkoutData? {
        return try {
            val inputStream = requireContext().resources.openRawResource(R.raw.workout_data)
            val reader = InputStreamReader(inputStream)

            val gson = Gson()
            val workoutDataType = object : TypeToken<WorkoutData>() {}.type
            gson.fromJson(reader, workoutDataType)
        } catch (e: IOException) {
            Log.e("WorkoutsFragment", "Error loading workout data: ${e.message}")
            null
        }
    }

    private fun setupRecyclerView(dayExerciseItems: List<DayExerciseItem>) {
        workoutAdapter = WorkoutAdapter(dayExerciseItems)
        binding.recyclerViewWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }
}


