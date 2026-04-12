//package com.example.edureach1.activities
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.RadioButton
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import com.example.edureach1.R
//import com.example.edureach1.databinding.ActivityLoginBinding
//import com.example.edureach1.utils.LoadingDialog
//import com.example.edureach1.viewmodels.AuthState
//import com.example.edureach1.viewmodels.AuthViewModel
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityLoginBinding
//    private val viewModel: AuthViewModel by viewModels()
//    private lateinit var loadingDialog: LoadingDialog
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        loadingDialog = LoadingDialog(this)
//        setupRoleToggle()
//        setupClickListeners()
//        observeViewModel()
//    }
//
//    private fun setupRoleToggle() {
//        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.rbStudent -> {
//                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_green)
//                    binding.rbStudent.setTextColor(getColor(R.color.white))
//                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_outline)
//                    binding.rbTeacher.setTextColor(getColor(R.color.green_primary))
//                }
//                R.id.rbTeacher -> {
//                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_green)
//                    binding.rbTeacher.setTextColor(getColor(R.color.white))
//                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_outline)
//                    binding.rbStudent.setTextColor(getColor(R.color.green_primary))
//                }
//            }
//        }
//    }
//
//    private fun setupClickListeners() {
//        binding.btnLogin.setOnClickListener {
//            val email = binding.etEmail.text.toString().trim()
//            val password = binding.etPassword.text.toString().trim()
//
//            if (email.isEmpty()) {
//                showError("Please enter your email")
//                return@setOnClickListener
//            }
//            if (password.isEmpty()) {
//                showError("Please enter your password")
//                return@setOnClickListener
//            }
//
//            hideError()
//            viewModel.login(email, password)
//        }
//
//        binding.tvRegister.setOnClickListener {
//            startActivity(Intent(this, RegisterActivity::class.java))
//        }
//    }
//
//    private fun observeViewModel() {
//        viewModel.authState.observe(this) { state ->
//            when (state) {
//                is AuthState.Loading -> loadingDialog.show()
//                is AuthState.Success -> {
//                    loadingDialog.dismiss()
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
//                is AuthState.Error -> {
//                    loadingDialog.dismiss()
//                    showError(state.message)
//                }
//            }
//        }
//    }
//
//    private fun showError(message: String) {
//        binding.tvError.text = message
//        binding.tvError.visibility = View.VISIBLE
//    }
//
//    private fun hideError() {
//        binding.tvError.visibility = View.GONE
//    }
//}
//

//package com.example.edureach1.activities
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import com.example.edureach1.R
//import com.example.edureach1.databinding.ActivityLoginBinding
//import com.example.edureach1.utils.LoadingDialog
//import com.example.edureach1.viewmodels.AuthState
//import com.example.edureach1.viewmodels.AuthViewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityLoginBinding
//    private val viewModel: AuthViewModel by viewModels()
//    private lateinit var loadingDialog: LoadingDialog
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        loadingDialog = LoadingDialog(this)
//        setupRoleToggle()
//        setupClickListeners()
//        observeViewModel()
//    }
//
//    private fun setupRoleToggle() {
//        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.rbStudent -> {
//                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_green)
//                    binding.rbStudent.setTextColor(getColor(R.color.white))
//                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_outline)
//                    binding.rbTeacher.setTextColor(getColor(R.color.green_primary))
//                }
//                R.id.rbTeacher -> {
//                    binding.rbTeacher.background = getDrawable(R.drawable.bg_button_green)
//                    binding.rbTeacher.setTextColor(getColor(R.color.white))
//                    binding.rbStudent.background = getDrawable(R.drawable.bg_button_outline)
//                    binding.rbStudent.setTextColor(getColor(R.color.green_primary))
//                }
//            }
//        }
//    }
//
//    private fun setupClickListeners() {
//        binding.btnLogin.setOnClickListener {
//            val email = binding.etEmail.text.toString().trim()
//            val password = binding.etPassword.text.toString().trim()
//
//            if (email.isEmpty()) {
//                showError("Please enter your email")
//                return@setOnClickListener
//            }
//            if (password.isEmpty()) {
//                showError("Please enter your password")
//                return@setOnClickListener
//            }
//
//            hideError()
//            viewModel.login(email, password)
//        }
//
//        binding.tvRegister.setOnClickListener {
//            startActivity(Intent(this, RegisterActivity::class.java))
//        }
//    }
//
//    private fun observeViewModel() {
//        viewModel.authState.observe(this) { state ->
//            when (state) {
//                is AuthState.Loading -> loadingDialog.show()
//                is AuthState.Success -> {
//                    loadingDialog.dismiss()
//                    navigateBasedOnRole()  // ← only change here
//                }
//                is AuthState.Error -> {
//                    loadingDialog.dismiss()
//                    showError(state.message)
//                }
//            }
//        }
//    }
//
//    private fun navigateBasedOnRole() {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(uid)
//            .get()
//            .addOnSuccessListener { doc ->
//                val role = doc.getString("role") ?: "student"
//                val destination = if (role == "teacher") {
//                    TeacherDashboardActivity::class.java
//                } else {
//                    MainActivity::class.java
//                }
//                startActivity(Intent(this, destination))
//                finish()
//            }
//            .addOnFailureListener {
//                // Fallback to student on any Firestore error
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//    }
//
//    private fun showError(message: String) {
//        binding.tvError.text = message
//        binding.tvError.visibility = View.VISIBLE
//    }
//
//    private fun hideError() {
//        binding.tvError.visibility = View.GONE
//    }
//}



package com.example.edureach1.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.edureach1.R
import com.example.edureach1.databinding.ActivityLoginBinding
import com.example.edureach1.utils.LoadingDialog
import com.example.edureach1.viewmodels.AuthState
import com.example.edureach1.viewmodels.AuthViewModel
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

            if (email.isEmpty()) {
                showError("Please enter your email")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showError("Please enter your password")
                return@setOnClickListener
            }

            // ── Read which portal the user selected ──────────────────────────
            val selectedRole = if (binding.rgRole.checkedRadioButtonId == R.id.rbTeacher)
                "teacher" else "student"
            // ─────────────────────────────────────────────────────────────────

            hideError()
            viewModel.login(email, password, selectedRole)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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