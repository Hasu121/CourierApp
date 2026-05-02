package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.ItemAvailableJobBinding

class AvailableJobsAdapter(
    private var items: List<Booking>,
    private val onAcceptClick: (Booking) -> Unit
) : RecyclerView.Adapter<AvailableJobsAdapter.JobViewHolder>() {

    inner class JobViewHolder(
        private val binding: ItemAvailableJobBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Booking) {
            binding.tvJobType.text = "Type: ${item.bookingType}"
            binding.tvPickup.text = "Pickup: ${item.pickupAddress}"
            binding.tvDrop.text = "Drop: ${item.dropAddress}"
            binding.tvPackage.text = "Package: ${item.packageType} / ${item.packageWeight}"
            binding.tvReceiver.text = "Receiver: ${item.receiverName} (${item.receiverPhone})"
            binding.tvFareDistance.text =
                "Fare: ৳${item.estimatedFare.toInt()} | Distance: ${"%.2f".format(item.distanceKm)} km"
            binding.tvStatus.text = "Status: ${item.status}"

            binding.btnAcceptJob.setOnClickListener {
                onAcceptClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemAvailableJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Booking>) {
        items = newItems
        notifyDataSetChanged()
    }
}