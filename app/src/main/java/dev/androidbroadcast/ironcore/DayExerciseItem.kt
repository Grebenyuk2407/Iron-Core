package dev.androidbroadcast.ironcore

sealed class DayExerciseItem {
    data class DayHeader(val title: String) : DayExerciseItem()
    data class ExerciseItem(val exercise: Exercise) : DayExerciseItem()
}
