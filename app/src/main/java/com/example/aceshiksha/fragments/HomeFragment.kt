package com.nikhil.aceshiksha.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.nikhil.aceshiksha.R
import com.nikhil.aceshiksha.databinding.FragmentHomeBinding
import com.nikhil.aceshiksha.viewmodels.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.loadQuizAccuracy(uid)
    }

    // ✅ Re-fetch user from server every time Home becomes visible
    // (e.g. returning from QuizResultActivity — streak/XP will now be fresh)
    override fun onResume() {
        super.onResume()
        viewModel.loadUser()
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvGreeting.text = "Hello, ${user.name.split(" ").first()}!"
            binding.tvLevel.text = "Learning Level ${viewModel.getLevel(user.xp)}"
            binding.tvXP.text = "${user.xp} XP"
            binding.tvStreak.text = "🔥 ${user.streak}-Day Streak"
            binding.progressXP.progress = viewModel.getXpProgress(user.xp)
            binding.tvTotalXp.text = "${user.xp}"
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            binding.tvGreeting.text = "Hello!"
        }

        viewModel.quizAccuracy.observe(viewLifecycleOwner) { accuracyData ->
            binding.tvQuizAccuracy.text = "${accuracyData.accuracy}%"
            binding.tvQuizzesTaken.text = "${accuracyData.quizzesTaken}"
        }
    }

    private fun setupClickListeners() {
        binding.cardSubjects.setOnClickListener {
            findNavController().navigate(R.id.subjectsFragment)
        }
        binding.cardLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.leaderboardFragment)
        }
        binding.cardContinue.setOnClickListener {
            findNavController().navigate(R.id.subjectsFragment)
        }
    }

    private fun navigateAsBottomNav(destinationId: Int) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, inclusive = false)
            .setLaunchSingleTop(true)
            .build()
        findNavController().navigate(destinationId, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}