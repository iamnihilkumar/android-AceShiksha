package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.nikhil.aceshiksha.R
import com.nikhil.aceshiksha.databinding.ActivitySettingsBinding
import com.nikhil.aceshiksha.repository.AuthRepository
import com.nikhil.aceshiksha.utils.LoadingDialog
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val auth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        val email = auth.currentUser?.email ?: "Unknown"
        binding.tvEmail.text = "Email: $email"

        binding.tvLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.tvDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage(
                "This will permanently delete your account and all your data " +
                        "(quiz history, game scores, progress). This cannot be undone.\n\n" +
                        "Are you sure?"
            )
            .setPositiveButton("Continue") { _, _ ->
                showReAuthDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showReAuthDialog() {
        // Firebase requires recent authentication before deleting an account.
        // We ask the user to confirm their password first.
        val dialogView = layoutInflater.inflate(R.layout.dialog_reauth, null)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etReAuthPassword)

        AlertDialog.Builder(this)
            .setTitle("Confirm Your Password")
            .setMessage("Enter your password to confirm account deletion.")
            .setView(dialogView)
            .setPositiveButton("Delete My Account") { _, _ ->
                val password = etPassword.text?.toString()?.trim() ?: ""
                if (password.isEmpty()) {
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                reAuthAndDelete(password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reAuthAndDelete(password: String) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        loadingDialog.show()

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Re-auth succeeded — now delete
                lifecycleScope.launch {
                    val result = authRepository.deleteAccount()
                    loadingDialog.dismiss()
                    result.fold(
                        onSuccess = {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Account deleted successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        },
                        onFailure = { e ->
                            Toast.makeText(
                                this@SettingsActivity,
                                "Failed to delete account: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(
                    this,
                    "Incorrect password. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}