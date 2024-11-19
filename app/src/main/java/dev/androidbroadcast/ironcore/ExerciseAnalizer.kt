package dev.androidbroadcast.ironcore


import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.atan2
import kotlin.math.abs

class ExerciseAnalyzer {

    private var australianPullUpReps = 0
    private var isPullingUp = false

    private var dipsReps = 0
    private var isDipping = false

    private var pushUpReps = 0
    private var isPushing = false

    private var kneeRaisesReps = 0
    private var isRaisingKnees = false

    // Функция для расчета угла между тремя точками тела
    fun calculateAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        val firstPointPosition = firstPoint.position
        val midPointPosition = midPoint.position
        val lastPointPosition = lastPoint.position

        val radians = Math.atan2(
            (lastPointPosition.y - midPointPosition.y).toDouble(),
            (lastPointPosition.x - midPointPosition.x).toDouble()
        ) -
                Math.atan2(
                    (firstPointPosition.y - midPointPosition.y).toDouble(),
                    (firstPointPosition.x - midPointPosition.x).toDouble()
                )

        var angle = Math.abs(Math.toDegrees(radians))
        if (angle > 180) {
            angle = 360.0 - angle
        }
        return angle
    }

    // Логика для анализа повторений в зависимости от упражнения
    fun analyzePose(pose: Pose, exerciseName: String): Boolean {
        return when (exerciseName) {
            "Australian Pull Up" -> analyzeAustralianPullUp(pose)
            "Dips on parallel bars" -> analyzeDipsOnParallelBars(pose)
            "Push up from floor" -> analyzePushUpFromFloor(pose)
            "Hanging Knee Raises" -> analyzeHangingKneeRaises(pose)
            else -> false
        }
    }

    // Логика для каждого упражнения

    // Австралийские подтягивания
    private fun analyzeAustralianPullUp(pose: Pose): Boolean {
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        if (leftElbow != null && leftShoulder != null && leftWrist != null) {
            val angle = calculateAngle(leftShoulder, leftElbow, leftWrist)

            if (angle < 90 && !isPullingUp) {
                isPullingUp = true
            }

            if (angle > 160 && isPullingUp) {
                australianPullUpReps++
                isPullingUp = false
                return true // Подход завершён
            }
        }
        return false
    }

    // Отжимания на параллельных брусьях
    private fun analyzeDipsOnParallelBars(pose: Pose): Boolean {
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        if (rightElbow != null && rightShoulder != null && rightWrist != null) {
            val angle = calculateAngle(rightShoulder, rightElbow, rightWrist)

            if (angle < 90 && !isDipping) {
                isDipping = true
            }

            if (angle > 160 && isDipping) {
                dipsReps++
                isDipping = false
                return true
            }
        }
        return false
    }

    // Отжимания от пола
    private fun analyzePushUpFromFloor(pose: Pose): Boolean {
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        if (rightElbow != null && rightShoulder != null && rightWrist != null) {
            val angle = calculateAngle(rightShoulder, rightElbow, rightWrist)

            if (angle < 90 && !isPushing) {
                isPushing = true
            }

            if (angle > 160 && isPushing) {
                pushUpReps++
                isPushing = false
                return true
            }
        }
        return false
    }

    // Подъём коленей в висе
    private fun analyzeHangingKneeRaises(pose: Pose): Boolean {
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        if (leftKnee != null && leftHip != null && leftAnkle != null) {
            val angle = calculateAngle(leftHip, leftKnee, leftAnkle)

            if (angle < 90 && !isRaisingKnees) {
                isRaisingKnees = true
            }

            if (angle > 160 && isRaisingKnees) {
                kneeRaisesReps++
                isRaisingKnees = false
                return true
            }
        }
        return false
    }

    // Методы для получения количества повторений
    fun getRepsForExercise(exerciseName: String): Int {
        return when (exerciseName) {
            "Australian Pull Up" -> australianPullUpReps
            "Dips on parallel bars" -> dipsReps
            "Push up from floor" -> pushUpReps
            "Hanging Knee Raises" -> kneeRaisesReps
            else -> 0
        }
    }
}



