package com.example.courierapp.ui.customer.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.model.NotificationItem
import com.example.courierapp.data.repository.NotificationRepository
import com.example.courierapp.databinding.FragmentCustomerNotificationsBinding
import com.example.courierapp.ui.common.CustomerNotificationsAdapter

class CustomerNotificationsFragment : Fragment() {

    private var _binding: FragmentCustomerNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationRepository = NotificationRepository()
    private lateinit var adapter: CustomerNotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CustomerNotificationsAdapter(emptyList()) { notification ->
            markAsRead(notification)
        }

        binding.rvCustomerNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomerNotifications.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        notificationRepository.getCustomerNotifications(
            onSuccess = { notifications ->
                val unreadCount = notifications.count { !it.isRead }
                binding.tvNotificationInfo.text =
                    "You have ${notifications.size} notifications ($unreadCount unread)"
                adapter.updateData(notifications)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun markAsRead(notification: NotificationItem) {
        if (notification.isRead) {
            Toast.makeText(requireContext(), "Already read", Toast.LENGTH_SHORT).show()
            return
        }

        notificationRepository.markNotificationAsRead(
            notificationId = notification.notificationId,
            onSuccess = {
                Toast.makeText(requireContext(), "Marked as read", Toast.LENGTH_SHORT).show()
                loadNotifications()
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