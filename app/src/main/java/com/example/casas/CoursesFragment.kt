package com.example.casas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CoursesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var coursesAdapter: CoursesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)

        // Set up toolbar
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Courses"

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.coursesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Dummy course data
        val courseList = listOf(
            Courses("High Performance Computing Technology", "HPC-8115", attendanceInfo = "Attendance data 1"),
            Courses("Artificial Intelligence", "AI-1023", attendanceInfo = "Attendance data 2"),
            Courses("Data Science", "DS-101", attendanceInfo = "Attendance data 3"),
            Courses("Machine Learning", "ML-202", attendanceInfo = "Attendance data 4")
        )
        coursesAdapter = CoursesAdapter(requireContext(), courseList)
        recyclerView.adapter = coursesAdapter

        return view
    }
}
