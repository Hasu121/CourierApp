package com.example.courierapp.ui.customer.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.repository.BookingRepository
import com.example.courierapp.databinding.FragmentCreateBookingBinding

class CreateBookingFragment : Fragment() {

    private var _binding: FragmentCreateBookingBinding? = null
    private val binding get() = _binding!!
    private val bookingRepository = BookingRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()

        binding.btnCreateBooking.setOnClickListener {
            createBooking()
        }
    }

    private fun setupSpinners() {
        val bookingTypes = listOf("within_city", "intercity")
        val packageWeights = listOf("light", "medium", "heavy")
        val preferredTimes = listOf("now", "today_morning", "today_evening", "tomorrow")

        binding.spinnerBookingType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            bookingTypes
        )

        binding.spinnerPackageWeight.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            packageWeights
        )

        binding.spinnerPreferredTime.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            preferredTimes
        )
    }

    private fun createBooking() {
        val bookingType = binding.spinnerBookingType.selectedItem.toString()
        val pickupAddress = binding.etPickupAddress.text.toString().trim()
        val dropAddress = binding.etDropAddress.text.toString().trim()
        val packageType = binding.etPackageType.text.toString().trim()
        val packageWeight = binding.spinnerPackageWeight.selectedItem.toString()
        val packageNote = binding.etPackageNote.text.toString().trim()
        val receiverName = binding.etReceiverName.text.toString().trim()
        val receiverPhone = binding.etReceiverPhone.text.toString().trim()
        val preferredTime = binding.spinnerPreferredTime.selectedItem.toString()

        if (pickupAddress.isEmpty() ||
            dropAddress.isEmpty() ||
            packageType.isEmpty() ||
            receiverName.isEmpty() ||
            receiverPhone.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        bookingRepository.createBooking(
            bookingType = bookingType,
            pickupAddress = pickupAddress,
            dropAddress = dropAddress,
            packageType = packageType,
            packageWeight = packageWeight,
            packageNote = packageNote,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            preferredTime = preferredTime,
            onSuccess = {
                Toast.makeText(requireContext(), "Booking created successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearForm() {
        binding.etPickupAddress.text?.clear()
        binding.etDropAddress.text?.clear()
        binding.etPackageType.text?.clear()
        binding.etPackageNote.text?.clear()
        binding.etReceiverName.text?.clear()
        binding.etReceiverPhone.text?.clear()
        binding.spinnerBookingType.setSelection(0)
        binding.spinnerPackageWeight.setSelection(0)
        binding.spinnerPreferredTime.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}