package com.example.courierapp.ui.customer.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.repository.CustomerRepository
import com.example.courierapp.databinding.FragmentCustomerBookingHistoryBinding
import com.example.courierapp.ui.common.BookingHistoryAdapter

class CustomerBookingHistoryFragment : Fragment() {

    private var _binding: FragmentCustomerBookingHistoryBinding? = null
    private val binding get() = _binding!!

    private val customerRepository = CustomerRepository()
    private lateinit var adapter: BookingHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerBookingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BookingHistoryAdapter(emptyList()) { booking ->
            val intent = Intent(requireContext(), BookingDetailsActivity::class.java)
            intent.putExtra("bookingId", booking.bookingId)
            startActivity(intent)
        }

        binding.rvBookingHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookingHistory.adapter = adapter

        loadBookings()
    }

    private fun loadBookings() {
        customerRepository.getAllCustomerBookings(
            onSuccess = { bookings ->
                adapter.updateData(bookings)
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