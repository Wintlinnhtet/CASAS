package com.example.casas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CoursesAdapter(
    private val courses: List<Courses>
) : RecyclerView.Adapter<CoursesAdapter.CoursesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_cardview, parent, false)
        return CoursesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoursesViewHolder, position: Int) {
        val course = courses[position]
        holder.courseTitle.text = course.courseTitle
        holder.courseId.text = course.courseId

    }

    override fun getItemCount(): Int {
        return courses.size
    }

    class CoursesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseTitle: TextView = itemView.findViewById(R.id.course)
        val courseId: TextView = itemView.findViewById(R.id.courseId)
    }
}
