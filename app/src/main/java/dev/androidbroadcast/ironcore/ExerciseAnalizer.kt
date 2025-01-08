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

    private var pullUpReps = 0
    private var isPullingUpOnBar = false

    private var widePushUpReps = 0
    private var isWidePushing = false

    private var hangingSideKneeRaiseReps = 0
    private var isSideRaisingKnees = false

    private var commandoPullUpReps = 0
    private var isCommandoPullingUp = false

    private var diamondPushUpReps = 0
    private var isDiamondPushing = false


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
            "Pull-ups on a bar" -> analyzePullUpOnBar(pose)
            "Wide Push Ups" -> analyzeWidePushUp(pose)
            "Hanging Side Knee Raise" -> analyzeHangingSideKneeRaise(pose)
            "Commando Pull Up" -> analyzeCommandoPullUp(pose)
            "Diamond Push Ups" -> analyzeDiamondPushUp(pose)

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

    private fun analyzePullUpOnBar(pose: Pose): Boolean {
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        if (rightElbow != null && rightShoulder != null && rightWrist != null) {
            // Рассчитываем угол между плечом, локтем и запястьем
            val elbowAngle = calculateAngle(rightShoulder, rightElbow, rightWrist)

            // Когда угол в локте меньше определенного значения, пользователь находится в нижней точке
            if (elbowAngle < 90 && !isPullingUpOnBar) {
                isPullingUpOnBar = true
            }

            // Когда угол в локте превышает определенное значение, пользователь завершает повторение
            if (elbowAngle > 160 && isPullingUpOnBar) {
                pullUpReps++
                isPullingUpOnBar = false
                return true // Повторение завершено
            }
        }
        return false
    }

    private fun analyzeWidePushUp(pose: Pose): Boolean {
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        if (leftElbow != null && leftShoulder != null && leftWrist != null) {
            // Рассчитываем угол между плечом, локтем и запястьем
            val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)

            // Когда угол в локте меньше определенного значения, пользователь находится в нижней точке
            if (elbowAngle < 90 && !isWidePushing) {
                isWidePushing = true
            }

            // Когда угол в локте превышает определенное значение, пользователь завершает повторение
            if (elbowAngle > 160 && isWidePushing) {
                widePushUpReps++
                isWidePushing = false
                return true // Повторение завершено
            }
        }
        return false
    }

    private fun analyzeHangingSideKneeRaise(pose: Pose): Boolean {
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        if (leftKnee != null && leftHip != null && leftAnkle != null) {
            // Рассчитываем угол между бедром, коленом и лодыжкой
            val kneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)

            // Также можем рассматривать угол в тазобедренном суставе для бокового подъема
            val hipAngle = calculateAngle(leftAnkle, leftKnee, leftHip)

            // Когда угол в колене и тазобедренном суставе указывает на начало подъема
            if (kneeAngle < 90 && hipAngle > 45 && !isSideRaisingKnees) {
                isSideRaisingKnees = true
            }

            // Когда колени поднимаются вверх и движение завершено
            if (kneeAngle > 160 && isSideRaisingKnees) {
                hangingSideKneeRaiseReps++
                isSideRaisingKnees = false
                return true // Повторение завершено
            }
        }
        return false
    }

    private fun analyzeCommandoPullUp(pose: Pose): Boolean {
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        // Проверяем, что обе руки определены
        if (leftElbow != null && leftShoulder != null && leftWrist != null &&
            rightElbow != null && rightShoulder != null && rightWrist != null) {

            // Рассчитываем углы на обеих руках
            val leftArmAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
            val rightArmAngle = calculateAngle(rightShoulder, rightElbow, rightWrist)

            // Если угол меньше 90 на одной из рук, начинаем подтягивание
            if ((leftArmAngle < 90 || rightArmAngle < 90) && !isCommandoPullingUp) {
                isCommandoPullingUp = true
            }

            // Если угол больше 160 на обеих руках, подтягивание завершено
            if (leftArmAngle > 160 && rightArmAngle > 160 && isCommandoPullingUp) {
                commandoPullUpReps++
                isCommandoPullingUp = false
                return true // Повторение завершено
            }
        }
        return false
    }

    private fun analyzeDiamondPushUp(pose: Pose): Boolean {
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        // Проверяем, что обе руки определены
        if (leftElbow != null && leftShoulder != null && leftWrist != null &&
            rightElbow != null && rightShoulder != null && rightWrist != null) {

            // Рассчитываем углы на обеих руках
            val leftArmAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
            val rightArmAngle = calculateAngle(rightShoulder, rightElbow, rightWrist)

            // Если угол меньше 90 на одной из рук, начинаем отжимание
            if ((leftArmAngle < 90 || rightArmAngle < 90) && !isDiamondPushing) {
                isDiamondPushing = true
            }

            // Если угол больше 160 на обеих руках, отжимание завершено
            if (leftArmAngle > 160 && rightArmAngle > 160 && isDiamondPushing) {
                diamondPushUpReps++
                isDiamondPushing = false
                return true // Повторение завершено
            }
        }
        return false
    }

}



