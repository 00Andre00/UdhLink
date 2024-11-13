package com.example.udhstudentapp

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupButton: Button
    private lateinit var loginRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() // Inicializar Firestore

        signupEmail = findViewById(R.id.signup_email)
        signupPassword = findViewById(R.id.signup_password)
        signupButton = findViewById(R.id.signup_button)
        loginRedirectText = findViewById(R.id.loginRedirectText)

        signupButton.setOnClickListener {
            val email = signupEmail.text.toString().trim()
            val pass = signupPassword.text.toString().trim()

            if (email.isEmpty()) {
                signupEmail.error = "El email no puede estar vacío"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                signupPassword.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        // Obtener el ID del usuario recién creado
                        val userId = auth.currentUser?.uid

                        // Crear el mapa de datos del usuario
                        val user = hashMapOf(
                            "email" to email,
                            "userType" to "alumno" // Asignar el rol "alumno" por defecto
                        )

                        // Guardar los datos del usuario en Firestore
                        if (userId != null) {
                            firestore.collection("users").document(userId)
                                .set(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(this, "Registro correcto", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                    } else {
                                        Toast.makeText(this, "Error al guardar los datos en Firestore", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registro incorrecto: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
