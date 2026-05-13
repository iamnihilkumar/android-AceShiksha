package com.nikhil.aceshiksha.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityCarRaceBinding
import com.nikhil.aceshiksha.fragments.EXTRA_CLASS_LEVEL
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_CAR_RACE
import com.nikhil.aceshiksha.viewmodels.GameState
import com.nikhil.aceshiksha.viewmodels.GameViewModel
import com.google.firebase.auth.FirebaseAuth

class CarRaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarRaceBinding
    private val viewModel: GameViewModel by viewModels()
    private var correctCount = 0

    // student UID
    private val studentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarRaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val classLevel = intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6"

        observeGame()
        setupAnswerButtons()


        viewModel.loadQuestions(
            classLevel,
            GAME_TYPE_CAR_RACE,
            studentUid
        )
    }

    private fun observeGame() {
        viewModel.gameState.observe(this) { state ->
            when (state) {
                is GameState.Loading -> {
                    binding.progressBarCar.visibility = View.VISIBLE
                    binding.layoutCarNoQuestions.visibility = View.GONE
                }
                is GameState.NoQuestions -> {
                    binding.progressBarCar.visibility = View.GONE
                    binding.layoutCarNoQuestions.visibility = View.VISIBLE
                }
                is GameState.Playing -> {
                    binding.progressBarCar.visibility = View.GONE
                    binding.layoutCarNoQuestions.visibility = View.GONE
                    binding.tvCarQuestion.text = "Question ${state.index} of ${state.total}"
                    binding.tvCarQuestionText.text = state.question.questionText
                    binding.btnCarA.text = "A.  ${state.question.optionA}"
                    binding.btnCarB.text = "B.  ${state.question.optionB}"
                    binding.btnCarC.text = "C.  ${state.question.optionC}"
                    binding.btnCarD.text = "D.  ${state.question.optionD}"
                    resetButtonColors()
                    setAnswerButtonsEnabled(true)
                    binding.tvCarFeedback.visibility = View.GONE
                    binding.btnCarNext.visibility = View.GONE
                }
                is GameState.Finished -> {
                    launchResult(state.correct, state.total, state.xpEarned, GAME_TYPE_CAR_RACE)
                }
                is GameState.Error -> { }
            }
        }

        viewModel.answerResult.observe(this) { isCorrect ->
            if (isCorrect == null) return@observe
            val question = viewModel.getCurrentQuestion() ?: return@observe
            setAnswerButtonsEnabled(false)

            if (isCorrect) {
                correctCount++
                val progress = (correctCount.toFloat() / 10f * 10).toInt()
                binding.progressCarRace.progress = progress

                val carX = (binding.progressCarRace.width * correctCount / 10f) - 32f
                binding.tvCarEmoji.animate().translationX(carX.coerceAtLeast(0f)).setDuration(400).start()

                binding.tvCarScore.text = "Speed: ${correctCount * 10}%"
                binding.tvCarFeedback.text = "✅ Correct! Your car accelerates!"
                binding.tvCarFeedback.setTextColor(Color.parseColor("#1565C0"))

            } else {
                binding.tvCarFeedback.text = "❌ Wrong! Correct answer: ${question.correctAnswer}"
                binding.tvCarFeedback.setTextColor(Color.parseColor("#B71C1C"))

                val correctBtn = when (question.correctAnswer) {
                    "A" -> binding.btnCarA
                    "B" -> binding.btnCarB
                    "C" -> binding.btnCarC
                    "D" -> binding.btnCarD
                    else -> null
                }
                correctBtn?.setBackgroundColor(Color.parseColor("#C8E6C9"))
            }

            binding.tvCarFeedback.visibility = View.VISIBLE
            binding.btnCarNext.visibility = View.VISIBLE
        }
    }

    private fun setupAnswerButtons() {
        binding.btnCarA.setOnClickListener { viewModel.submitAnswer("A") }
        binding.btnCarB.setOnClickListener { viewModel.submitAnswer("B") }
        binding.btnCarC.setOnClickListener { viewModel.submitAnswer("C") }
        binding.btnCarD.setOnClickListener { viewModel.submitAnswer("D") }
        binding.btnCarNext.setOnClickListener { viewModel.nextQuestion() }
    }

    private fun resetButtonColors() {
        val defaultColor = Color.parseColor("#E3F2FD")
        binding.btnCarA.setBackgroundColor(defaultColor)
        binding.btnCarB.setBackgroundColor(defaultColor)
        binding.btnCarC.setBackgroundColor(defaultColor)
        binding.btnCarD.setBackgroundColor(defaultColor)
    }

    private fun setAnswerButtonsEnabled(enabled: Boolean) {
        binding.btnCarA.isEnabled = enabled
        binding.btnCarB.isEnabled = enabled
        binding.btnCarC.isEnabled = enabled
        binding.btnCarD.isEnabled = enabled
    }

    private fun launchResult(correct: Int, total: Int, xp: Int, gameType: String) {
        startActivity(Intent(this, GameResultActivity::class.java).apply {
            putExtra("correct", correct)
            putExtra("total", total)
            putExtra("xp", xp)
            putExtra("game_type", gameType)
        })
        finish()
    }
}