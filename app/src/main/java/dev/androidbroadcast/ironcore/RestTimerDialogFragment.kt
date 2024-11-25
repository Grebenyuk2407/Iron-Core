package dev.androidbroadcast.ironcore

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import dev.androidbroadcast.ironcore.databinding.DialogRestTimerBinding

class RestTimerDialogFragment : DialogFragment() {

    private lateinit var binding: DialogRestTimerBinding
    private var countdownTime: Long = 60_000L // 60 секунд
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogRestTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Стартуем таймер обратного отсчета
        startRestTimer()

        binding.btnSkipRest.setOnClickListener {
            timer?.cancel()
            dismiss() // Закрываем диалог досрочно
        }
    }

    private fun startRestTimer() {
        timer = object : CountDownTimer(countdownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvTimer.text = "$secondsRemaining секунд"
            }

            override fun onFinish() {
                dismiss() // Закрываем диалог по завершении
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel() // Останавливаем таймер при закрытии диалога
    }
}

