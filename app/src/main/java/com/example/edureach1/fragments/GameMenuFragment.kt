package com.example.edureach1.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.edureach1.activities.CarRaceActivity
import com.example.edureach1.activities.MazeGameActivity
import com.example.edureach1.activities.QuizBattleActivity
import com.example.edureach1.databinding.FragmentGameMenuBinding
import com.example.edureach1.repository.AuthRepository
import com.example.edureach1.utils.Constants.GAME_TYPE_CAR_RACE
import com.example.edureach1.utils.Constants.GAME_TYPE_MAZE
import com.example.edureach1.utils.Constants.GAME_TYPE_QUIZ_BATTLE

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