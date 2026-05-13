package com.nikhil.aceshiksha.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nikhil.aceshiksha.adapters.LeaderboardAdapter
import com.nikhil.aceshiksha.databinding.FragmentLeaderboardBinding
import com.nikhil.aceshiksha.repository.AuthRepository
import com.nikhil.aceshiksha.viewmodels.LeaderboardState
import com.nikhil.aceshiksha.viewmodels.LeaderboardViewModel

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeaderboardViewModel by viewModels()
    private val authRepository = AuthRepository()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        loadLeaderboard()
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(emptyList(), "")
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.leaderboardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LeaderboardState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutContent.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is LeaderboardState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = state.data
                    if (data.topUsers.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.layoutContent.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.layoutContent.visibility = View.VISIBLE
                        adapter = LeaderboardAdapter(data.topUsers, data.currentUserUid)
                        binding.rvLeaderboard.adapter = adapter

                        // calculate rank from list position if user is in top list
                        val positionInList = data.topUsers.indexOfFirst { it.uid == data.currentUserUid }
                        val isInTop = positionInList >= 0
                        val displayRank = if (isInTop) positionInList + 1 else data.currentUserRank

                        if (displayRank > 0) {
                            binding.cardMyRank.visibility = View.VISIBLE
                            binding.tvMyRank.text = "Your Rank: #$displayRank"
                        } else {
                            binding.cardMyRank.visibility = View.GONE
                        }
                    }
                }
                is LeaderboardState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.layoutContent.visibility = View.GONE
                }
            }
        }
    }

    private fun loadLeaderboard() {
        val firebaseUser = authRepository.getCurrentUser() ?: return
        authRepository.getUserData(firebaseUser.uid) { user ->
            if (!isAdded || user == null) return@getUserData
            requireActivity().runOnUiThread {
                viewModel.loadLeaderboard(user)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
