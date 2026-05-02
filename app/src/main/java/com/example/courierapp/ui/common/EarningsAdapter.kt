package com.example.courierapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courierapp.data.model.Earning
import com.example.courierapp.databinding.ItemEarningBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EarningsAdapter(
    private var items: List<Earning>
) : RecyclerView.Adapter<EarningsAdapter.EarningViewHolder>() {

    inner class EarningViewHolder(
        private val binding: ItemEarningBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Earning) {
            binding.tvAmount.text = "Amount: ৳${item.amount.toInt()}"

            binding.tvCustomerName.text = if (item.customerName.isNotEmpty()) {
                "Customer: ${item.customerName}"
            } else {
                "Customer: Unknown"
            }

            binding.tvDropAddress.text = if (item.dropAddress.isNotEmpty()) {
                "Drop: ${item.dropAddress}"
            } else {
                "Drop: Not available"
            }

            binding.tvPayment.text = "Payment: ${item.paymentMethod} / ${item.paymentStatus}"

            val dateText = if (item.createdAt > 0L) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(item.createdAt))
            } else {
                "Unknown time"
            }

            binding.tvDate.text = dateText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningViewHolder {
        val binding = ItemEarningBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EarningViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EarningViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Earning>) {
        items = newItems
        notifyDataSetChanged()
    }
}