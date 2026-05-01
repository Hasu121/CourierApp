package com.example.courierapp.ui.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.courierapp.R
import com.example.courierapp.databinding.ActivityCustomerMainBinding
import com.example.courierapp.ui.customer.booking.CreateBookingFragment
import com.example.courierapp.ui.customer.home.CustomerHomeFragment
import com.example.courierapp.ui.customer.profile.CustomerProfileFragment
import com.example.courierapp.ui.customer.history.CustomerBookingHistoryFragment


class CustomerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(CustomerHomeFragment())
        }

        binding.bottomNavigationCustomer.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_customer_home -> {
                    loadFragment(CustomerHomeFragment())
                    true
                }
                R.id.menu_customer_booking -> {
                    loadFragment(CreateBookingFragment())
                    true
                }
                R.id.menu_customer_history -> {
                    loadFragment(CustomerBookingHistoryFragment())
                    true
                }
                R.id.menu_customer_profile -> {
                    loadFragment(CustomerProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.customerFragmentContainer, fragment)
            .commit()
    }
}