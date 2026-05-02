package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.ItemDriverDeliveryHistoryBinding
import com.example.courierapp.utils.StatusFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriverDeliveryHistoryAdapter(
    private var items: List<Booking>,
    private var customerNames: Map<String, String>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<DriverDeliveryHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(
        private val binding: ItemDriverDeliveryHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Booking) {
            val customerName = customerNames[item.customerId] ?: "Unknown Customer"

            binding.tvStatus.text = "Status: ${StatusFormatter.formatStatus(item.status)}"
            binding.tvCustomer.text = "Customer: $customerName"
            binding.tvPickup.text = "Pickup: ${item.pickupAddress}"
            binding.tvDrop.text = "Drop: ${item.dropAddress}"

            val fare = if (item.finalFare > 0.0) item.finalFare else item.estimatedFare
            binding.tvFareDistance.text =
                "Fare: ৳${fare.toInt()} | Distance: ${"%.2f".format(item.distanceKm)} km"

            val dateText = if (item.updatedAt > 0L) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(item.updatedAt))
            } else {
                "Unknown time"
            }

            binding.tvDeliveredAt.text = "Delivered At: $dateText"

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemDriverDeliveryHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Booking>, newCustomerNames: Map<String, String>) {
        items = newItems
        customerNames = newCustomerNames
        notifyDataSetChanged()
    }
}