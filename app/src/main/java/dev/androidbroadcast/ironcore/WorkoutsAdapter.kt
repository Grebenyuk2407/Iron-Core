package dev.androidbroadcast.ironcore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private val items: List<DayExerciseItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_DAY_HEADER = 0
    private val TYPE_EXERCISE_ITEM = 1

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DayExerciseItem.DayHeader -> TYPE_DAY_HEADER
            is DayExerciseItem.ExerciseItem -> TYPE_EXERCISE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DAY_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_header, parent, false)
                DayHeaderViewHolder(view)
            }
            TYPE_EXERCISE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_day, parent, false)
                ExerciseViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DayHeaderViewHolder -> holder.bind((items[position] as DayExerciseItem.DayHeader).title)
            is ExerciseViewHolder -> holder.bind((items[position] as DayExerciseItem.ExerciseItem).exercise)
        }
    }

    override fun getItemCount(): Int {
        return items.indices.count()
    }

    class DayHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTitle: TextView = itemView.findViewById(R.id.dayTitle)

        fun bind(title: String) {
            dayTitle.text = title
        }
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workoutName: TextView = itemView.findViewById(R.id.workoutName)
        private val setsRepsOrSec: TextView = itemView.findViewById(R.id.setsRepsOrSec)

        fun bind(exercise: Exercise) {
            workoutName.text = exercise.name

            val setsText = "Sets: ${exercise.sets}"
            val repsOrSecText = exercise.reps?.let { "Reps: $it" } ?: exercise.sec?.let { "Sec: $it" }
            setsRepsOrSec.text = "$setsText, ${repsOrSecText ?: ""}"
        }
    }
}


