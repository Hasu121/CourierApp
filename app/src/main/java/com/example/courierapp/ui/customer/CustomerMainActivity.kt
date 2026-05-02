package com.example.courierapp.ui.customer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.courierapp.R
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.ActivityCustomerMainBinding
import com.example.courierapp.ui.auth.LoginActivity
import com.example.courierapp.ui.customer.booking.CreateBookingFragment
import com.example.courierapp.ui.customer.history.CustomerBookingHistoryFragment
import com.example.courierapp.ui.customer.home.CustomerHomeFragment
import com.example.courierapp.ui.customer.profile.CustomerProfileFragment
import com.example.courierapp.utils.InsetHelper
import com.example.courierapp.ui.customer.notifications.CustomerNotificationsFragment

class CustomerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetHelper.applySystemBarPadding(
            view = binding.customerTopBar,
            applyTop = true,
            applyBottom = false
        )

        InsetHelper.applySystemBarPadding(
            view = binding.bottomNavigationCustomer,
            applyTop = false,
            applyBottom = true
        )

        InsetHelper.applySystemBarPadding(
            view = binding.customerDrawerContent,
            applyTop = true,
            applyBottom = true
        )

        sessionManager = SessionManager(this)

        loadDrawerProfile()

        if (savedInstanceState == null) {
            loadFragment(CustomerHomeFragment())
            binding.tvCustomerTopTitle.text = "Home"
        }

        binding.btnCustomerMenu.setOnClickListener {
            binding.customerDrawerLayout.openDrawer(GravityCompat.START)
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
                R.id.menu_customer_notifications -> {
                    loadFragment(CustomerNotificationsFragment())
                    true
                }
                R.id.menu_customer_profile -> {
                    loadFragment(CustomerProfileFragment())
                    true
                }
                else -> false
            }
        }

        setupDrawerButtons()
    }

    override fun onResume() {
        super.onResume()
        loadDrawerProfile()
    }
    private fun setupDrawerButtons() {
        binding.btnDrawerCheckProfile.setOnClickListener {
            binding.customerDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationCustomer.selectedItemId = R.id.menu_customer_profile
        }

        binding.btnDrawerGoHome.setOnClickListener {
            binding.customerDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationCustomer.selectedItemId = R.id.menu_customer_home
        }

        binding.btnDrawerGoBooking.setOnClickListener {
            binding.customerDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationCustomer.selectedItemId = R.id.menu_customer_booking
        }

        binding.btnDrawerGoHistory.setOnClickListener {
            binding.customerDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationCustomer.selectedItemId = R.id.menu_customer_history
        }

        binding.btnDrawerLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadDrawerProfile() {
        val currentUser = FirebaseRefs.auth.currentUser ?: return

        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                binding.tvDrawerName.text = document.getString("fullName").orEmpty()
                binding.tvDrawerEmail.text = document.getString("email").orEmpty()
                binding.tvDrawerRole.text = "Customer"
            }
    }

    private fun logout() {
        FirebaseRefs.auth.signOut()
        sessionManager.clear()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.customerFragmentContainer, fragment)
            .commit()
    }
}