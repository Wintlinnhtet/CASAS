package com.example.casas

data class Courses(
    val courseTitle: String,
    val courseId: String,
    val university: String = "University of Information Technology",
    val attendanceInfo: String // example data for AttendanceActivity
)
