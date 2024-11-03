package dev.androidbroadcast.ironcore

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dev.androidbroadcast.ironcore.databinding.ExerciseFragmentBinding
import java.util.regex.Pattern

class ExerciseFragment : Fragment() {

    private lateinit var binding: ExerciseFragmentBinding
    private lateinit var exercises: List<Exercise>
    private var currentExerciseIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExerciseFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем список упражнений, переданных из WorkoutsFragment
        exercises = arguments?.getParcelableArrayList<Parcelable>("exercises")?.map { it as Exercise } ?: listOf()
        val day = arguments?.getInt("day") ?: 1

        // Показываем первое упражнение
        showExercise(currentExerciseIndex)

        // Обработка нажатия на кнопку Start для начала выполнения упражнения
        binding.btnStartExercise.setOnClickListener {
            startExercise(currentExerciseIndex)
        }
    }

    private fun showExercise(index: Int) {
        val exercise = exercises[index]
        binding.exerciseName.text = exercise.name
        binding.exerciseSetsReps.text = "Sets: ${exercise.sets}, Reps: ${exercise.reps ?: 0}, Sec: ${exercise.sec ?: 0}"

        // Инициализация YouTube плеера для видео
        lifecycle.addObserver(binding.youtubePlayerView) // Связываем с жизненным циклом
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoUrl = extractYouTubeId(exercise.videoUrl)
                if (videoUrl != null) {
                    youTubePlayer.loadVideo(videoUrl, 0f) // Загружаем видео
                }
            }
        })
    }

    private fun startExercise(index: Int) {
        Toast.makeText(requireContext(), "Starting exercise: ${exercises[index].name}", Toast.LENGTH_SHORT).show()

        // Переход к следующему упражнению или таймеру для отдыха
        if (index < exercises.size - 1) {
            currentExerciseIndex++
            showExercise(currentExerciseIndex)
        } else {
            Toast.makeText(requireContext(), "You have completed all exercises for today!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractYouTubeId(url: String): String? {
        val regex = "(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/\\S+/|(?:v|e(?:mbed)?)|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubePlayerView.release() // Освобождаем плеер
    }
}

