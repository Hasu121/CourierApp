package com.example.courierapp.ui.customer

import android.content.Intent
import android.os.Bundle
import android.os.Build
import androidx.core.view.WindowCompat
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
import com.example.courierapp.ui.customer.notifications.CustomerNotificationsFragment
import com.example.courierapp.ui.customer.profile.CustomerProfileFragment

class CustomerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.statusBarColor =
            androidx.core.content.ContextCompat.getColor(this, R.color.courier_green)

        window.navigationBarColor =
            androidx.core.content.ContextCompat.getColor(this, R.color.courier_light_bg)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = 0
        }

        sessionManager = SessionManager(this)

        window.statusBarColor =
            androidx.core.content.ContextCompat.getColor(this, R.color.courier_green)
        window.navigationBarColor =
            androidx.core.content.ContextCompat.getColor(this, R.color.courier_light_bg)

        if (savedInstanceState == null) {
            loadFragment(CustomerHomeFragment(), "Customer Home")
            binding.bottomNavigationCustomer.selectedItemId = R.id.menu_customer_home
        }

        binding.btnCustomerMenu.setOnClickListener {
            binding.customerDrawerLayout.openDrawer(GravityCompat.START)
        }

        binding.bottomNavigationCustomer.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_customer_home -> {
                    loadFragment(CustomerHomeFragment(), "Customer Home")
                    true
                }

                R.id.menu_customer_booking -> {
                    loadFragment(CreateBookingFragment(), "Create Booking")
                    true
                }

                R.id.menu_customer_history -> {
                    loadFragment(CustomerBookingHistoryFragment(), "Booking History")
                    true
                }

                R.id.menu_customer_notifications -> {
                    loadFragment(CustomerNotificationsFragment(), "Alerts")
                    true
                }

                else -> false
            }
        }

        binding.btnDrawerCheckProfile.setOnClickListener {
            loadFragment(CustomerProfileFragment(), "Customer Profile")
            binding.customerDrawerLayout.closeDrawer(GravityCompat.START)
        }


        binding.btnDrawerLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDrawerProfile()
    }
    private fun setupDrawerButtons() {
        binding.btnDrawerCheckProfile.setOnClickListener {
            loadFragment(CustomerProfileFragment(), "Customer Profile")
            binding.customerDrawerLayout.closeDrawer(GravityCompat.START)
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

    private fun loadFragment(fragment: Fragment, title: String) {
        binding.tvCustomerTopTitle.text = title

        supportFragmentManager.beginTransaction()
            .replace(R.id.customerFragmentContainer, fragment)
            .commit()
    }
}