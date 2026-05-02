package com.example.courierapp.ui.driver.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.databinding.FragmentDriverDeliveryHistoryBinding
import com.example.courierapp.ui.common.DriverDeliveryHistoryAdapter

class DriverDeliveryHistoryFragment : Fragment() {

    private var _binding: FragmentDriverDeliveryHistoryBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private lateinit var adapter: DriverDeliveryHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverDeliveryHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DriverDeliveryHistoryAdapter(
            items = emptyList(),
            customerNames = emptyMap()
        ) {
            Toast.makeText(requireContext(), "Details screen can be added later", Toast.LENGTH_SHORT).show()
        }

        binding.rvDeliveryHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDeliveryHistory.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        driverRepository.getDeliveryHistory(
            onSuccess = { deliveries ->
                binding.tvDeliveryHistoryInfo.text = if (deliveries.isEmpty()) {
                    "No completed deliveries yet."
                } else {
                    "Completed deliveries: ${deliveries.size}"
                }

                loadCustomerNames(deliveries)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadCustomerNames(deliveries: List<Booking>) {
        driverRepository.getCustomerNamesForBookings(
            bookings = deliveries,
            onSuccess = { customerNames ->
                adapter.updateData(deliveries, customerNames)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                adapter.updateData(deliveries, emptyMap())
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}