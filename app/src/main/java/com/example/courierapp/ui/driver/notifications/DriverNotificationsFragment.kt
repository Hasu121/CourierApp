package com.example.courierapp.ui.driver.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.repository.NotificationRepository
import com.example.courierapp.databinding.FragmentDriverNotificationsBinding
import com.example.courierapp.ui.common.DriverNotificationsAdapter

class DriverNotificationsFragment : Fragment() {

    private var _binding: FragmentDriverNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationRepository = NotificationRepository()
    private lateinit var adapter: DriverNotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = DriverNotificationsAdapter(emptyList())
        binding.rvDriverNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDriverNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        notificationRepository.getDriverNotifications(
            onSuccess = { notifications ->
                adapter.updateData(notifications)
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