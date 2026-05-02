package com.example.courierapp.ui.driver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.courierapp.R
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.ActivityDriverMainBinding
import com.example.courierapp.ui.auth.LoginActivity
import com.example.courierapp.ui.driver.home.DriverHomeFragment
import com.example.courierapp.ui.driver.jobs.AvailableJobsFragment
import com.example.courierapp.ui.driver.jobs.MyDeliveriesFragment
import com.example.courierapp.ui.driver.profile.DriverProfileFragment
import com.example.courierapp.utils.InsetHelper

class DriverMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetHelper.applySystemBarPadding(
            view = binding.driverTopBar,
            applyTop = true,
            applyBottom = false
        )

        InsetHelper.applySystemBarPadding(
            view = binding.bottomNavigationDriver,
            applyTop = false,
            applyBottom = true
        )

        InsetHelper.applySystemBarPadding(
            view = binding.driverDrawerContent,
            applyTop = true,
            applyBottom = true
        )

        sessionManager = SessionManager(this)

        loadDrawerProfile()

        if (savedInstanceState == null) {
            loadFragment(DriverHomeFragment())
            binding.tvDriverTopTitle.text = "Driver Home"
        }

        binding.btnDriverMenu.setOnClickListener {
            binding.driverDrawerLayout.openDrawer(GravityCompat.START)
        }

        binding.bottomNavigationDriver.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_driver_home -> {
                    loadFragment(DriverHomeFragment())
                    binding.tvDriverTopTitle.text = "Driver Home"
                    true
                }

                R.id.menu_driver_jobs -> {
                    loadFragment(AvailableJobsFragment())
                    binding.tvDriverTopTitle.text = "Available Jobs"
                    true
                }

                R.id.menu_driver_my_deliveries -> {
                    loadFragment(MyDeliveriesFragment())
                    binding.tvDriverTopTitle.text = "My Jobs"
                    true
                }

                R.id.menu_driver_profile -> {
                    loadFragment(DriverProfileFragment())
                    binding.tvDriverTopTitle.text = "Profile"
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
            binding.driverDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationDriver.selectedItemId = R.id.menu_driver_profile
        }

        binding.btnDrawerGoHome.setOnClickListener {
            binding.driverDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationDriver.selectedItemId = R.id.menu_driver_home
        }

        binding.btnDrawerGoJobs.setOnClickListener {
            binding.driverDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationDriver.selectedItemId = R.id.menu_driver_jobs
        }

        binding.btnDrawerGoMyJobs.setOnClickListener {
            binding.driverDrawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigationDriver.selectedItemId = R.id.menu_driver_my_deliveries
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.driverFragmentContainer, fragment)
            .commit()
    }
}