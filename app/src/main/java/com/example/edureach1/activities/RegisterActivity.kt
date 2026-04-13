package com.example.edureach1.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.edureach1.R
import com.example.edureach1.databinding.ActivityRegisterBinding
import com.example.edureach1.utils.Constants
import com.example.edureach1.utils.LoadingDialog
import com.example.edureach1.viewmodels.AuthState
import com.example.edureach1.viewmodels.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        setupClassSpinner()
        setupRoleToggle()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClassSpinner() {
        val classes = listOf("Select Class", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, classes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClass.adapter = adapter
    }

    private fun setupRoleToggle() {
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStudent -> {
                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_green)
                    binding.rbStudent.setTextColor(getColor(R.color.white))
                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_outline)
                    binding.rbTeacher.setTextColor(getColor(R.color.green_primary))
                    binding.tvClassLabel.visibility = View.VISIBLE
                    binding.spinnerClass.visibility = View.VISIBLE
                }
                R.id.rbTeacher -> {
                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_green)
                    binding.rbTeacher.setTextColor(getColor(R.color.white))
                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_outline)
                    binding.rbStudent.setTextColor(getColor(R.color.green_primary))
                    binding.tvClassLabel.visibility = View.GONE
                    binding.spinnerClass.visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val role = if (binding.rbTeacher.isChecked) Constants.ROLE_TEACHER else Constants.ROLE_STUDENT
            val classPosition = binding.spinnerClass.selectedItemPosition

            if (name.isEmpty()) { showError("Please enter your name"); return@setOnClickListener }
            if (email.isEmpty()) { showError("Please enter your email"); return@setOnClickListener }
            if (role == Constants.ROLE_STUDENT && classPosition == 0) { showError("Please select your class"); return@setOnClickListener }
            if (password.isEmpty()) { showError("Please enter a password"); return@setOnClickListener }
            if (password.length < 6) { showError("Password must be at least 6 characters"); return@setOnClickListener }
            if (password != confirmPassword) { showError("Passwords do not match"); return@setOnClickListener }

            val classLevel = if (role == Constants.ROLE_STUDENT) (classPosition + 5).toString() else ""

            hideError()
            viewModel.register(name, email, password, role, classLevel)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> loadingDialog.show()

                is AuthState.Success -> {
                    loadingDialog.dismiss()
                    binding.tvError.text = "✓ Account created! Check your email to verify before logging in."
                    binding.tvError.setTextColor(getColor(R.color.green_primary))
                    binding.tvError.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                is AuthState.Error -> {
                    loadingDialog.dismiss()
                    showError(state.message)
                }
            }
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