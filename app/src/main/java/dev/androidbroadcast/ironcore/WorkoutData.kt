package dev.androidbroadcast.ironcore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class WorkoutData(
    val workoutLevels: List<WorkoutLevel>
)

data class WorkoutLevel(
    val level: String,
    val weeks: List<Week>
)

data class Week(
    val days: List<Day>,
    val week: Int
)

data class Day(
    val day: Int,
    val exercises: List<Exercise>
)

@Parcelize
data class Exercise(
    val name: String,
    val reps: Int?,
    val sec: Int?,
    val sets: Int,
    val videoUrl: String,
    var currentSet: Int = 0, // Текущий подход
    var currentReps: Int = 0, // Текущие повторения
    var isCompleted: Boolean = false
):Parcelable