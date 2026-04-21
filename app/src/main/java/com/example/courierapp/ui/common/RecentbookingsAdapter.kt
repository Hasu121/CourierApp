package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.ItemRecentBookingBinding

class RecentBookingsAdapter(
    private var items: List<Booking>
) : RecyclerView.Adapter<RecentBookingsAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(private val binding: ItemRecentBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Booking) {
            binding.tvBookingType.text = "Type: ${item.bookingType}"
            binding.tvAddresses.text = "From: ${item.pickupAddress}\nTo: ${item.dropAddress}"
            binding.tvStatus.text = "Status: ${item.status}"
            binding.tvReceiver.text = "Receiver: ${item.receiverName}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemRecentBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Booking>) {
        items = newItems
        notifyDataSetChanged()
    }
}