package com.example.udhstudentapp

import QrFragment
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class UserType {
    alumno, profesor, administrador
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var userType: UserType = UserType.alumno // Valor por defecto
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Si no hay un usuario autenticado, redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad actual para que no se pueda volver
        } else {
            // Si el usuario está autenticado, cargar la UI principal
            setContentView(R.layout.activity_main)
            initializeUI()

            // Llama a Firestore para obtener el tipo de usuario
            getUserTypeFromFirestore()
        }
    }

    private fun initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        if (supportFragmentManager.findFragmentById(R.id.content_frame) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        // Titulo
        supportActionBar?.title = "Inicio"
    }

    private fun getUserTypeFromFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userTypeString =
                            document.getString("userType")?.toLowerCase() ?: "alumno"
                        userType = when (userTypeString) {
                            "profesor" -> UserType.profesor
                            "administrador" -> UserType.administrador
                            else -> UserType.alumno
                        }
                        // Después de obtener el tipo de usuario, cargar dinámicamente el menú
                        adjustMenuItemsVisibility()
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun adjustMenuItemsVisibility() {
        val menu = navigationView.menu
        menu.clear() // Limpiar el menú antes de agregar los elementos

        // Agregar los ítems principales
        val mainItemsGroup = menu.addSubMenu("") // Sin título para evitar "Opciones"
        mainItemsGroup.add(Menu.NONE, R.id.nav_home, Menu.NONE, "Inicio").setIcon(R.drawable.baseline_home_24)

        when (userType) {
            UserType.alumno -> {
                mainItemsGroup.add(Menu.NONE, R.id.nav_qr, Menu.NONE, "Mi QR")
                    .setIcon(R.drawable.baseline_qr_code_24)
            }

            UserType.profesor -> {
                mainItemsGroup.add(Menu.NONE, R.id.nav_scan, Menu.NONE, "Generar asistencias")
                    .setIcon(R.drawable.baseline_checklist_24)
                mainItemsGroup.add(Menu.NONE, R.id.nav_list, Menu.NONE, "Ver asistencias")
                    .setIcon(R.drawable.baseline_view_list_24)
            }

            UserType.administrador -> {
                mainItemsGroup.add(Menu.NONE, R.id.nav_modify_users, Menu.NONE, "Modificar usuarios")
                    .setIcon(R.drawable.baseline_mode_edit_24)
            }
        }

        // Android ya crea una línea divisoria automáticamente entre los grupos
        // Agregar el item de "Logout" al final en un grupo diferente
        menu.add(Menu.NONE, R.id.nav_logout, Menu.NONE, "Logout")
            .setIcon(R.drawable.baseline_logout_24)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, HomeFragment())
                    .commit()
                supportActionBar?.title = "Inicio"
            }

            R.id.nav_qr -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, QrFragment())
                    .commit()
                supportActionBar?.title = "Mi QR"
            }

            R.id.nav_scan -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, ChecklistFragment())
                    .commit()
                supportActionBar?.title = "Generar asistencias"
            }

            R.id.nav_list -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, ViewlistFragment())
                    .commit()
                supportActionBar?.title = "Asistencias"
            }

            R.id.nav_modify_users -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, ModifyUsersFragment())
                    .commit()
                supportActionBar?.title = "Modificar usuarios"
            }

            R.id.nav_logout -> {
                // Cerrar sesión de Firebase
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                // Redirigir al LoginActivity después de cerrar sesión
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Cierra la actividad actual para evitar que el usuario vuelva a MainActivity
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
