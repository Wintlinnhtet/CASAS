package com.example.casas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AttendanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attendance_view)

        val listView = findViewById<ListView>(R.id.attendanceListView)

        val attendanceList = listOf(
            Attendance("2025-08-21", "09:00 AM", "10:00 AM"),
            Attendance("2025-08-22", "11:00 AM", "12:30 PM"),
            Attendance("2025-08-23", "02:00 PM", "03:00 PM")
        )

        val adapter = AttendanceAdapter(this, attendanceList)
        listView.adapter = adapter
    }
}
