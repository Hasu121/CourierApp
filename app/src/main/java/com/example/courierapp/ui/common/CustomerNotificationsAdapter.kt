package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.NotificationItem
import com.example.courierapp.databinding.ItemCustomerNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerNotificationsAdapter(
    private var items: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<CustomerNotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(
        private val binding: ItemCustomerNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvNotificationTitle.text = item.title
            binding.tvNotificationBody.text = item.body
            binding.tvReadStatus.text = if (item.isRead) "Read" else "Unread"

            val formattedDate = if (item.createdAt > 0L) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(item.createdAt))
            } else {
                "Unknown time"
            }

            binding.tvNotificationTime.text = formattedDate

            binding.rootNotification.alpha = if (item.isRead) 0.65f else 1.0f

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemCustomerNotificationBinding.inflate(
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