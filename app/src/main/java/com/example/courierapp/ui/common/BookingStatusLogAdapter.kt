package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.BookingStatusLog
import com.example.courierapp.databinding.ItemBookingStatusLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.courierapp.utils.StatusFormatter

class BookingStatusLogAdapter(
    private var items: List<BookingStatusLog>
) : RecyclerView.Adapter<BookingStatusLogAdapter.LogViewHolder>() {

    inner class LogViewHolder(
        private val binding: ItemBookingStatusLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookingStatusLog) {
            binding.tvStatus.text = "Status: ${StatusFormatter.formatStatus(item.status)}"
            binding.tvNote.text = "Note: ${item.note}"

            val formattedDate = if (item.timestamp > 0L) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(item.timestamp))
            } else {
                "Unknown time"
            }

            binding.tvTime.text = formattedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemBookingStatusLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<BookingStatusLog>) {
        items = newItems
        notifyDataSetChanged()
    }
}