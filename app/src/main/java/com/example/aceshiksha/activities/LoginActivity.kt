package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.R
import com.nikhil.aceshiksha.databinding.ActivityLoginBinding
import com.nikhil.aceshiksha.utils.LoadingDialog
import com.nikhil.aceshiksha.viewmodels.AuthState
import com.nikhil.aceshiksha.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        setupRoleToggle()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRoleToggle() {
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStudent -> {
                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_green)
                    binding.rbStudent.setTextColor(getColor(R.color.white))
                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_outline)
                    binding.rbTeacher.setTextColor(getColor(R.color.green_primary))
                }
                R.id.rbTeacher -> {
                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_green)
                    binding.rbTeacher.setTextColor(getColor(R.color.white))
                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_outline)
                    binding.rbStudent.setTextColor(getColor(R.color.green_primary))
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) { showError("Please enter your email"); return@setOnClickListener }
            if (password.isEmpty()) { showError("Please enter your password"); return@setOnClickListener }

            val selectedRole = if (binding.rgRole.checkedRadioButtonId == R.id.rbTeacher)
                "teacher" else "student"

            hideError()
            viewModel.login(email, password, selectedRole)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvResendVerification.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                showError("Enter your email and password to resend verification")
                return@setOnClickListener
            }
            hideError()
            binding.tvResendVerification.visibility = View.GONE
            viewModel.resendVerificationEmail(email, password)
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> loadingDialog.show()
                is AuthState.Success -> {
                    loadingDialog.dismiss()
                    navigateBasedOnRole()
                }
                is AuthState.Error -> {
                    loadingDialog.dismiss()
                    if (state.message == "EMAIL_NOT_VERIFIED") {
                        showError("Please verify your email before logging in.")
                        binding.tvResendVerification.visibility = View.VISIBLE
                    } else {
                        showError(state.message)
                        binding.tvResendVerification.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.resendState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> loadingDialog.show()
                is AuthState.Success -> {
                    loadingDialog.dismiss()
                    showError("✓ Verification email sent! Check your inbox.")
                    binding.tvError.setTextColor(getColor(R.color.green_primary))
                    binding.tvResendVerification.visibility = View.GONE
                }
                is AuthState.Error -> {
                    loadingDialog.dismiss()
                    showError(state.message)
                }
            }
        }
    }

    private fun navigateBasedOnRole() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "student"
                val destination = if (role == "teacher") {
                    TeacherDashboardActivity::class.java
                } else {
                    MainActivity::class.java
                }
                startActivity(Intent(this, destination))
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}