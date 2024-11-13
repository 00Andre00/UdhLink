package com.example.udhstudentapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.udhstudentapp.adapters.UserAdapter
import com.example.udhstudentapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ModifyUsersFragment : Fragment() {

    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentUserUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_users, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_users)
        recyclerView.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(listOf()) { user ->
            showUserModificationDialog(user)
        }
        recyclerView.adapter = userAdapter

        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        fetchUsers()

        return view
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.map { document ->
                    val email = document.getString("email") ?: ""
                    val userType = document.getString("userType") ?: "alumno"
                    val uid = document.id
                    User(uid, email, userType) // Usar uid como identificador de usuario
                }
                userAdapter.submitList(userList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al obtener los usuarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUserModificationDialog(user: User) {
        // Evitar que el administrador cambie su propio tipo de usuario
        if (user.uid == currentUserUid) {
            Toast.makeText(context, "No puedes modificar tu propio tipo de usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Opciones de tipo de usuario (sin la opción "administrador")
        val userTypes = arrayOf("alumno", "profesor")
        val currentTypeIndex = userTypes.indexOf(user.userType)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modificar tipo de usuario")
            .setSingleChoiceItems(userTypes, currentTypeIndex) { dialog, which ->
                val newUserType = userTypes[which]
                modifyUserType(user.uid, newUserType) // Modificar el tipo de usuario en Firestore
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun modifyUserType(userId: String, newUserType: String) {
        db.collection("users").document(userId)
            .update("userType", newUserType)
            .addOnSuccessListener {
                Toast.makeText(context, "Tipo de usuario actualizado", Toast.LENGTH_SHORT).show()
                fetchUsers() // Actualizar la lista de usuarios después de modificar
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al actualizar tipo de usuario", Toast.LENGTH_SHORT).show()
            }
    }
}
