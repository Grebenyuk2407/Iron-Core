package dev.androidbroadcast.ironcore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.ironcore.databinding.FragmentExerciseCameraBinding
import java.util.concurrent.Executors

@AndroidEntryPoint
class ExerciseCameraFragment : Fragment() {

    private lateinit var binding: FragmentExerciseCameraBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private val exerciseViewModel: ExerciseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Запускаем камеру
        startCamera()

        // Обновляем UI информацией об упражнении
        exerciseViewModel.currentExercise.observe(viewLifecycleOwner) { exercise ->
            binding.tvExerciseName.text = exercise.name
            binding.tvSetProgress.text = "Set: ${exercise.currentSet}/${exercise.sets}"
            binding.tvRepsProgress.text = "Reps: ${exercise.currentReps}/${exercise.reps}"
        }

        // Слушаем завершение подхода
        exerciseViewModel.setCompleted.observe(viewLifecycleOwner) { setCompleted ->
            if (setCompleted) {
                // Переходим на RestFragment для таймера
                findNavController().navigate(R.id.action_exerciseCamera_to_restFragment)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Настраиваем Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
            }

            // Настраиваем анализ данных
            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    analyzePoseFromImage(imageProxy) { poseResult ->
                        // Логика подсчета повторений и подходов
                        poseResult?.let { it1 -> exerciseViewModel.processPose(it1) }
                    }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("ExerciseCameraFragment", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Создание детектора поз
    private val poseDetector: PoseDetector by lazy {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // STREAM_MODE для анализа в реальном времени
            .build()

        PoseDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzePoseFromImage(imageProxy: ImageProxy, callback: (Pose?) -> Unit) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        // Обрабатываем изображение с помощью детектора поз
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                // Возвращаем результат через callback
                callback(pose)
                imageProxy.close() // Не забывай закрывать imageProxy после использования
            }
            .addOnFailureListener { e ->
                Log.e("ExerciseCameraFragment", "Pose detection failed", e)
                callback(null)
                imageProxy.close() // Закрываем imageProxy даже при ошибке
            }
    }

}
