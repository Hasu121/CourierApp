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
import com.example.courierapp.databinding.FragmentAvailableJobsBinding
import com.example.courierapp.ui.common.AvailableJobsAdapter

class AvailableJobsFragment : Fragment() {

    private var _binding: FragmentAvailableJobsBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private lateinit var adapter: AvailableJobsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailableJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AvailableJobsAdapter(emptyList()) { booking ->
            acceptJob(booking)
        }

        binding.rvAvailableJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableJobs.adapter = adapter

        loadJobs()
    }

    private fun loadJobs() {
        driverRepository.getAvailableJobs(
            onSuccess = { jobs ->
                binding.tvJobsInfo.text = "Found ${jobs.size} matching jobs"
                adapter.updateData(jobs)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun acceptJob(booking: Booking) {
        driverRepository.acceptJob(
            bookingId = booking.bookingId,
            customerId = booking.customerId,
            onSuccess = {
                Toast.makeText(requireContext(), "Job accepted", Toast.LENGTH_SHORT).show()
                loadJobs()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadJobs()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}