package com.example.courierapp.ui.driver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.core.view.WindowInsetsCompat
import com.example.courierapp.R
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.ActivityDriverMainBinding
import com.example.courierapp.ui.auth.LoginActivity
import com.example.courierapp.ui.driver.earnings.DriverEarningsFragment
import com.example.courierapp.ui.driver.history.DriverDeliveryHistoryFragment
import com.example.courierapp.ui.driver.home.DriverHomeFragment
import com.example.courierapp.ui.driver.jobs.AvailableJobsFragment
import com.example.courierapp.ui.driver.jobs.MyDeliveriesFragment
import com.example.courierapp.ui.driver.profile.DriverProfileFragment

class DriverMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        window.statusBarColor = ContextCompat.getColor(this, R.color.courier_green)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.courier_light_bg)

        ViewCompat.setOnApplyWindowInsetsListener(binding.driverTopBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                view.paddingBottom
            )

            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.driverDrawerContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBars.top + 20,
                view.paddingRight,
                systemBars.bottom + 20
            )

            insets
        }

        setupTopBar()
        setupBottomNavigation()
        setupDrawerButtons()

        if (savedInstanceState == null) {
            loadFragment(DriverHomeFragment(), "Driver Home")
            binding.bottomNavigationDriver.selectedItemId = R.id.menu_driver_home
        }

        loadDrawerProfile()
    }

    override fun onResume() {
        super.onResume()
        loadDrawerProfile()
    }

    private fun setupTopBar() {
        binding.btnDriverMenu.setOnClickListener {
            binding.driverDrawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationDriver.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_driver_home -> {
                    loadFragment(DriverHomeFragment(), "Driver Home")
                    true
                }

                R.id.menu_driver_jobs -> {
                    loadFragment(AvailableJobsFragment(), "Available Jobs")
                    true
                }

                R.id.menu_driver_my_deliveries -> {
                    loadFragment(MyDeliveriesFragment(), "My Jobs")
                    true
                }

                R.id.menu_driver_history -> {
                    loadFragment(DriverDeliveryHistoryFragment(), "Delivery History")
                    true
                }

                else -> false
            }
        }
    }

    private fun setupDrawerButtons() {
        binding.btnDrawerCheckProfile.setOnClickListener {
            loadFragment(DriverProfileFragment(), "Driver Profile")
            binding.driverDrawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.btnDrawerGoEarnings.setOnClickListener {
            loadFragment(DriverEarningsFragment(), "Earnings")
            binding.driverDrawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.btnDrawerLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
                binding.tvDrawerRole.text = "Driver"
            }

        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val vehicleType = document.getString("vehicleType").orEmpty()
                val vehicleNumber = document.getString("vehicleNumber").orEmpty()
                binding.tvDrawerVehicle.text = "Vehicle: $vehicleType $vehicleNumber"
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
        binding.tvDriverTopTitle.text = title

        supportFragmentManager.beginTransaction()
            .replace(R.id.driverFragmentContainer, fragment)
            .commit()
    }
}