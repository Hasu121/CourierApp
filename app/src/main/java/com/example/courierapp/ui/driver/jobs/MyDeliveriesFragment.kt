package com.example.courierapp.ui.driver.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.databinding.FragmentMyDeliveriesBinding
import com.example.courierapp.ui.common.MyDeliveriesAdapter
import com.example.courierapp.utils.Constants

class MyDeliveriesFragment : Fragment() {

    private var _binding: FragmentMyDeliveriesBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private lateinit var adapter: MyDeliveriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDeliveriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MyDeliveriesAdapter(emptyList()) { booking ->
            updateToNextStatus(booking)
        }

        binding.rvMyDeliveries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyDeliveries.adapter = adapter

        loadMyDeliveries()
    }

    private fun loadMyDeliveries() {
        driverRepository.getMyDeliveries(
            onSuccess = { deliveries ->
                binding.tvMyDeliveriesInfo.text = "Active deliveries: ${deliveries.size}"
                adapter.updateData(deliveries)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateToNextStatus(booking: Booking) {
        val nextStatus = when (booking.status) {
            Constants.STATUS_ACCEPTED -> Constants.STATUS_PICKED_UP
            Constants.STATUS_PICKED_UP -> Constants.STATUS_IN_TRANSIT
            Constants.STATUS_IN_TRANSIT -> Constants.STATUS_DELIVERED
            else -> ""
        }

        if (nextStatus.isEmpty()) {
            Toast.makeText(requireContext(), "No next status available", Toast.LENGTH_SHORT).show()
            return
        }

        driverRepository.updateDeliveryStatus(
            booking = booking,
            newStatus = nextStatus,
            onSuccess = {
                Toast.makeText(requireContext(), "Status updated to $nextStatus", Toast.LENGTH_SHORT).show()
                loadMyDeliveries()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadMyDeliveries()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}