package com.example.courierapp.ui.customer.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.repository.CustomerRepository
import com.example.courierapp.databinding.FragmentCustomerHomeBinding
import com.example.courierapp.ui.common.RecentBookingsAdapter

class CustomerHomeFragment : Fragment() {

    private var _binding: FragmentCustomerHomeBinding? = null
    private val binding get() = _binding!!

    private val customerRepository = CustomerRepository()
    private lateinit var recentBookingsAdapter: RecentBookingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadCustomerData()
        loadRecentBookings()
    }

    private fun setupRecyclerView() {
        recentBookingsAdapter = RecentBookingsAdapter(emptyList())
        binding.rvRecentBookings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentBookingsAdapter
        }
    }

    private fun loadCustomerData() {
        customerRepository.getCurrentCustomerProfile(
            onSuccess = { user ->
                binding.tvWelcomeCustomer.text = "Welcome, ${user.fullName}"
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadRecentBookings() {
        customerRepository.getRecentBookings(
            onSuccess = { bookings ->
                binding.tvBookingSummary.text = "You have ${bookings.size} recent bookings"
                recentBookingsAdapter.updateData(bookings)

                if (bookings.isEmpty()) {
                    binding.tvEmptyRecentBookings.visibility = View.VISIBLE
                    binding.rvRecentBookings.visibility = View.GONE
                } else {
                    binding.tvEmptyRecentBookings.visibility = View.GONE
                    binding.rvRecentBookings.visibility = View.VISIBLE
                }
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