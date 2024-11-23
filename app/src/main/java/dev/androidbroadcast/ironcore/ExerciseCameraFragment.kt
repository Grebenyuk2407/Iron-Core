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
import androidx.fragment.app.activityViewModels
import dev.androidbroadcast.ironcore.databinding.ExerciseFragmentBinding

@AndroidEntryPoint
class ExerciseCameraFragment : Fragment() {

    private lateinit var binding: FragmentExerciseCameraBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private val exerciseViewModel: ExerciseViewModel by activityViewModels() // Общая ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отображаем информацию о текущем упражнении
        exerciseViewModel.currentExercise.observe(viewLifecycleOwner) { exercise ->
            if (exercise != null) {
                binding.tvExerciseName.text = exercise.name
                binding.tvSetProgress.text = "Set: ${exercise.currentSet}/${exercise.sets}"
                binding.tvRepsProgress.text = "Reps: 0/${exercise.reps}"
            }
        }

        // Обновляем количество повторений на UI
        exerciseViewModel.repsCount.observe(viewLifecycleOwner) { reps ->
            exerciseViewModel.currentExercise.value?.let { exercise ->
                binding.tvRepsProgress.text = "Reps: $reps/${exercise.reps}"
            }
        }

        // Если подход завершен, переходим к следующему экрану
        exerciseViewModel.setCompleted.observe(viewLifecycleOwner) { setCompleted ->
            if (setCompleted) {
                findNavController().navigate(R.id.action_exerciseCamera_to_restFragment)
            }
        }

        startCamera()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).hideBottomNavigation()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as MainActivity).showBottomNavigation()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
            }

            val rotation = binding.cameraPreviewView.display?.rotation ?: Surface.ROTATION_0
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        analyzePoseFromImage(imageProxy) { pose ->
                            pose?.let { exerciseViewModel.processPose(it) }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzePoseFromImage(imageProxy: ImageProxy, callback: (Pose?) -> Unit) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                callback(pose)
                imageProxy.close()
            }
            .addOnFailureListener {
                callback(null)
                imageProxy.close()
            }
    }

    private val poseDetector: PoseDetector by lazy {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }
}


