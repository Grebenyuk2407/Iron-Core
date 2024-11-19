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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.ironcore.databinding.ExerciseFragmentBinding
import java.util.regex.Pattern

@AndroidEntryPoint
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

        // Показываем первое упражнение
        showExercise(currentExerciseIndex)

        // Обработка нажатия на кнопку Start для начала выполнения упражнения
        binding.btnStartExercise.setOnClickListener {
            startExercise(currentExerciseIndex)
        }
    }

    private fun showExercise(index: Int) {
        val exercise = exercises[index]

        // Установка данных упражнения
        binding.exerciseName.text = exercise.name
        binding.tvSets.text = "Sets: ${exercise.sets}"
        binding.tvReps.text = "Reps: ${exercise.reps ?: 0}"

        // Очистка текущего YouTube плеера перед загрузкой нового видео
        binding.youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                val videoUrl = extractYouTubeId(exercise.videoUrl)
                if (videoUrl != null) {
                    youTubePlayer.cueVideo(videoUrl, 0f)
                }
            }
        })
    }

    private fun startExercise(index: Int) {
        Toast.makeText(requireContext(), "Starting exercise: ${exercises[index].name}", Toast.LENGTH_SHORT).show()

        // Создание Bundle для передачи данных
        val bundle = Bundle().apply {
            putParcelable("exercise", exercises[index]) // Передача выбранного упражнения
        }

        // Переход на ExerciseCameraFragment с передачей данных
        findNavController().navigate(R.id.action_exerciseFragment_to_exerciseCameraFragment, bundle)
    }



    private fun extractYouTubeId(url: String): String? {
        val regex = "(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/\\S+/|(?:v|e(?:mbed)?)|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubePlayerView.release()
    }
}



