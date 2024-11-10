package dev.androidbroadcast.ironcore


import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.atan2
import kotlin.math.abs

class ExerciseAnalyzer {

    // Функция для расчета угла между тремя точками тела (PoseLandmark)
    fun calculateAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        val firstPointPosition = firstPoint.position
        val midPointPosition = midPoint.position
        val lastPointPosition = lastPoint.position

        val radians = Math.atan2((lastPointPosition.y - midPointPosition.y).toDouble(),
            (lastPointPosition.x - midPointPosition.x).toDouble()
        ) -
                Math.atan2((firstPointPosition.y - midPointPosition.y).toDouble(),
                    (firstPointPosition.x - midPointPosition.x).toDouble()
                )
        var angle = Math.abs(Math.toDegrees(radians))
        if (angle > 180) {
            angle = 360.0 - angle
        }
        return angle
    }

    // Логика для каждого упражнения
    fun countRepetitions(pose: Pose, exerciseName: String): Int {
        return when (exerciseName) {
            "Australian Pull Up" -> countAustralianPullUp(pose)
            "Dips on parallel bars" -> countDipsOnParallelBars(pose)
            "Push up from floor" -> countPushUpFromFloor(pose)
            "Hanging Knee Raises" -> countHangingKneeRaises(pose)
            else -> 0
        }
    }

    private var australianPullUpReps = 0
    private var isPullingUp = false

    private fun countAustralianPullUp(pose: Pose): Int {
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        if (leftElbow != null && leftShoulder != null && leftWrist != null) {
            val angle = calculateAngle(
                leftShoulder, leftElbow, leftWrist
            )

            if (angle < 90 && !isPullingUp) {
                isPullingUp = true
            }

            if (angle > 160 && isPullingUp) {
                australianPullUpReps++
                isPullingUp = false
                Log.d("Exercise", "Australian Pull Up Reps: $australianPullUpReps")
            }
        }
        return australianPullUpReps
    }

    private var dipsReps = 0
    private var isDipping = false

    private fun countDipsOnParallelBars(pose: Pose): Int {
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        if (rightElbow != null && rightShoulder != null && rightWrist != null) {
            val angle = calculateAngle(
                rightShoulder, rightElbow, rightWrist
            )

            if (angle < 90 && !isDipping) {
                isDipping = true
            }

            if (angle > 160 && isDipping) {
                dipsReps++
                isDipping = false
                Log.d("Exercise", "Dips Reps: $dipsReps")
            }
        }
        return dipsReps
    }

    // Аналогично для остальных упражнений
    private var pushUpReps = 0
    private var isPushing = false

    private fun countPushUpFromFloor(pose: Pose): Int {
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        if (rightElbow != null && rightShoulder != null && rightWrist != null) {
            val angle = calculateAngle(
                rightShoulder, rightElbow, rightWrist
            )

            if (angle < 90 && !isPushing) {
                isPushing = true
            }

            if (angle > 160 && isPushing) {
                pushUpReps++
                isPushing = false
                Log.d("Exercise", "Push Up Reps: $pushUpReps")
            }
        }
        return pushUpReps
    }

    private var kneeRaisesReps = 0
    private var isRaisingKnees = false

    private fun countHangingKneeRaises(pose: Pose): Int {
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        if (leftKnee != null && leftHip != null && leftAnkle != null) {
            val angle = calculateAngle(
                leftHip, leftKnee, leftAnkle
            )

            if (angle < 90 && !isRaisingKnees) {
                isRaisingKnees = true
            }

            if (angle > 160 && isRaisingKnees) {
                kneeRaisesReps++
                isRaisingKnees = false
                Log.d("Exercise", "Knee Raises Reps: $kneeRaisesReps")
            }
        }
        return kneeRaisesReps
    }
}


