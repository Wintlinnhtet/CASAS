package com.example.casas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimetableAdapter(
    private val classList: List<Timetable>
) : RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timetable_card, parent, false)
        return TimetableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimetableViewHolder, position: Int) {
        val item = classList[position]

        holder.courseName.text = item.courseName
        holder.courseId.text = item.courseId
        holder.roomNo.text = item.roomNo
        holder.courseTeacher.text = item.courseTeacher
        holder.classStartTime.text = item.startTime
        holder.classEndTime.text = item.endTime
    }

    override fun getItemCount(): Int = classList.size

    class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.courseName)
        val courseId: TextView = itemView.findViewById(R.id.courseId)
        val roomNo: TextView = itemView.findViewById(R.id.roomNo)
        val courseTeacher: TextView = itemView.findViewById(R.id.courseTeacher)
        val classStartTime: TextView = itemView.findViewById(R.id.classStartTime)
        val classEndTime: TextView = itemView.findViewById(R.id.classEndTime)
    }
}
