package dev.androidbroadcast.ironcore

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

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int?,
    val sec: Int?,
    val equipment: String?
)