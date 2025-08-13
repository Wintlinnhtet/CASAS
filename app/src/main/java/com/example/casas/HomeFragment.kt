package com.example.casas

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false) // Replace with your actual layout file name
        val recyclerView = view.findViewById<RecyclerView>(R.id.upComingRecyclerView)
        val sampleList = listOf(
            UpComingClass("Math Class", "Starts at 9:00 AM", "Mya Mya"),
            UpComingClass("Science Class", "Starts at 11:00 AM", "Aye Aye"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("English Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("Last above Class", "Starts at 1:00 PM", "Tun Tun"),
            UpComingClass("Last Class", "Starts at 1:00 PM", "Tun Tun"),
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = UpComingClassAdapter(sampleList)

        val current = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d, MMM, yyy")
        val formatted = current.format(formatter)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        dateTextView.text = formatted

        return view
    }
}
