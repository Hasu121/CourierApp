package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.AdminDriverItem
import com.example.courierapp.databinding.ItemAdminDriverBinding

class AdminDriversAdapter(
    private var items: List<AdminDriverItem>,
    private val onPendingClick: (AdminDriverItem) -> Unit,
    private val onApproveClick: (AdminDriverItem) -> Unit,
    private val onRejectClick: (AdminDriverItem) -> Unit
) : RecyclerView.Adapter<AdminDriversAdapter.AdminDriverViewHolder>() {

    inner class AdminDriverViewHolder(
        private val binding: ItemAdminDriverBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminDriverItem) {
            binding.tvDriverName.text = "Name: ${item.fullName}"
            binding.tvDriverEmail.text = "Email: ${item.email}"
            binding.tvVehicleInfo.text = "Vehicle: ${item.vehicleType} / ${item.vehicleNumber}"
            binding.tvLicenseInfo.text = "License: ${item.licenseNumber}"
            binding.tvServiceMode.text = "Service Mode: ${item.serviceMode.joinToString(", ")}"
            binding.tvVerificationStatus.text = "Status: ${item.verificationStatus}"

            binding.btnPendingDriver.isEnabled = item.verificationStatus != "pending"
            binding.btnApproveDriver.isEnabled = item.verificationStatus != "verified"
            binding.btnRejectDriver.isEnabled = item.verificationStatus != "rejected"

            binding.btnPendingDriver.alpha = if (binding.btnPendingDriver.isEnabled) 1.0f else 0.45f
            binding.btnApproveDriver.alpha = if (binding.btnApproveDriver.isEnabled) 1.0f else 0.45f
            binding.btnRejectDriver.alpha = if (binding.btnRejectDriver.isEnabled) 1.0f else 0.45f

            binding.btnPendingDriver.setOnClickListener {
                onPendingClick(item)
            }

            binding.btnApproveDriver.setOnClickListener {
                onApproveClick(item)
            }

            binding.btnRejectDriver.setOnClickListener {
                onRejectClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminDriverViewHolder {
        val binding = ItemAdminDriverBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminDriverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminDriverViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AdminDriverItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}