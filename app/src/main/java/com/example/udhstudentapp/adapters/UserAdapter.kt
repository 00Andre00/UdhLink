package com.example.udhstudentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.udhstudentapp.R
import com.example.udhstudentapp.models.User

class UserAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userEmail: TextView = itemView.findViewById(R.id.user_email)
        private val userType: TextView = itemView.findViewById(R.id.user_type)

        fun bind(user: User, onUserClick: (User) -> Unit) {
            userEmail.text = user.email
            userType.text = user.userType.capitalize()

            // Listener para el clic en un usuario
            itemView.setOnClickListener { onUserClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_view, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], onUserClick)
    }

    override fun getItemCount() = users.size

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
