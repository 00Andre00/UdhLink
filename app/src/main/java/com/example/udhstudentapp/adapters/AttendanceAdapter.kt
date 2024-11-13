package com.example.udhstudentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.udhstudentapp.R
import com.example.udhstudentapp.models.Attendance

class AttendanceAdapter(private val attendanceList: List<Attendance>) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attendance = attendanceList[position]

        holder.emailTextView.text = "Alumno: ${attendance.email}"
        holder.timestampTextView.text = "Hora: ${attendance.timestamp}"
    }

    override fun getItemCount() = attendanceList.size

}
