package com.example.casas

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CoursesAdapter(
    private val context: Context,
    private val courses: List<Courses>
) : RecyclerView.Adapter<CoursesAdapter.CoursesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursesViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.course_cardview, parent, false)
        return CoursesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoursesViewHolder, position: Int) {
        val course = courses[position]
        holder.courseTitle.text = course.courseTitle
        holder.courseId.text = course.courseId
        holder.university.text = course.university

        holder.continueButton.setOnClickListener {
            // Pass course title and ID to AttendanceActivity
            val intent = Intent(context, AttendanceActivity::class.java).apply {
                putExtra("courseTitle", course.courseTitle)
                putExtra("courseId", course.courseId)
                putExtra("university", course.university)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = courses.size

    class CoursesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseTitle: TextView = itemView.findViewById(R.id.courseName)
        val courseId: TextView = itemView.findViewById(R.id.courseId)
        val university: TextView = itemView.findViewById(R.id.universityTextView)
        val continueButton: Button = itemView.findViewById(R.id.continueButton)
    }
}
