package com.example.casas

import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AttendanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attendance_view)

        // ListView
        val listView: ListView = findViewById(R.id.attendanceListView)

        // Set course name bar
        val courseNameBar = findViewById<TextView>(R.id.courseNameBar)
        val courseTitle = intent.getStringExtra("courseTitle") ?: "Course"
        courseNameBar.text = courseTitle

        // Sample attendance data per course
        val attendanceList = when(courseTitle) {
            "High Performance Computing Technology" -> listOf(
                Attendance("2025-08-28", "09:00 AM", "10:00 AM", "Present"),
                Attendance("2025-08-29", "09:00 AM", "10:00 AM", "Absent")
            )
            "Artificial Intelligence" -> listOf(
                Attendance("2025-08-28", "10:00 AM", "11:00 AM", "Excuse"),
                Attendance("2025-08-29", "10:00 AM", "11:00 AM", "Present")
            )
            else -> listOf(
                Attendance("2025-08-28", "08:00 AM", "09:00 AM", "Present")
            )
        }

        // Adapter
        val adapter = AttendanceAdapter(this, attendanceList)
        listView.adapter = adapter
    }
}
