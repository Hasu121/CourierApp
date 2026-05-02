package com.example.courierapp.ui.admin.drivers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.model.AdminDriverItem
import com.example.courierapp.data.repository.AdminRepository
import com.example.courierapp.databinding.FragmentAdminDriversBinding
import com.example.courierapp.ui.common.AdminDriversAdapter

class AdminDriversFragment : Fragment() {

    private var _binding: FragmentAdminDriversBinding? = null
    private val binding get() = _binding!!

    private val adminRepository = AdminRepository()
    private lateinit var adapter: AdminDriversAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDriversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminDriversAdapter(
            items = emptyList(),
            onPendingClick = { driver ->
                updateDriverStatus(driver, "pending")
            },
            onApproveClick = { driver ->
                updateDriverStatus(driver, "verified")
            },
            onRejectClick = { driver ->
                updateDriverStatus(driver, "rejected")
            }
        )

        binding.rvAdminDrivers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminDrivers.adapter = adapter

        loadDrivers()
    }

    private fun loadDrivers() {
        adminRepository.getAllDrivers(
            onSuccess = { drivers ->
                binding.tvAdminDriversInfo.text = if (drivers.isEmpty()) {
                    "No drivers found."
                } else {
                    "Registered drivers: ${drivers.size}"
                }

                adapter.updateData(drivers)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateDriverStatus(driver: AdminDriverItem, status: String) {
        adminRepository.updateDriverVerification(
            driverId = driver.uid,
            status = status,
            onSuccess = {
                Toast.makeText(requireContext(), "Driver marked as $status", Toast.LENGTH_SHORT).show()
                loadDrivers()
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