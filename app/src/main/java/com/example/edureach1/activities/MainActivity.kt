package com.example.edureach1.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.edureach1.R
import com.example.edureach1.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        setupBottomNav()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "student"
                if (role == "teacher") {
                    startActivity(Intent(this, TeacherDashboardActivity::class.java))
                    finish()
                }
            }
    }

    private fun setupBottomNav() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Manually handle bottom nav clicks instead of setupWithNavController
        // This prevents the back stack confusion when cards navigate to
        // bottom nav destinations (subjectsFragment, leaderboardFragment)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            navigateToBottomNavDestination(item.itemId)
            true
        }

        // Keep bottom nav icon highlighted in sync with current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menu = binding.bottomNavigationView.menu
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                if (menuItem.itemId == destination.id) {
                    menuItem.isChecked = true
                    break
                }
            }
        }
    }

    private fun navigateToBottomNavDestination(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        // Always clear the entire back stack back to homeFragment first,
        // then navigate to the selected tab fresh — no stale entries
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, inclusive = false, saveState = false)
            .setLaunchSingleTop(true)
            .setRestoreState(false)
            .build()

        // If going home, pop everything above it instead of navigating
        if (destinationId == R.id.homeFragment) {
            navController.popBackStack(R.id.homeFragment, inclusive = false)
            return
        }

        navController.navigate(destinationId, null, navOptions)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}