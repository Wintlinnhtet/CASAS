package com.example.casas

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.graphics.Color
import androidx.core.content.ContextCompat


class AttendanceAdapter(
    private val context: Context,
    private val dataList: List<Attendance>
) : BaseAdapter() {

    override fun getCount(): Int = dataList.size

    override fun getItem(position: Int): Any = dataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.attendance_card, parent, false)

        val dateText = view.findViewById<TextView>(R.id.attendance_date)
        val startTimeText = view.findViewById<TextView>(R.id.attendance_startTime)
        val endTimeText = view.findViewById<TextView>(R.id.attendance_endTime)
        val statusText = view.findViewById<TextView>(R.id.attendance_status)

        val attendance = dataList[position]
        dateText.text = attendance.date
        startTimeText.text = attendance.startTime
        endTimeText.text = attendance.endTime
        statusText.text = "Status: ${attendance.status}"
        // Set color based on status
        when (attendance.status.lowercase()) {
            "present" -> statusText.setTextColor(ContextCompat.getColor(context, R.color.green))
            "absent" -> statusText.setTextColor(ContextCompat.getColor(context, R.color.red))
            "excuse" -> statusText.setTextColor(ContextCompat.getColor(context, R.color.yellow))
            else -> statusText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }


        return view
    }
}
