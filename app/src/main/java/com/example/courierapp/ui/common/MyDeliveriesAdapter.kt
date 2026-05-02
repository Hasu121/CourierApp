package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.ItemMyDeliveryBinding
import com.example.courierapp.utils.Constants
import com.example.courierapp.utils.StatusFormatter

class MyDeliveriesAdapter(
    private var items: List<Booking>,
    private val onNextStatusClick: (Booking) -> Unit,
    private val onRejectClick: (Booking) -> Unit
) : RecyclerView.Adapter<MyDeliveriesAdapter.MyDeliveryViewHolder>() {

    inner class MyDeliveryViewHolder(
        private val binding: ItemMyDeliveryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Booking) {
            binding.tvStatus.text = "Status: ${StatusFormatter.formatStatus(item.status)}"
            binding.tvPickup.text = "Pickup: ${item.pickupAddress}"
            binding.tvDrop.text = "Drop: ${item.dropAddress}"
            binding.tvPackage.text = "Package: ${item.packageType} / ${item.packageWeight}"
            binding.tvReceiver.text = "Receiver: ${item.receiverName} (${item.receiverPhone})"

            val fare = if (item.finalFare > 0.0) item.finalFare else item.estimatedFare
            binding.tvFareDistance.text =
                "Fare: ৳${fare.toInt()} | Distance: ${"%.2f".format(item.distanceKm)} km"

            binding.btnNextStatus.text = when (item.status) {
                Constants.STATUS_ACCEPTED -> "Mark Picked Up"
                Constants.STATUS_PICKED_UP -> "Mark In Transit"
                Constants.STATUS_IN_TRANSIT -> "Mark Delivered"
                else -> "Update Status"
            }

            binding.btnRejectJob.visibility = if (item.status == Constants.STATUS_ACCEPTED) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.btnNextStatus.visibility = when (item.status) {
                Constants.STATUS_ACCEPTED,
                Constants.STATUS_PICKED_UP,
                Constants.STATUS_IN_TRANSIT -> View.VISIBLE
                else -> View.GONE
            }

            binding.btnNextStatus.setOnClickListener {
                onNextStatusClick(item)
            }

            binding.btnRejectJob.setOnClickListener {
                onRejectClick(item)
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