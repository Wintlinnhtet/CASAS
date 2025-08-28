package com.example.casas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DayFragment : Fragment() {

    companion object {
        private const val ARG_DAY = "day"

        fun newInstance(day: String): DayFragment {
            val fragment = DayFragment()
            val bundle = Bundle()
            bundle.putString(ARG_DAY, day)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var day: String? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        day = arguments?.getString(ARG_DAY)
    }

    private fun getScheduleForDay(day: String?): List<Timetable> {
        val allClasses = listOf(
            Timetable("Mon", "High Performance Computing", "HPC-8115", "R101", "Dr. Aung", "09:00 AM", "10:00 AM"),
            Timetable("Mon", "Artificial Intelligence", "AI-8201", "R102", "Dr. Hla", "10:00 AM", "11:00 AM"),
            Timetable("Tue", "Machine Learning", "ML-8203", "R103", "Dr. Min", "09:00 AM", "10:00 AM"),
            Timetable("Fri", "Database Systems", "DB-8302", "R104", "Dr. Kyaw", "11:00 AM", "12:00 PM"),
            Timetable("Wed", "Database Systems", "DB-8302", "R104", "Dr. Kyaw", "11:00 AM", "12:00 PM"),
            Timetable("Thu", "Database Systems", "DB-8302", "R104", "Dr. Kyaw", "11:00 AM", "12:00 PM")

        )
        return allClasses.filter { it.day.equals(day, ignoreCase = true) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timetable_recyclerview, container, false)

        recyclerView = view.findViewById(R.id.timetableRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TimetableAdapter(getScheduleForDay(day))
        Log.d("DayFragment", "Day: $day, Classes: ${getScheduleForDay(day).size}")

        return view
    }
}
