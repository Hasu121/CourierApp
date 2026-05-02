package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.ItemBookingHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.courierapp.utils.StatusFormatter

class BookingHistoryAdapter(
    private var items: List<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingHistoryAdapter.BookingHistoryViewHolder>() {

    inner class BookingHistoryViewHolder(
        private val binding: ItemBookingHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Booking) {
            binding.tvStatus.text = "Status: ${StatusFormatter.formatStatus(item.status)}"
            binding.tvBookingType.text = "Type: ${item.bookingType}"
            binding.tvPickup.text = "Pickup: ${item.pickupAddress}"
            binding.tvDrop.text = "Drop: ${item.dropAddress}"
            binding.tvFareDistance.text =
                "Fare: ৳${item.estimatedFare.toInt()} | Distance: ${"%.2f".format(item.distanceKm)} km"

            val formattedDate = if (item.createdAt > 0L) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(item.createdAt))
            } else {
                "Unknown time"
            }

            binding.tvCreatedAt.text = formattedDate

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingHistoryViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingHistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Booking>) {
        items = newItems
        notifyDataSetChanged()
    }
}