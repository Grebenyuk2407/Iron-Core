package dev.androidbroadcast.ironcore

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
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
import android.Manifest
import dev.androidbroadcast.ironcore.databinding.ExerciseFragmentBinding

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

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Проверка разрешений на камеру
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // Если разрешение уже есть, запускаем камеру
            startCamera()
        }

        // Наблюдаем за текущим упражнением
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

            // Настроить Preview и ImageAnalyzer (как в вашем коде)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
            }

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

    // Обработчик разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение получено, запускаем камеру
                    startCamera()
                } else {
                    // Разрешение не получено, показываем сообщение
                    Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                imageProxy.close() // Не забываем закрывать imageProxy после использования
            }
            .addOnFailureListener { e ->
                Log.e("ExerciseCameraFragment", "Pose detection failed", e)
                callback(null)
                imageProxy.close() // Закрываем imageProxy даже при ошибке
            }
    }

    // Создание детектора поз
    private val poseDetector: PoseDetector by lazy {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // STREAM_MODE для анализа в реальном времени
            .build()

        PoseDetection.getClient(options)
    }
}

