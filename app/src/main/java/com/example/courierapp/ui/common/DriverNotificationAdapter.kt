package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.NotificationItem
import com.example.courierapp.databinding.ItemDriverNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriverNotificationsAdapter(
    private var items: List<NotificationItem>
) : RecyclerView.Adapter<DriverNotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(
        private val binding: ItemDriverNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvTitle.text = item.title
            binding.tvBody.text = item.body

            val formattedDate = if (item.createdAt > 0L) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(item.createdAt))
            } else {
                "Unknown time"
            }

            binding.tvCreatedAt.text = formattedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemDriverNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NotificationItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}