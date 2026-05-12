package com.nikhil.aceshiksha.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nikhil.aceshiksha.activities.CarRaceActivity
import com.nikhil.aceshiksha.activities.MazeGameActivity
import com.nikhil.aceshiksha.activities.MemoryMatchActivity   // ✅ ADD
import com.nikhil.aceshiksha.activities.QuizBattleActivity
import com.nikhil.aceshiksha.activities.TrueOrFalseActivity   // ✅ ADD
import com.nikhil.aceshiksha.databinding.FragmentGameMenuBinding
import com.nikhil.aceshiksha.repository.AuthRepository
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_CAR_RACE
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_MAZE
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_MEMORY_MATCH   // ✅ ADD
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_QUIZ_BATTLE
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_TRUE_OR_FALSE  // ✅ ADD

const val EXTRA_GAME_TYPE = "game_type"
const val EXTRA_CLASS_LEVEL = "class_level"

class GameMenuFragment : Fragment() {

    private var _binding: FragmentGameMenuBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = authRepository.getCurrentUser()?.uid ?: return

        authRepository.getUserData(uid) { user ->
            if (!isAdded || user == null) return@getUserData
            val classLevel = user.classLevel

            requireActivity().runOnUiThread {
                binding.cardMaze.setOnClickListener {
                    launchGame(MazeGameActivity::class.java, GAME_TYPE_MAZE, classLevel)
                }
                binding.cardCarRace.setOnClickListener {
                    launchGame(CarRaceActivity::class.java, GAME_TYPE_CAR_RACE, classLevel)
                }
                binding.cardQuizBattle.setOnClickListener {
                    launchGame(QuizBattleActivity::class.java, GAME_TYPE_QUIZ_BATTLE, classLevel)
                }
                binding.cardMemoryMatch.setOnClickListener {   // ✅ ADD
                    launchGame(MemoryMatchActivity::class.java, GAME_TYPE_MEMORY_MATCH, classLevel)
                }
                binding.cardTrueOrFalse.setOnClickListener {  // ✅ ADD
                    launchGame(TrueOrFalseActivity::class.java, GAME_TYPE_TRUE_OR_FALSE, classLevel)
                }
            }
        }
    }

    private fun launchGame(activityClass: Class<*>, gameType: String, classLevel: String) {
        val intent = Intent(requireContext(), activityClass).apply {
            putExtra(EXTRA_GAME_TYPE, gameType)
            putExtra(EXTRA_CLASS_LEVEL, classLevel)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}