package com.example.casas

class AttendanceAdapter {
    private val context: Context,
    private val dataList: List<Attendance>
    ) : BaseAdapter()
    {

        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.attendance_card, parent, false)

            val dateText = view.findViewById<TextView>(R.id.attendance_date)
            val startTimeText = view.findViewById<TextView>(R.id.attendance_startTime)
            val endTimeText = view.findViewById<TextView>(R.id.attendance_endTime)

            val attendance = dataList[position]
            dateText.text = attendance.date
            startTimeText.text = attendance.startTime
            endTimeText.text = attendance.endTime

            return view
        }
    }
}