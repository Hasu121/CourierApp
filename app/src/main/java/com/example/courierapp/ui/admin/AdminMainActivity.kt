package com.example.courierapp.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.courierapp.R
import com.example.courierapp.databinding.ActivityAdminMainBinding
import com.example.courierapp.ui.admin.drivers.AdminDriversFragment
import com.example.courierapp.ui.admin.home.AdminHomeFragment
import com.example.courierapp.ui.admin.profile.AdminProfileFragment

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.courier_green)
        window.navigationBarColor = getColor(R.color.courier_light_bg)

        if (savedInstanceState == null) {
            loadFragment(AdminHomeFragment())
        }

        binding.bottomNavigationAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_admin_home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }

                R.id.menu_admin_drivers -> {
                    loadFragment(AdminDriversFragment())
                    true
                }

                R.id.menu_admin_profile -> {
                    loadFragment(AdminProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}