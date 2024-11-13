package com.example.udhstudentapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChecklistFragment : Fragment() {

    private lateinit var courseEditText: EditText
    private lateinit var addCourseButton: Button
    private lateinit var courseSpinner: Spinner
    private lateinit var scanButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var cameraProvider: ProcessCameraProvider

    private var isScanning = false
    private val courses = mutableListOf<String>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_checklist, container, false)

        courseEditText = view.findViewById(R.id.courseEditText)
        addCourseButton = view.findViewById(R.id.addCourseButton)
        courseSpinner = view.findViewById(R.id.courseSpinner)
        scanButton = view.findViewById(R.id.scanButton)

        loadCourses()
        db = FirebaseFirestore.getInstance()

        scanButton.setOnClickListener {
            toggleCamera()
        }

        addCourseButton.setOnClickListener {
            addCourse()
        }

        return view
    }

    private fun loadCourses() {
        firestore.collection("cursos")
            .get()
            .addOnSuccessListener { result ->
                courses.clear()
                for (document in result) {
                    val courseName = document.getString("name")
                    if (courseName != null) {
                        courses.add(courseName)
                    }
                }
                updateCourseSpinner()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting courses: $exception")
            }
    }

    private fun addCourse() {
        val courseName = courseEditText.text.toString().trim()
        if (courseName.isNotEmpty()) {
            db.collection("cursos").document(courseName)
                .set(mapOf("name" to courseName))
                .addOnSuccessListener {
                    Toast.makeText(context, "Curso añadido: $courseName", Toast.LENGTH_SHORT).show()
                    courseEditText.text.clear()
                    loadCourses()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al añadir curso: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Por favor ingresa un nombre de curso", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCourseSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapter
    }

    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun toggleCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1001)
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val previewView = view?.findViewById<PreviewView>(R.id.previewView)

            if (isScanning) {
                // Detener el escaneo y ocultar la vista previa
                cameraProvider.unbindAll()
                previewView?.visibility = View.GONE
                isScanning = false
            } else {
                // Iniciar el escaneo y mostrar la vista previa
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), BarcodeAnalyzer())
                    }

                try {
                    cameraProvider.unbindAll() // Asegúrate de desactivar cualquier caso de uso anterior
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                    previewView?.visibility = View.VISIBLE
                    isScanning = true
                } catch (exc: Exception) {
                    Log.e("CameraX", "Error al vincular casos de uso", exc)
                    Toast.makeText(context, "Error al iniciar la cámara: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        @androidx.annotation.OptIn(ExperimentalGetImage::class)
        override fun analyze(image: ImageProxy) {
            val mediaImage = image.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient()

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            handleBarcode(barcode)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MLKit", "Error al escanear: ${e.message}")
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }
        }
    }

    private fun handleBarcode(barcode: Barcode) {
        val studentEmail = barcode.displayValue
        val courseName = courseSpinner.selectedItem.toString()
        if (studentEmail != null) {
            saveAttendance(studentEmail, courseName)
        }
    }

    private fun saveAttendance(email: String, course: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val attendanceData = hashMapOf(
            "email" to email,
            "timestamp" to currentTime
        )

        firestore.collection("asistencias")
            .document(course)
            .collection(currentDate)
            .document(email)
            .set(attendanceData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Asistencia guardada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al guardar asistencia", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
