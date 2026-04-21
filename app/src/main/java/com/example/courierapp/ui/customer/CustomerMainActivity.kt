package com.example.courierapp.ui.customer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.ActivityCustomerMainBinding
import com.example.courierapp.ui.auth.LoginActivity

class CustomerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}