package dev.androidbroadcast.ironcore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.ironcore.databinding.FragmentWorkoutCompleteBinding


@AndroidEntryPoint
class WorkoutCompleteFragment : Fragment() {

    private lateinit var binding: FragmentWorkoutCompleteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkoutCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем текст завершения
        binding.tvWorkoutComplete.text = "Тренировка завершена! Отличная работа!"

        // Обработка нажатия на кнопку "Перейти в профиль"
        binding.btnGoToProfile.setOnClickListener {
            findNavController().navigate(R.id.action_workoutCompleteFragment_to_profileFragment)
        }
    }
}
