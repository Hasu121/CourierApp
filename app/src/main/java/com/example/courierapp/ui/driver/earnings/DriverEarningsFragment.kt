package com.example.courierapp.ui.driver.earnings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.databinding.FragmentDriverEarningsBinding
import com.example.courierapp.ui.common.EarningsAdapter

class DriverEarningsFragment : Fragment() {

    private var _binding: FragmentDriverEarningsBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private lateinit var adapter: EarningsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverEarningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EarningsAdapter(emptyList())
        binding.rvEarnings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEarnings.adapter = adapter

        loadEarnings()
    }

    private fun loadEarnings() {
        driverRepository.getDriverEarnings(
            onSuccess = { totalEarnings, todayEarnings, completedCount, earnings ->
                binding.tvTotalEarnings.text = "Total Earnings: ৳${totalEarnings.toInt()}"
                binding.tvTodayEarnings.text = "Today's Earnings: ৳${todayEarnings.toInt()}"
                binding.tvCompletedCount.text = "Completed Deliveries: $completedCount"

                binding.tvEarningsInfo.text = if (earnings.isEmpty()) {
                    "No earnings yet."
                } else {
                    "Recent earnings"
                }

                adapter.updateData(earnings)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}