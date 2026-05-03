package com.nikhil.aceshiksha.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nikhil.aceshiksha.activities.SettingsActivity
import com.nikhil.aceshiksha.activities.StudentReportActivity
import com.nikhil.aceshiksha.databinding.FragmentProfileBinding
import com.nikhil.aceshiksha.repository.AuthRepository

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
        binding.layoutSettingsShortcut.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.btnViewReports.setOnClickListener {
            startActivity(Intent(requireContext(), StudentReportActivity::class.java))
        }
    }

    private fun getLevelFromXp(xp: Int): Int = (xp / 100) + 1

    private fun loadProfile() {
        val firebaseUser = authRepository.getCurrentUser() ?: return
        authRepository.getUserData(firebaseUser.uid) { user ->
            if (!isAdded || user == null) return@getUserData
            requireActivity().runOnUiThread {
                val initials = user.name
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it[0].uppercaseChar().toString() }
                binding.tvAvatarInitials.text = initials
                binding.tvProfileName.text = user.name
                binding.tvProfileEmail.text = user.email
                binding.tvProfileClass.text = "Class ${user.classLevel}"
                binding.tvProfileXp.text = user.xp.toString()
                binding.tvProfileLevel.text = getLevelFromXp(user.xp).toString()
                binding.tvProfileStreak.text = user.streak.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}