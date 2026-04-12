package com.example.edureach1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.edureach1.R
import com.example.edureach1.models.User

class LeaderboardAdapter(
    private var users: List<User>,
    private val currentUserUid: String
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvXp: TextView = view.findViewById(R.id.tvXp)
        val tvLevel: TextView = view.findViewById(R.id.tvLevel)
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val rank = position + 1

        holder.tvRank.text = when (rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "#$rank"
        }

        holder.tvName.text = user.name
        holder.tvXp.text = "${user.xp} XP"
        holder.tvLevel.text = "Lvl ${(user.xp / 100) + 1}"

        val initials = user.name
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it[0].uppercaseChar().toString() }
        holder.tvInitials.text = initials

        // Highlight current user
        if (user.uid == currentUserUid) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_light)
            )
            holder.tvName.text = "${user.name} (You)"
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
            )
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
