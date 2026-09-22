package com.example.campus_companion_prog.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.databinding.ItemNotificationBinding

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isUnread: Boolean,
    val iconRes: Int
)

class NotificationAdapter(
    private var items: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvNotificationTitle.text = item.title
            tvNotificationMessage.text = item.message
            tvNotificationTime.text = item.time
            ivNotificationIcon.setImageResource(item.iconRes)

            viewUnreadDot.visibility = if (item.isUnread) View.VISIBLE else View.GONE

            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<NotificationItem>) {
        items = newList
        notifyDataSetChanged()
    }
}