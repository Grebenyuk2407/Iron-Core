package dev.androidbroadcast.ironcore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private val exercises: List<Exercise>) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.workoutName)
        val setsRepsOrSec: TextView = itemView.findViewById(R.id.setsRepsOrSec)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_day, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val exercise = exercises[position]

        holder.workoutName.text = exercise.name

        // Определяем, что отображать: повторения или секунды
        val setsText = "Sets: ${exercise.sets}"
        val repsOrSecText = exercise.reps?.let { "Reps: $it" } ?: exercise.sec?.let { "Sec: $it" }

        holder.setsRepsOrSec.text = "$setsText, ${repsOrSecText ?: ""}"
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}

