package com.example.casas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UpComingClassAdapter(private val upComingClass: List<UpComingClass>) :
        RecyclerView.Adapter<UpComingClassAdapter.UpComingClassViewHolder>() {

    inner class UpComingClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subject: TextView = itemView.findViewById(R.id.subject)
        val room: TextView = itemView.findViewById(R.id.place)
        val teacher: TextView = itemView.findViewById(R.id.teacher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpComingClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view, parent, false)
        return UpComingClassViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UpComingClassViewHolder,
        position: Int
    ) {
        holder.subject.text = upComingClass[position].subject
        holder.room.text = upComingClass[position].roomNo
        holder.teacher.text = upComingClass[position].teacherName
    }

    override fun getItemCount(): Int = upComingClass.size
}
