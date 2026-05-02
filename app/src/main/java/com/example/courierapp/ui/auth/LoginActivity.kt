package com.example.courierapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.data.repository.AuthRepository
import com.example.courierapp.databinding.ActivityLoginBinding
import com.example.courierapp.ui.customer.CustomerMainActivity
import com.example.courierapp.ui.driver.DriverMainActivity
import com.example.courierapp.utils.Constants
import com.example.courierapp.ui.admin.AdminMainActivity
import android.app.AlertDialog
import com.example.courierapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.courier_green)
        window.navigationBarColor = getColor(R.color.courier_light_bg)

        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authRepository.loginUser(
                email = email,
                password = password,
                onSuccess = { uid, role ->
                    sessionManager.saveUserId(uid)
                    sessionManager.saveRole(role)

                    when (role) {
                        Constants.ROLE_CUSTOMER -> {
                            startActivity(Intent(this, CustomerMainActivity::class.java))
                            finish()
                        }

                        Constants.ROLE_DRIVER -> {
                            startActivity(Intent(this, DriverMainActivity::class.java))
                            finish()
                        }

                        Constants.ROLE_ADMIN -> {
                            startActivity(Intent(this, AdminMainActivity::class.java))
                            finish()
                        }

                        else -> {
                            Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterChoiceActivity::class.java))
        }
    }

    private fun showForgotPasswordDialog() {
        val emailInput = android.widget.EditText(this)
        emailInput.hint = "Enter your email"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your account email. We will send a password reset link.")
            .setView(emailInput)
            .setPositiveButton("Send") { dialog, _ ->
                val email = emailInput.text.toString().trim()

                if (email.isEmpty()) {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                authRepository.sendPasswordResetEmail(
                    email = email,
                    onSuccess = {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    },
                    onFailure = { message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}