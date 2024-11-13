package com.example.udhstudentapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var signupRedirectText: TextView
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Verificar si el usuario ya ha iniciado sesión
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si ya hay un usuario autenticado, ir directamente al MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Initialize views
        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signupRedirectText)

        // Set up login button click listener
        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val pass = loginPassword.text.toString()

            when {
                email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    if (pass.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        loginPassword.error = "Empty fields are not allowed"
                    }
                }
                email.isEmpty() -> {
                    loginEmail.error = "Empty fields are not allowed"
                }
                else -> {
                    loginEmail.error = "Please enter correct email"
                }
            }
        }

        // Set up sign up redirect click listener
        signupRedirectText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
