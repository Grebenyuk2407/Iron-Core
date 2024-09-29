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
    private var exercisesList: List<Exercise> = listOf()

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

        // Получаем уровень сложности пользователя из Firestore
        loadUserDifficultyLevel()
    }

    // Метод для загрузки уровня сложности пользователя из Firestore
    private fun loadUserDifficultyLevel() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val difficultyLevel = document.getString("difficultyLevel")
                        if (difficultyLevel != null) {
                            loadWorkoutsForDifficulty(difficultyLevel)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load difficulty level: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Метод для загрузки тренировок на основе уровня сложности
    private fun loadWorkoutsForDifficulty(difficultyLevel: String) {
        // Загружаем JSON с тренировками
        val workoutData = loadWorkoutDataFromJson()

        // Получаем список всех упражнений для всех дней всех недель, соответствующих уровню сложности
        val exercises = workoutData?.workoutLevels
            ?.firstOrNull { it.level == difficultyLevel }
            ?.weeks?.flatMap { it.days }
            ?.flatMap { it.exercises }
            ?: listOf()

        setupRecyclerView(exercises)
    }

    // Метод для загрузки JSON тренировок из assets или raw
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

    // Настройка RecyclerView для отображения списка тренировок
    private fun setupRecyclerView(exercises: List<Exercise>) {
        workoutAdapter = WorkoutAdapter(exercises)
        binding.recyclerViewWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workoutAdapter
        }
    }
}
