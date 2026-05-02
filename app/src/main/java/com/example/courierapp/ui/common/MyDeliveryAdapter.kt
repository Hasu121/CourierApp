package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.ItemMyDeliveryBinding

class MyDeliveriesAdapter(
    private var items: List<Booking>,
    private val onNextStatusClick: (Booking) -> Unit
) : RecyclerView.Adapter<MyDeliveriesAdapter.MyDeliveryViewHolder>() {

    inner class MyDeliveryViewHolder(
        private val binding: ItemMyDeliveryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Booking) {
            binding.tvStatus.text = "Status: ${item.status}"
            binding.tvPickup.text = "Pickup: ${item.pickupAddress}"
            binding.tvDrop.text = "Drop: ${item.dropAddress}"
            binding.tvPackage.text = "Package: ${item.packageType} / ${item.packageWeight}"
            binding.tvReceiver.text = "Receiver: ${item.receiverName} (${item.receiverPhone})"
            binding.tvFareDistance.text =
                "Fare: ৳${item.estimatedFare.toInt()} | Distance: ${"%.2f".format(item.distanceKm)} km"

            binding.btnNextStatus.text = when (item.status) {
                "accepted" -> "Mark Picked Up"
                "picked_up" -> "Mark In Transit"
                "in_transit" -> "Mark Delivered"
                else -> "Update Status"
            }

            binding.btnNextStatus.setOnClickListener {
                onNextStatusClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDeliveryViewHolder {
        val binding = ItemMyDeliveryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyDeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyDeliveryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Booking>) {
        items = newItems
        notifyDataSetChanged()
    }
}