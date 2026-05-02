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
import com.example.courierapp.data.repository.ReviewRepository

class DriverDeliveryHistoryFragment : Fragment() {

    private var _binding: FragmentDriverDeliveryHistoryBinding? = null
    private val binding get() = _binding!!
    private val reviewRepository = ReviewRepository()
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
            customerNames = emptyMap(),
            reviewedBookingIds = emptySet(),
            onSubmitReview = { booking, rating, comment ->
                submitDriverReview(booking, rating, comment)
            },
            onItemClick = {
                Toast.makeText(requireContext(), "Details screen can be added later", Toast.LENGTH_SHORT).show()
            }
        )

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

    private fun submitDriverReview(booking: Booking, rating: Int, comment: String) {
        reviewRepository.submitDriverReviewForCustomer(
            booking = booking,
            rating = rating,
            comment = comment,
            onSuccess = {
                Toast.makeText(requireContext(), "Customer review submitted", Toast.LENGTH_SHORT).show()
                loadHistory()
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
                loadDriverReviewedBookings(deliveries, customerNames)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadDriverReviewedBookings(deliveries, emptyMap())
            }
        )
    }

    private fun loadDriverReviewedBookings(
        deliveries: List<Booking>,
        customerNames: Map<String, String>
    ) {
        val reviewedIds = mutableSetOf<String>()

        if (deliveries.isEmpty()) {
            adapter.updateData(emptyList(), customerNames, emptySet())
            return
        }

        var completed = 0

        deliveries.forEach { booking ->
            reviewRepository.checkDriverReviewed(
                bookingId = booking.bookingId,
                onResult = { reviewed ->
                    if (reviewed) {
                        reviewedIds.add(booking.bookingId)
                    }

                    completed++

                    if (completed == deliveries.size) {
                        adapter.updateData(deliveries, customerNames, reviewedIds)
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}