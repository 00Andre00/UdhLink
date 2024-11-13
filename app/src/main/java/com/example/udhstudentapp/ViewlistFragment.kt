package com.example.udhstudentapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.udhstudentapp.adapters.AttendanceAdapter
import com.example.udhstudentapp.models.Attendance
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewlistFragment : Fragment() {

    private lateinit var courseSpinner: Spinner
    private lateinit var selectDateButton: Button
    private lateinit var viewButton: Button
    private lateinit var attendanceRecyclerView: RecyclerView
    private lateinit var attendanceList: MutableList<Attendance>
    private lateinit var adapter: AttendanceAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var courseList: MutableList<String>
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewlist, container, false)

        courseSpinner = view.findViewById(R.id.courseSpinner)
        selectDateButton = view.findViewById(R.id.selectDateButton)
        viewButton = view.findViewById(R.id.viewButton)
        attendanceRecyclerView = view.findViewById(R.id.attendanceRecyclerView)
        attendanceRecyclerView.layoutManager = LinearLayoutManager(context)

        attendanceList = mutableListOf()
        adapter = AttendanceAdapter(attendanceList)
        attendanceRecyclerView.adapter = adapter

        courseList = mutableListOf()
        db = FirebaseFirestore.getInstance()
        loadCourses()

        selectDateButton.setOnClickListener {
            showDatePicker()
        }

        viewButton.setOnClickListener {
            loadAttendance()
        }

        return view
    }

    private fun loadCourses() {
        db.collection("cursos").get()
            .addOnSuccessListener { result ->
                courseList.clear()
                for (document in result) {
                    document.id.let { courseList.add(it) }
                }
                updateSpinner()
            }
            .addOnFailureListener { exception ->
                Log.w("ViewlistFragment", "Error getting courses", exception)
                Toast.makeText(context, "Error al cargar cursos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSpinner() {
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseList)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapterSpinner
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog, // Aplica tu estilo aquí
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                Toast.makeText(context, "Fecha seleccionada: $selectedDate", Toast.LENGTH_SHORT).show()
            },
            year, month, day
        )

        datePickerDialog.show()
    }


    private fun loadAttendance() {
        val selectedCourse = courseSpinner.selectedItem?.toString()

        if (selectedCourse != null && selectedDate != null) {
            db.collection("asistencias").document(selectedCourse)
                .collection(selectedDate!!).get()
                .addOnSuccessListener { result ->
                    attendanceList.clear()
                    if (result.isEmpty) {
                        Toast.makeText(context, "No hay asistencias para esta fecha", Toast.LENGTH_SHORT).show()
                    } else {
                        for (document in result) {
                            val email = document.id
                            val timestamp = document.getString("timestamp") ?: continue
                            attendanceList.add(Attendance(email, timestamp))
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("ViewlistFragment", "Error getting attendance", exception)
                    Toast.makeText(context, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Por favor selecciona un curso y una fecha", Toast.LENGTH_SHORT).show()
        }
    }
}
