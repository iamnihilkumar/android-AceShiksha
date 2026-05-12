package com.nikhil.aceshiksha.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.aceshiksha.databinding.ActivityTrueOrFalseBinding
import com.nikhil.aceshiksha.fragments.EXTRA_CLASS_LEVEL
import com.nikhil.aceshiksha.utils.Constants.GAME_TYPE_TRUE_OR_FALSE
import com.nikhil.aceshiksha.viewmodels.GameState
import com.nikhil.aceshiksha.viewmodels.GameViewModel
import com.google.firebase.auth.FirebaseAuth

class TrueOrFalseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrueOrFalseBinding
    private val viewModel: GameViewModel by viewModels()
    private val studentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var countDownTimer: CountDownTimer? = null
    private val TIME_PER_QUESTION = 8000L   // 8 seconds
    private var streak = 0
    private var correctCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrueOrFalseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            countDownTimer?.cancel()
            finish()
        }

        val classLevel = intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6"

        observeViewModel()
        viewModel.loadQuestions(classLevel, GAME_TYPE_TRUE_OR_FALSE, studentUid)
    }

    private fun observeViewModel() {
        viewModel.gameState.observe(this) { state ->
            when (state) {
                is GameState.Loading -> {
                    binding.progressBarTof.visibility = View.VISIBLE
                    binding.layoutTofNoQuestions.visibility = View.GONE
                    binding.layoutTofGame.visibility = View.GONE
                }
                is GameState.NoQuestions -> {
                    binding.progressBarTof.visibility = View.GONE
                    binding.layoutTofNoQuestions.visibility = View.VISIBLE
                }
                is GameState.Playing -> {
                    binding.progressBarTof.visibility = View.GONE
                    binding.layoutTofGame.visibility = View.VISIBLE
                    binding.layoutTofNoQuestions.visibility = View.GONE

                    binding.tvTofQuestion.text = state.question.questionText
                    binding.tvTofProgress.text = "${state.index} / ${state.total}"
                    binding.tvTofFeedback.visibility = View.GONE
                    setButtonsEnabled(true)
                    resetButtonColors()
                    startTimer()
                }
                is GameState.Finished -> {
                    countDownTimer?.cancel()
                    launchResult(state.correct, state.total, state.xpEarned)
                }
                is GameState.Error -> {}
            }
        }

        viewModel.answerResult.observe(this) { isCorrect ->
            if (isCorrect == null) return@observe
            countDownTimer?.cancel()
            setButtonsEnabled(false)

            val question = viewModel.getCurrentQuestion() ?: return@observe
            // correctAnswer == "A" → True,  "B" → False
            val correctLabel = if (question.correctAnswer == "A") "TRUE" else "FALSE"

            if (isCorrect) {
                streak++
                correctCount++
                val streakBonus = if (streak > 1) " 🔥 ×$streak streak!" else ""
                binding.tvTofFeedback.text = "✅ Correct!$streakBonus"
                binding.tvTofFeedback.setTextColor(Color.parseColor("#1B5E20"))
                val correctBtn = if (question.correctAnswer == "A") binding.btnTrue else binding.btnFalse
                correctBtn.setBackgroundColor(Color.parseColor("#C8E6C9"))
            } else {
                streak = 0
                binding.tvTofFeedback.text = "❌ Wrong! It was $correctLabel"
                binding.tvTofFeedback.setTextColor(Color.parseColor("#B71C1C"))
                val correctBtn = if (question.correctAnswer == "A") binding.btnTrue else binding.btnFalse
                correctBtn.setBackgroundColor(Color.parseColor("#C8E6C9"))
            }

            binding.tvTofStreak.text = if (streak > 1) "🔥 Streak: $streak" else ""
            binding.tvTofFeedback.visibility = View.VISIBLE

            // Auto-advance after 1.2 seconds
            binding.root.postDelayed({
                viewModel.nextQuestion()
            }, 1200)
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        binding.progressBarTimer.max = TIME_PER_QUESTION.toInt()
        binding.progressBarTimer.progress = TIME_PER_QUESTION.toInt()

        // Animate the progress bar smoothly
        val animator = ObjectAnimator.ofInt(
            binding.progressBarTimer, "progress",
            TIME_PER_QUESTION.toInt(), 0
        ).apply {
            duration = TIME_PER_QUESTION
            interpolator = LinearInterpolator()
            start()
        }

        countDownTimer = object : CountDownTimer(TIME_PER_QUESTION, TIME_PER_QUESTION) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                // Time's up — treat as wrong answer
                streak = 0
                binding.tvTofStreak.text = ""
                binding.tvTofFeedback.text = "⏰ Time's up!"
                binding.tvTofFeedback.setTextColor(Color.parseColor("#E65100"))
                binding.tvTofFeedback.visibility = View.VISIBLE
                setButtonsEnabled(false)
                animator.cancel()

                binding.root.postDelayed({
                    viewModel.nextQuestion()
                }, 1200)
            }
        }.start()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnTrue.isEnabled = enabled
        binding.btnFalse.isEnabled = enabled
    }

    private fun resetButtonColors() {
        binding.btnTrue.setBackgroundColor(Color.parseColor("#E8F5E9"))
        binding.btnFalse.setBackgroundColor(Color.parseColor("#FFEBEE"))
    }

    private fun setupButtons() {
        // correctAnswer "A" = True, "B" = False
        binding.btnTrue.setOnClickListener { viewModel.submitAnswer("A") }
        binding.btnFalse.setOnClickListener { viewModel.submitAnswer("B") }
    }

    override fun onStart() {
        super.onStart()
        setupButtons()
    }

    private fun launchResult(correct: Int, total: Int, xp: Int) {
        startActivity(Intent(this, GameResultActivity::class.java).apply {
            putExtra("correct", correct)
            putExtra("total", total)
            putExtra("xp", xp)
            putExtra("game_type", GAME_TYPE_TRUE_OR_FALSE)
            putExtra("class_level", intent.getStringExtra(EXTRA_CLASS_LEVEL) ?: "6")
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
