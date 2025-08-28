package com.example.casas

data class Timetable(
    val day: String,
    val courseName: String,
    val courseId: String,
    val roomNo: String,
    val courseTeacher: String,
    val startTime: String,
    val endTime: String
)
