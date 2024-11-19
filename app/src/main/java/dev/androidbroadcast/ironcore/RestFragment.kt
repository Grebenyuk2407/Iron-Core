package dev.androidbroadcast.ironcore

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.ironcore.databinding.FragmentRestBinding

@AndroidEntryPoint
class RestFragment : Fragment() {

    private lateinit var binding: FragmentRestBinding
    private var countdownTimer: CountDownTimer? = null
    private val restTimeInMillis = 2 * 60 * 1000L // 2 минуты

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Запускаем таймер на 2 минуты
        startRestTimer()

        // Кнопка для пропуска отдыха
        binding.btnSkipRest.setOnClickListener {
            skipRest()
        }
    }

    // Функция для запуска таймера
    private fun startRestTimer() {
        countdownTimer = object : CountDownTimer(restTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Обновляем текст таймера каждую секунду
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvRestTimer.text = formatTime(secondsRemaining)
            }

            override fun onFinish() {
                // Когда таймер заканчивается, возвращаемся на ExerciseCameraFragment
                navigateBackToExerciseCamera()
            }
        }.start()
    }

    // Пропуск отдыха
    private fun skipRest() {
        countdownTimer?.cancel()
        navigateBackToExerciseCamera()
    }

    // Переход обратно на ExerciseCameraFragment
    private fun navigateBackToExerciseCamera() {
        findNavController().navigate(R.id.action_restFragment_to_exerciseCameraFragment)
    }

    // Форматирование времени в MM:SS
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secondsRemaining = seconds % 60
        return String.format("%02d:%02d", minutes, secondsRemaining)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Останавливаем таймер, если пользователь ушёл с фрагмента
        countdownTimer?.cancel()
    }
}
