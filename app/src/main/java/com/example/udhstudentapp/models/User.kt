package com.example.udhstudentapp.models

data class User(
    val uid: String = "",      // UID del documento en Firestore
    val email: String = "",
    val userType: String = ""
)
